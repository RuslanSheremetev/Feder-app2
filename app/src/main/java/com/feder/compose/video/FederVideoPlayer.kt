package com.feder.compose.video

import android.content.Context
import android.net.Uri
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * FederVideoPlayer v2 — на Media3/ExoPlayer.
 *
 * Умеет:
 *  - mp4 / webm / mkv (прямые ссылки)
 *  - HLS (.m3u8)
 *  - YouTube (youtube.com/watch, youtu.be, /shorts/, /embed/) — через youtube-extractor
 *  - Другие источники (VK Video, Vimeo — если отдают прямую ссылку или HLS)
 */
@OptIn(UnstableApi::class)
class FederVideoPlayer(private val ctx: Context) {

    private var player: ExoPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var currentUrl: String? = null
    private var isReleased = false

    // Слушатели
    var onPrepared: ((Int) -> Unit)? = null       // durationMs
    var onProgress: ((Int) -> Unit)? = null       // positionMs
    var onCompleted: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onBuffering: ((Boolean) -> Unit)? = null
    var lastError: String? = null
    private var mutedState: Boolean = false

    // Тикер прогресса
    private var tickerActive = AtomicBoolean(false)

    init {
        player = ExoPlayer.Builder(ctx).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            onPrepared?.invoke((duration).toInt())
                        }
                        Player.STATE_ENDED -> {
                            onCompleted?.invoke()
                        }
                        Player.STATE_BUFFERING -> {
                            onBuffering?.invoke(true)
                        }
                    }
                    if (state != Player.STATE_BUFFERING) onBuffering?.invoke(false)
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) startTicker() else stopTicker()
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    lastError = error.message ?: "Playback error"
                    onError?.invoke(lastError!!)
                }
            })
        }
    }

    fun attachSurface(holder: SurfaceHolder) {
        surfaceHolder = holder
        player?.setVideoSurfaceHolder(holder)
    }

    fun detachSurface() {
        player?.clearVideoSurface()
        surfaceHolder = null
    }

    /**
     * Подготовить видео. Если URL — YouTube, сначала разрешаем его через extractor.
     */
    fun prepare(url: String) {
        currentUrl = url
        lastError = null
        // YouTube теперь обрабатывается через InlineYouTubePlayer (WebView).
        // Здесь — только прямые ссылки / HLS.
        prepareDirect(url)
    }

    private fun prepareDirect(url: String) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 13) Feder/1.0")
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        val mediaSource: MediaSource = if (url.contains(".m3u8")) {
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }

        player?.setMediaSource(mediaSource)
        player?.prepare()
    }



    fun play() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
        stopTicker()
    }

    fun toggle() {
        player?.let {
            if (it.isPlaying) pause() else play()
        }
    }

    fun setMuted(muted: Boolean) {
        mutedState = muted
        player?.volume = if (muted) 0f else 1f
    }

    val isMuted: Boolean
        get() = mutedState

    fun seekTo(ms: Int) {
        player?.seekTo(ms.toLong())
    }

    val isPlaying: Boolean
        get() = player?.isPlaying == true





    // ─── Совместимость со старым API ───
    val isPrepared: Boolean
        get() = player?.playbackState == Player.STATE_READY || 
                player?.playbackState == Player.STATE_BUFFERING

    val duration: Int
        get() = (player?.duration ?: 0L).toInt().coerceAtLeast(0)

    val position: Int
        get() = (player?.currentPosition ?: 0L).toInt()

    fun release() {
        if (isReleased) return
        isReleased = true
        stopTicker()
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    private fun startTicker() {
        if (tickerActive.get()) return
        tickerActive.set(true)
        Thread {
            while (tickerActive.get()) {
                try {
                    onProgress?.invoke((player?.currentPosition ?: 0L).toInt())
                    Thread.sleep(200)
                } catch (_: InterruptedException) { break }
            }
        }.apply { isDaemon = true }.start()
    }

    private fun stopTicker() {
        tickerActive.set(false)
    }
}

@Composable
fun rememberFederVideoPlayer(): FederVideoPlayer {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val player = remember { FederVideoPlayer(ctx) }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    return player
}
