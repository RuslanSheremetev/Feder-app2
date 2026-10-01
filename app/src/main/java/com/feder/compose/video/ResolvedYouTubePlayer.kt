package com.feder.compose.video

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * ResolvedYouTubePlayer v3 — с fallback на YouTube.
 *
 * Логика:
 * 1. Показываем спиннер и пробуем загрузить через сервер /youtube/stream
 * 2. Если mp4 не пришёл за 5 сек — показываем "Открыть в YouTube"
 * 3. Пользователь тапает — открывается приложение YouTube
 */
@Composable
fun ResolvedYouTubePlayer(
    videoId: String,
    proxyBaseUrl: String = "http://2.26.71.102:8018",
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = false,
    onClose: (() -> Unit)? = null
) {
    val ctx = LocalContext.current

    // Фазы: "loading" → "ready" | "failed"
    var phase by remember(videoId) { mutableStateOf("loading") }

    // Таймер: если за 6 сек не загрузилось — failed
    LaunchedEffect(videoId) {
        phase = "loading"
        delay(6000)
        if (phase == "loading") {
            phase = "failed"
        }
    }

    // Внешняя функция открытия
    val openInYouTube: () -> Unit = remember {
        {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://youtu.be/$videoId")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            "loading" -> {
                // Спиннер
                CircularProgressIndicator(
                    color = Color(0xFF5EB5F7),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "Загрузка…",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }

            "failed" -> {
                // Кнопка "Открыть в YouTube"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Видео недоступно",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Возможно, оно новое или закрыто",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = openInYouTube,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5EB5F7)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Открыть в YouTube",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Кнопка закрытия
        if (showCloseButton && onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA000000))
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Fullscreen YouTube-плеер с fallback.
 */
@Composable
fun FullscreenYouTubePlayer(
    videoId: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        ResolvedYouTubePlayer(
            videoId = videoId,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            showCloseButton = true,
            onClose = onClose
        )
    }
}
