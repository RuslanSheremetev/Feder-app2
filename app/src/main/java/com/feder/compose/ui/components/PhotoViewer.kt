package com.feder.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// FIX_PHOTOVIEWER_V2 — Telegram-style viewer
@Composable
fun PhotoViewer(
    urls: List<String>,
    initialIndex: Int = 0,
    senderName: String = "",
    timeText: String = "",
    onClose: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onForward: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null
) {
    if (urls.isEmpty()) return
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex) { urls.size }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var uiVisible by remember { mutableStateOf(true) }

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
                ) { _, dragAmount -> dragOffset += dragAmount }
            }
    ) {
        // ─── Pager ──────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
        ) { page ->
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { uiVisible = !uiVisible }
                    },
                contentAlignment = Alignment.Center
            ) {
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

        // ─── Top bar (скрывается по тапу) ──────────────
        if (uiVisible) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xDD000000), Color(0x00000000))
                        )
                    )
                    .padding(top = 32.dp, bottom = 24.dp, start = 4.dp, end = 4.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ← назад
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Имя + дата (с "‹" как в Telegram для перехода к чату)
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.ChevronLeft,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                senderName.ifEmpty { "Photo" },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        if (timeText.isNotEmpty()) {
                            Text(
                                timeText,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        }
                    }
                    // ✏️ edit
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "edit",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    // 📤 forward
                    if (onForward != null) {
                        IconButton(onClick = onForward) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "forward",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    // ⋮ more
                    if (onMore != null) {
                        IconButton(onClick = onMore) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "more",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // ─── Счётчик N of M снизу ────────────────────────
        if (uiVisible && urls.size > 1) {
            Text(
                "${pagerState.currentPage + 1} of ${urls.size}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color(0xB3000000), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
