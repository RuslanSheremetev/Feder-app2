package com.feder.compose.stories

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.feder.compose.video.VideoSurface

/**
 * StoryVideoPlayer — свой видеоплеер для stories.
 *
 * Отличия от FederVideoPlayer (чат-видео):
 *   - Autoplay при isActive = true (не по тапу)
 *   - Callback onDurationKnown(ms) — для прогресс-бара в StoryViewer
 *   - Callback onCompleted() — когда видео закончилось (переключить на след.)
 *   - Callback onError(msg) — если не загрузилось
 *   - Без внутреннего прогресс-бара (progress рисует StoryViewer)
 *
 * Паттерн: backing property (Kotlin 2.0 safe).
 */
class StoryVideoPlayer(private val ctx: Context) {

    // ─── Backing properties ───
    private val _isPrepared = mutableStateOf(false)
    val isPrepared: Boolean get() = _isPrepared.value

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: Boolean get() = _isPlaying.value

    private val _duration = mutableStateOf(0)
    val duration: Int get() = _duration.value

    private val _position = mutableStateOf(0)
    val position: Int get() = _position.value

    private val _isMuted = mutableStateOf(true)
    val isMuted: Boolean get() = _isMuted.value

    // ─── Native ───
    private var mediaPlayer: MediaPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var pendingUrl: String? = null

    // ─── Surface ───
    fun attachSurface(holder: SurfaceHolder) {
        surfaceHolder = holder
        try { mediaPlayer?.setDisplay(holder) } catch (_: Exception) {}
    }

    fun detachSurface() {
        try { mediaPlayer?.setDisplay(null) } catch (_: Exception) {}
        surfaceHolder = null
    }

    // ─── Prepare ───
    fun prepare(
        url: String,
        onDurationKnown: (Int) -> Unit = {},
        onCompleted: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        release()
        pendingUrl = url

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(ctx, Uri.parse(url))
                isLooping = false
                setVolume(0f, 0f)  // start muted

                setOnPreparedListener { prepared ->
                    Log.d("StoryVideo", "Prepared: ${prepared.duration}ms")
                    _duration.value = prepared.duration
                    onDurationKnown(prepared.duration)
                    _isPrepared.value = true
                    surfaceHolder?.let { prepared.setDisplay(it) }
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    onCompleted()
                }

                setOnErrorListener { _, what, extra ->
                    val msg = "err what=$what extra=$extra"
                    Log.e("StoryVideo", msg)
                    _isPrepared.value = false
                    _isPlaying.value = false
                    onError(msg)
                    true
                }

                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e("StoryVideo", "prepare failed", e)
            onError(e.message ?: "unknown")
        }
    }

    // ─── Playback ───
    fun play() {
        val mp = mediaPlayer ?: return
        if (!isPrepared) return
        try {
            mp.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            Log.e("StoryVideo", "play failed", e)
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
            _isPlaying.value = false
        } catch (_: Exception) {}
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        val v = if (muted) 0f else 1f
        try { mediaPlayer?.setVolume(v, v) } catch (_: Exception) {}
    }

    /**
     * Обновить текущую позицию (для прогресс-бара).
     * StoryViewer сам вызовет в LaunchedEffect каждые 100-200мс.
     */
    fun refreshPosition() {
        try {
            _position.value = mediaPlayer?.currentPosition ?: 0
        } catch (_: Exception) {}
    }

    // ─── Release ───
    fun release() {
        try {
            mediaPlayer?.setDisplay(null)
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        surfaceHolder = null
        _isPrepared.value = false
        _isPlaying.value = false
        _position.value = 0
        _duration.value = 0
    }
}

/**
 * Composable обёртка для StoryVideoPlayer.
 * Автоматически: prepare → autoplay если isActive → release при закрытии.
 */
@Composable
fun StoryVideoPlayerView(
    videoUrl: String,
    isActive: Boolean,
    muted: Boolean = true,
    onDurationKnown: (Int) -> Unit = {},
    onCompleted: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val player = remember { StoryVideoPlayer(ctx) }

    // Prepare при смене URL
    LaunchedEffect(videoUrl) {
        player.prepare(videoUrl, onDurationKnown, onCompleted, onError)
    }

    // Autoplay / pause
    LaunchedEffect(isActive, player.isPrepared) {
        if (player.isPrepared) {
            if (isActive) player.play() else player.pause()
        }
    }

    // Mute toggle
    LaunchedEffect(muted) {
        player.setMuted(muted)
    }

    // Release при уходе
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    VideoSurface(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        onSurfaceReady = { holder -> player.attachSurface(holder) },
        onSurfaceDestroyed = { player.detachSurface() }
    )
}
