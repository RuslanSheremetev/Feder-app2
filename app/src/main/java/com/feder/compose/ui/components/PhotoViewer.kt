package com.feder.compose.ui.components

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// FIX_TELEGRAM_VIEWER_V1
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
            .pointerInput(Unit) {
                detectTapGestures { uiVisible = !uiVisible }
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
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
