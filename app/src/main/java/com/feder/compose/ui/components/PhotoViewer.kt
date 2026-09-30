package com.feder.compose.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
// FIX_HERO_STEP3_V3: импорты для coroutineScope + launch + delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
    onMore: (() -> Unit)? = null,
    // FIX_HERO_STEP2: координаты миниатюры в окне для hero-анимации
    sourceX: Float = 0f,
    sourceY: Float = 0f,
    sourceWidth: Float = 100f,
    sourceHeight: Float = 100f
) {
    if (urls.isEmpty()) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialIndex) { urls.size }
    val offsetY = remember { Animatable(0f) }
    // FIX_HERO_STEP2: offsetX тоже анимируем для полёта в миниатюру
    val offsetX = remember { Animatable(0f) }
    // FIX_HERO_STEP3_V3: scaleAnim + alphaAnim
    val scaleAnim = remember { Animatable(1f) }
    val alphaAnim = remember { Animatable(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var uiVisible by remember { mutableStateOf(true) }
    // FIX_HERO_STEP2: получаем размеры экрана для hero-анимации
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val screenW = config.screenWidthDp.toFloat() * context.resources.displayMetrics.density
    val screenH = config.screenHeightDp.toFloat() * context.resources.displayMetrics.density

    // FIX_HERO_STEP3_V3: dragDistance от максимума |X|,|Y|
    val dragDistance = maxOf(abs(offsetX.value), abs(offsetY.value))
    val baseScale = (1f - dragDistance / 1500f).coerceIn(0.3f, 1f)
    // FIX_SMOOTH_HERO_V1: bgAlpha плавнее (медленнее пропадает)
    val bgAlpha = (1f - dragDistance / 900f).coerceIn(0f, 1f)

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
                                // FIX_HERO_STEP3_V3: полный hero-полёт
                                val targetX = sourceX + sourceWidth / 2f - screenW / 2f
                                val targetY = sourceY + sourceHeight / 2f - screenH / 2f
                                val targetScale = maxOf(
                                    sourceWidth / screenW,
                                    sourceHeight / screenH
                                ).coerceIn(0.05f, 1f)

                                // FIX_SMOOTH_HERO_V1: плавный hero-полёт с easing
                                val dur = 420
                                val curve = tween<androidx.compose.ui.geometry.Offset>(durationMillis = dur, easing = FastOutSlowInEasing)
                                launch { offsetX.animateTo(targetX, tween(durationMillis = dur, easing = FastOutSlowInEasing)) }
                                launch { offsetY.animateTo(targetY, tween(durationMillis = dur, easing = FastOutSlowInEasing)) }
                                launch { scaleAnim.animateTo(targetScale, tween(durationMillis = dur, easing = FastOutSlowInEasing)) }
                                // Alpha чуть позже — фото сначала летит, потом растворяется
                                launch {
                                    kotlinx.coroutines.delay(140)
                                    alphaAnim.animateTo(0f, tween(durationMillis = dur - 140, easing = FastOutSlowInEasing))
                                }
                                // onClose чуть позже завершения анимации
                                kotlinx.coroutines.delay(dur + 60L)
                                onClose()
                            } else {
                                scope.launch {
                                    offsetY.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = 0.85f,   // FIX_SMOOTH_HERO_V1: мягче, без отскока
                                            stiffness = 380f
                                        )
                                    )
                                }
                                scope.launch { offsetX.animateTo(0f, spring()) }
                                scope.launch { scaleAnim.animateTo(1f, spring()) }
                                scope.launch { alphaAnim.animateTo(1f, spring()) }
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        val softSpring = spring<Float>(dampingRatio = 0.85f, stiffness = 380f)
                        scope.launch { offsetY.animateTo(0f, softSpring) }
                        scope.launch { offsetX.animateTo(0f, softSpring) }
                        scope.launch { scaleAnim.animateTo(1f, softSpring) }
                        scope.launch { alphaAnim.animateTo(1f, softSpring) }
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
                    translationX = offsetX.value
                    translationY = offsetY.value
                    // FIX_HERO_STEP3_V3: общий scale = base × animated
                    val totalScale = baseScale * scaleAnim.value
                    scaleX = totalScale
                    scaleY = totalScale
                    alpha = alphaAnim.value * (1f - dragDistance / 1500f).coerceIn(0.15f, 1f)
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
        if (uiVisible && !isDragging) {
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
        if (uiVisible && !isDragging) {
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
