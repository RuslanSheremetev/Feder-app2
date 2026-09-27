package com.feder.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Полноэкранный просмотрщик фото со свайпом.
 *
 * @param urls — список URL (с token, если надо)
 * @param initialIndex — с какого начинать
 * @param onClose — закрыть
 */
@Composable
fun PhotoViewer(
    urls: List<String>,
    initialIndex: Int = 0,
    onClose: () -> Unit
) {
    if (urls.isEmpty()) return

    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex) { urls.size }

    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 150f) onClose()
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount
                }
            }
    ) {
        // Pager со свайпом влево/вправо
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
        ) { page ->
            val fullUrl = urls[page]
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(fullUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Кнопка закрыть (справа сверху)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
                .size(40.dp)
                .background(Color(0x80000000), CircleShape)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Закрыть",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Счётчик страниц (снизу по центру)
        if (urls.size > 1) {
            Text(
                "${pagerState.currentPage + 1} / ${urls.size}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .background(Color(0x80000000), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
