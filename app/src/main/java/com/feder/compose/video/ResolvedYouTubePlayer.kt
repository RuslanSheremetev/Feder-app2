package com.feder.compose.video

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(UnstableApi::class)
@Composable
fun ResolvedYouTubePlayer(
    videoId: String,
    proxyBaseUrl: String = "http://2.26.71.102:8018",
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = false,
    onClose: (() -> Unit)? = null
) {
    val ctx = LocalContext.current
    var resolvedUrl by remember(videoId) { mutableStateOf<String?>(null) }
    var error by remember(videoId) { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(videoId) {
        // Сервер проксирует поток: отдаёт mp4 напрямую.
        // Клиент просто играет URL.
        resolvedUrl = "$proxyBaseUrl/youtube/stream?id=$videoId"
        error = null
    }

    LaunchedEffect(resolvedUrl) {
        val u = resolvedUrl ?: return@LaunchedEffect
        val p = ExoPlayer.Builder(ctx).build().apply {
            val dsf = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .setAllowCrossProtocolRedirects(true)
            setMediaSource(
                ProgressiveMediaSource.Factory(dsf)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(u)))
            )
            playWhenReady = true
            prepare()
        }
        player = p
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
            player = null
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        when {
            error != null -> Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠ $error", color = Color(0xFFFF6B6B), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Попробуйте другое видео", color = Color(0xFF8A919E), fontSize = 11.sp)
            }
            resolvedUrl == null -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White)
                Text("Загрузка…", color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
            }
            else -> AndroidView(
                factory = { c -> PlayerView(c).apply {
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    this.player = player
                }},
                modifier = Modifier.fillMaxSize(),
                update = { view -> view.player = player }
            )
        }
        if (showCloseButton && onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(36.dp)
                    .background(Color(0xAA000000), CircleShape)
            ) {
                Icon(Icons.Filled.Close, "close", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun FullscreenYouTubePlayer(
    videoId: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        ResolvedYouTubePlayer(
            videoId = videoId,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            showCloseButton = true,
            onClose = onClose
        )
    }
}
