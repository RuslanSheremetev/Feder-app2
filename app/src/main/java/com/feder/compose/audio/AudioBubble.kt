package com.feder.compose.audio

import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlinx.coroutines.delay

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun AudioBubble(
    audioUrl: String,
    token: String,
    isMine: Boolean,
    time: String,
    msgStatus: String,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableStateOf(0) }
    var durationMs by remember { mutableStateOf(0) }

    val fullUrl = if (audioUrl.startsWith("http")) "$audioUrl?token=$token"
                  else "http://2.26.71.102:8014/$audioUrl?token=$token"

    // Обновление позиции, когда играет
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { positionMs = it.currentPosition }
            delay(100)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { player?.stop() } catch (_: Exception) {}
            try { player?.release() } catch (_: Exception) {}
            player = null
        }
    }

    fun togglePlay() {
        if (isPlaying) {
            player?.pause()
            isPlaying = false
        } else {
            if (player == null) {
                try {
                    player = MediaPlayer().apply {
                        setDataSource(fullUrl)
                        setOnPreparedListener {
                            durationMs = it.duration
                            it.start()
                            isPlaying = true
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            positionMs = 0
                            it.seekTo(0)
                        }
                        setOnErrorListener { _, _, _ -> isPlaying = false; true }
                        prepareAsync()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AudioBubble", "play error: ${e.message}")
                }
            } else {
                player?.start()
                isPlaying = true
            }
        }
    }

    // Формат M:SS
    fun fmt(ms: Int): String {
        val s = ms / 1000
        return "%d:%02d".format(s / 60, s % 60)
    }

    Row(
        modifier = Modifier
            .width(240.dp)
            .combinedClickable(onClick = { togglePlay() }, onLongClick = onLongClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/pause button
        Box(
            Modifier.size(44.dp).clip(CircleShape)
                .background(if (isMine) Color.White.copy(alpha = 0.25f) else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                "play",
                tint = if (isMine) Color.White else Color(0xFF339DFF),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            // Waveform (условные полоски, прогресс — синий)
            val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
            Canvas(Modifier.fillMaxWidth().height(24.dp)) {
                val bars = 28
                val barW = size.width / bars
                val gap = 1.5f
                val seed = audioUrl.hashCode()
                for (i in 0 until bars) {
                    val h = ((kotlin.math.abs(seed xor (i * 7919)) % 60) + 30) / 100f
                    val barH = size.height * h
                    val x = i * barW
                    val yTop = (size.height - barH) / 2
                    val paint = if ((i.toFloat() / bars) <= progress)
                        Color(0xFF339DFF) else Color.White.copy(alpha = 0.55f)
                    drawLine(
                        color = paint,
                        start = Offset(x, yTop),
                        end = Offset(x, yTop + barH),
                        strokeWidth = barW - gap,
                        cap = StrokeCap.Round
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (durationMs > 0) fmt(positionMs) + " / " + fmt(durationMs)
                    else "0:00",
                    color = if (isMine) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.weight(1f))
                Text(time, color = if (isMine) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                if (isMine) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        when (msgStatus) {
                            "read" -> "✓✓"
                            "received" -> "✓✓"
                            else -> "✓"
                        },
                        color = if (msgStatus == "read") Color(0xFF4CAF50) else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
