package com.feder.compose.video

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Свой видеоплеер на MediaPlayer + SurfaceView. Без ExoPlayer.
 */
class FederVideoPlayer(private val ctx: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null

    var isPrepared by mutableStateOf(false)

        private set
    var isPlaying by mutableStateOf(false)
        private set
    var duration by mutableStateOf(0)
        private set
    var position by mutableStateOf(0)
        private set
    var isMuted by mutableStateOf(true)
        private set
    var lastError by mutableStateOf<String?>(null)
        private set

    private var progressJob: Job? = null

    fun attachSurface(holder: SurfaceHolder) {
        surfaceHolder = holder
        mediaPlayer?.setDisplay(holder)
    }

    fun detachSurface() {
        try { mediaPlayer?.setDisplay(null) } catch (_: Exception) {}
        surfaceHolder = null
    }

    fun prepare(url: String) {
        release()
        lastError = null
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
                setOnPreparedListener { mp ->
                    Log.d("FederVideo", "Prepared: ${mp.duration}ms")
                    duration = mp.duration
                    isPrepared = true
                    surfaceHolder?.let { mp.setDisplay(it) }
                }
                setOnCompletionListener {
                    isPlaying = false
                    position = 0
                    try { it.seekTo(0) } catch (_: Exception) {}
                }
                setOnErrorListener { _, what, extra ->
                    lastError = "err what=$what extra=$extra"
                    Log.e("FederVideo", lastError!!)
                    isPrepared = false
                    isPlaying = false
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            lastError = e.message
            Log.e("FederVideo", "prepare failed", e)
        }
    }

    fun play() {
        val mp = mediaPlayer ?: return
        if (!isPrepared) return
        try {
            mp.start()
            isPlaying = true
            startTicker()
        } catch (e: Exception) {
            Log.e("FederVideo", "play failed", e)
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
            isPlaying = false
            stopTicker()
        } catch (_: Exception) {}
    }

    fun toggle() { if (isPlaying) pause() else play() }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        val v = if (muted) 0f else 1f
        try { mediaPlayer?.setVolume(v, v) } catch (_: Exception) {}
    }

    fun seekTo(ms: Int) {
        try {
            mediaPlayer?.seekTo(ms)
            position = ms
        } catch (_: Exception) {}
    }

    fun release() {
        stopTicker()
        try {
            mediaPlayer?.setDisplay(null)
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isPrepared = false
        isPlaying = false
        position = 0
        duration = 0
    }

    private fun startTicker() {
        stopTicker()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                try {
                    val mp = mediaPlayer
                    if (mp != null && isPrepared) {
                        position = try { mp.currentPosition } catch (_: Exception) { position }
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
