package com.feder.compose.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.abs

// FIX_TELEGRAM_VIEWER_V2 — Telegram-style swipe with animation
@Composable
fun PhotoViewer(
    urls: List<String>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    senderName: String = "",
    timeText: String = "",
    onEdit: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null
) {
    if (urls.isEmpty()) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialIndex) { urls.size }
    val offsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var uiVisible by remember { mutableStateOf(true) }

    // Расчёт scale и alpha от offsetY
    val currentOffset = offsetY.value
    val scale = (1f - abs(currentOffset) / 1500f).coerceIn(0.7f, 1f)
    val bgAlpha = (1f - abs(currentOffset) / 800f).coerceIn(0.2f, 1f)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha))
            // Свайп вверх/вниз — тянем фото за пальцем
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        scope.launch {
                            if (abs(offsetY.value) > 200f) {
                                // Закрыть: улетает за экран
                                val target = if (offsetY.value > 0) 2000f else -2000f
                                offsetY.animateTo(target, tween(durationMillis = 220))
                                onClose()
                            } else {
                                // Возврат на место
                                offsetY.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        scope.launch {
                            offsetY.animateTo(0f, spring())
                        }
                    }
                ) { _, dragAmount ->
                    scope.launch {
                        offsetY.snapTo(offsetY.value + dragAmount)
                    }
                }
            }
            // Тап → скрыть/показать UI
            .pointerInput(Unit) {
                detectTapGestures { uiVisible = !uiVisible }
            }
    ) {
        // Контент с трансформацией
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = offsetY.value
                    scaleX = scale
                    scaleY = scale
                    alpha = (1f - abs(offsetY.value) / 1200f).coerceIn(0.4f, 1f)
                }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp,
                userScrollEnabled = !isDragging   // отключить пока тащат вертикально
            ) { page ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(urls[page])
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ─── Верхняя панель ───
        if (uiVisible) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xCC000000), Color(0x00000000))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(Modifier.weight(1f).padding(start = 4.dp)) {
                    if (senderName.isNotEmpty()) {
                        Text(
                            senderName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                    if (timeText.isNotEmpty()) {
                        Text(
                            timeText,
                            color = Color(0xFFC0C7D4),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = { onEdit?.invoke() }) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "edit",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { onShare?.invoke() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "share",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { onMore?.invoke() }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "more",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ─── Счётчик N of M ───
        if (uiVisible) {
            Text(
                "${pagerState.currentPage + 1} of ${urls.size}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .background(Color(0x99000000), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
