package com.feder.compose.video

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * FederVideoPlayer — свой видеоплеер на MediaPlayer + SurfaceView.
 *
 * Паттерн: backing property.
 * - Публичные поля — val (Compose читает, не пишет)
 * - Внутренние _X — mutableStateOf (private, меняются только изнутри)
 *
 * Так избегаем бага Kotlin 2.0: `var X by mutableStateOf(...)` + `private set`
 * (компилятор запрещает присваивание X даже внутри класса).
 */
class FederVideoPlayer(private val ctx: Context) {

    // ─── Internal state (backing properties) ───
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

    private val _lastError = mutableStateOf<String?>(null)
    val lastError: String? get() = _lastError.value

    // ─── Native objects ───
    private var mediaPlayer: MediaPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var progressJob: Job? = null

    // ─── Surface management ───
    fun attachSurface(holder: SurfaceHolder) {
        surfaceHolder = holder
        mediaPlayer?.setDisplay(holder)
    }

    fun detachSurface() {
        try { mediaPlayer?.setDisplay(null) } catch (_: Exception) {}
        surfaceHolder = null
    }

    // ─── Prepare ───
    fun prepare(url: String) {
        release()
        _lastError.value = null

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
                setVolume(0f, 0f)

                setOnPreparedListener { prepared ->
                    Log.d("FederVideo", "Prepared: ${prepared.duration}ms")
                    _duration.value = prepared.duration
                    _isPrepared.value = true
                    surfaceHolder?.let { prepared.setDisplay(it) }
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _position.value = 0
                    try { it.seekTo(0) } catch (_: Exception) {}
                }

                setOnErrorListener { _, what, extra ->
                    _lastError.value = "err what=$what extra=$extra"
                    Log.e("FederVideo", _lastError.value!!)
                    _isPrepared.value = false
                    _isPlaying.value = false
                    true
                }

                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            _lastError.value = e.message
            Log.e("FederVideo", "prepare failed", e)
        }
    }

    // ─── Playback ───
    fun play() {
        val mp = mediaPlayer ?: return
        if (!isPrepared) return
        try {
            mp.start()
            _isPlaying.value = true
            startTicker()
        } catch (e: Exception) {
            Log.e("FederVideo", "play failed", e)
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
            _isPlaying.value = false
            stopTicker()
        } catch (_: Exception) {}
    }

    fun toggle() {
        if (isPlaying) pause() else play()
    }

    // ─── Volume ───
    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        val v = if (muted) 0f else 1f
        try { mediaPlayer?.setVolume(v, v) } catch (_: Exception) {}
    }

    // ─── Seek ───
    fun seekTo(ms: Int) {
        try {
            mediaPlayer?.seekTo(ms)
            _position.value = ms
        } catch (_: Exception) {}
    }

    // ─── Release ───
    fun release() {
        stopTicker()
        try {
            mediaPlayer?.setDisplay(null)
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPrepared.value = false
        _isPlaying.value = false
        _position.value = 0
        _duration.value = 0
    }

    // ─── Progress ticker ───
    private fun startTicker() {
        stopTicker()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                try {
                    val mp = mediaPlayer
                    if (mp != null && _isPrepared.value) {
                        _position.value = try { mp.currentPosition } catch (_: Exception) { _position.value }
                    }
                } catch (_: Exception) {}
                delay(200)
            }
        }
    }

    private fun stopTicker() {
        try { progressJob?.cancel() } catch (_: Exception) {}
        progressJob = null
    }
}

@Composable
fun rememberFederVideoPlayer(): FederVideoPlayer {
    val ctx = LocalContext.current
    val player = remember { FederVideoPlayer(ctx) }
    DisposableEffect(Unit) { onDispose { player.release() } }
    return player
}
