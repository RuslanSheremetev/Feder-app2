package com.feder.compose.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun VideoBubble(
    videoUrl: String,
    token: String,
    isMine: Boolean,
    time: String,
    msgStatus: String,
    thumbUrl: String? = null,
    isVisible: Boolean = true,
    onLongPress: (() -> Unit)? = null,
    onOpenFullscreen: (() -> Unit)? = null
) {
    val ctx = LocalContext.current
    val player = rememberFederVideoPlayer()

    val fullVideoUrl = remember(videoUrl, token) {
        if (videoUrl.startsWith("http")) {
            if (videoUrl.contains("?")) videoUrl else "$videoUrl?token=$token"
        } else {
            "http://2.26.71.102:8018/videos/$videoUrl?token=$token"
        }
    }

    val fullThumbUrl = remember(thumbUrl) {
        thumbUrl?.let {
            if (it.startsWith("http")) it
            else "http://2.26.71.102:8018/video_thumbs/$it?token=$token"
        }
    }

    LaunchedEffect(fullVideoUrl) { player.prepare(fullVideoUrl) }

    LaunchedEffect(isVisible, player.isPrepared) {
        if (player.isPrepared) {
            if (isVisible) player.play() else player.pause()
        }
    }

    Box(
        Modifier
            .width(280.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (player.isPrepared) player.toggle() },
                    onLongPress = { onLongPress?.invoke() }
                )
            }
    ) {
        VideoSurface(
            modifier = Modifier.fillMaxSize(),
            onSurfaceReady = { holder -> player.attachSurface(holder) },
            onSurfaceDestroyed = { player.detachSurface() }
        )

        if (!player.isPlaying && fullThumbUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(fullThumbUrl).build(),
                contentDescription = "video preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (player.isPrepared && !player.isPlaying) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.Center).size(64.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.PlayArrow, "play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp).padding(start = 4.dp)
                    )
                }
            }
        }

        if (player.isPlaying) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(32.dp)
                    .clickable { player.setMuted(!player.isMuted) }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        if (player.isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        "mute",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (player.isPrepared && player.duration > 0) {
            val progress = (player.position.toFloat() / player.duration.toFloat()).coerceIn(0f, 1f)
            Box(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(progress).background(Color.White)
                )
            }
        }

        if (!player.isPrepared && player.lastError == null) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.align(Alignment.Center).size(32.dp)
            )
        }

        player.lastError?.let { err ->
            Box(
                Modifier.align(Alignment.Center).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(err.take(60), color = Color.White, fontSize = 10.sp)
            }
        }
    }
}
