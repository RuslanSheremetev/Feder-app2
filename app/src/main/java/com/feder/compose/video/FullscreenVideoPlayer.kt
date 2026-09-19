package com.feder.compose.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FullscreenVideoPlayer(
    videoUrl: String,
    token: String,
    onClose: () -> Unit
) {
    val player = rememberFederVideoPlayer()
    var showControls by remember { mutableStateOf(true) }

    val fullUrl = remember(videoUrl, token) {
        if (videoUrl.startsWith("http")) {
            if (videoUrl.contains("?")) videoUrl else "$videoUrl?token=$token"
        } else {
            "http://2.26.71.102:8018/videos/$videoUrl?token=$token"
        }
    }

    LaunchedEffect(fullUrl) { player.prepare(fullUrl) }

    LaunchedEffect(player.isPrepared) {
        if (player.isPrepared) {
            player.setMuted(false)
            player.play()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showControls = !showControls })
            }
    ) {
        VideoSurface(
            modifier = Modifier.fillMaxSize(),
            onSurfaceReady = { holder -> player.attachSurface(holder) },
            onSurfaceDestroyed = { player.detachSurface() }
        )

        if (showControls) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(40.dp)
                    .clickable { onClose() }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, "close", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                if (player.duration > 0) {
                    Box(
                        Modifier.fillMaxWidth().height(4.dp)
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    ) {
                        val progress = (player.position.toFloat() / player.duration).coerceIn(0f, 1f)
                        Box(
                            Modifier.fillMaxHeight().fillMaxWidth(progress)
                                .background(Color.White, RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (player.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "play-pause",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp).clickable { player.toggle() }
                    )
                    Text(
                        "${fmt(player.position)} / ${fmt(player.duration)}",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Icon(
                        if (player.isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        "mute",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp).clickable { player.setMuted(!player.isMuted) }
                    )
                }
            }
        }
    }
}

private fun fmt(ms: Int): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
