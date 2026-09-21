package com.feder.compose.stories

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope

/**
 * StoryViewer — полноэкранный просмотрщик stories.
 *
 * Функции:
 *   - Прогресс-бар сверху (сегменты)
 *   - Autoplay видео + таймер 5с для картинок
 *   - Тап левая треть / правая треть — навигация
 *   - Long-press — пауза
 *   - Свайп вниз — закрыть
 *   - POST /view при показе story
 */
@Composable
fun StoryViewer(
    users: List<StoryApi.StoryUser>,
    startUserIndex: Int,
    myUsername: String,
    token: String,
    onClose: () -> Unit
) {
    if (users.isEmpty()) { onClose(); return }

    var userIdx by remember { mutableIntStateOf(startUserIndex.coerceIn(0, users.size - 1)) }
    var storyIdx by remember { mutableIntStateOf(0) }
    var progressMs by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(true) }
    var replyText by remember { mutableStateOf("") }
    var isReplyFocused by remember { mutableStateOf(false) }
    var replySent by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var dragX by remember { mutableFloatStateOf(0f) }

    val currentUser = users.getOrNull(userIdx)
    val currentStory = currentUser?.stories?.getOrNull(storyIdx)

    LaunchedEffect(Unit) { visible = true }

    // Отметить просмотренной при смене story
    LaunchedEffect(userIdx, storyIdx) {
        val s = users.getOrNull(userIdx)?.stories?.getOrNull(storyIdx) ?: return@LaunchedEffect
        if (!s.viewed) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                StoryApi.markViewed(s.id, myUsername, token)
            }
        }
        progressMs = 0
    }

    // Таймер для image-сторис
    LaunchedEffect(userIdx, storyIdx, paused) {
        val s = currentStory ?: return@LaunchedEffect
        if (s.mediaType == "image" && !paused) {
            val durationMs = 5000
            val stepMs = 50
            while (progressMs < durationMs) {
                delay(stepMs.toLong())
                if (paused) break
                progressMs += stepMs
            }
            if (!paused && progressMs >= durationMs) {
                // Следующая story
                val u = users.getOrNull(userIdx) ?: return@LaunchedEffect
                if (storyIdx + 1 < u.stories.size) {
                    storyIdx++
                } else if (userIdx + 1 < users.size) {
                    userIdx++
                    storyIdx = 0
                } else {
                    onClose()
                }
            }
        }
    }

    // Обновление прогресса для видео
    LaunchedEffect(userIdx, storyIdx, paused) {
        val s = currentStory ?: return@LaunchedEffect
        if (s.mediaType == "video" && !paused) {
            while (true) {
                delay(100)
                if (paused) break
                progressMs += 100
            }
        }
    }

    // Анимация drag (свайп вниз)
    val dragOffset by animateFloatAsState(targetValue = dragY, label = "drag")
    val dragXOffset by animateFloatAsState(targetValue = dragX, label = "dragX")

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(250)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.92f, animationSpec = tween(200))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = (1f - (dragOffset / 800f)).coerceIn(0.3f, 1f)))
                .offset(y = dragOffset.dp)
                .offset(x = dragXOffset.dp)
                .pointerInput(userIdx, storyIdx) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragY > 150f) onClose() else dragY = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragY = (dragY + dragAmount).coerceAtLeast(0f)
                        }
                    )
                }
                .pointerInput(userIdx, storyIdx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragX > 100f -> {
                                    // Свайп вправо — предыдущий user
                                    if (userIdx > 0) {
                                        userIdx--
                                        storyIdx = users[userIdx].stories.size - 1
                                    }
                                    dragX = 0f
                                }
                                dragX < -100f -> {
                                    // Свайп влево — следующий user
                                    if (userIdx + 1 < users.size) {
                                        userIdx++
                                        storyIdx = 0
                                    } else {
                                        onClose()
                                    }
                                    dragX = 0f
                                }
                                else -> dragX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragX += dragAmount
                        }
                    )
                }
                .pointerInput(userIdx, storyIdx) {
                    detectTapGestures(
                        onTap = { offset ->
                            val w = size.width
                            when {
                                offset.x < w / 3f -> {
                                    // Назад
                                    if (storyIdx > 0) storyIdx--
                                    else if (userIdx > 0) {
                                        userIdx--
                                        storyIdx = users[userIdx].stories.size - 1
                                    }
                                }
                                offset.x > w * 2f / 3f -> {
                                    // Вперёд
                                    val u = users.getOrNull(userIdx) ?: return@detectTapGestures
                                    if (storyIdx + 1 < u.stories.size) storyIdx++
                                    else if (userIdx + 1 < users.size) {
                                        userIdx++
                                        storyIdx = 0
                                    } else onClose()
                                }
                                else -> {
                                    // Центр — toggle mute
                                    muted = !muted
                                }
                            }
                        },
                        onLongPress = { paused = true },
                        onPress = {
                            awaitRelease()
                            paused = false
                        }
                    )
                }
        ) {
            // ─── Медиа ───
            if (currentStory != null) {
                if (currentStory.mediaType == "video") {
                    StoryVideoPlayerView(
                        videoUrl = currentStory.fullUrl,
                        isActive = !paused,
                        muted = muted,
                        onDurationKnown = { /* можем использовать для прогресса */ },
                        onCompleted = {
                            val u = users.getOrNull(userIdx) ?: return@StoryVideoPlayerView
                            if (storyIdx + 1 < u.stories.size) storyIdx++
                            else if (userIdx + 1 < users.size) {
                                userIdx++
                                storyIdx = 0
                            } else onClose()
                        },
                        onError = { msg -> Log.e("StoryViewer", "video error: $msg") }
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentStory.fullUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "story",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ─── Верхняя панель ───
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp)
                    .padding(horizontal = 12.dp)
            ) {
                // Прогресс-бар (сегменты)
                val total = currentUser?.stories?.size ?: 1
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(total) { i ->
                        val segProgress = when {
                            i < storyIdx -> 1f
                            i == storyIdx -> {
                                val s = currentUser?.stories?.getOrNull(i)
                                val totalMs = if (s?.mediaType == "video") 5000 else 5000
                                (progressMs.toFloat() / totalMs).coerceIn(0f, 1f)
                            }
                            else -> 0f
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .pointerInput(i) {
                                    detectTapGestures(
                                        onTap = {
                                            if (i < (currentUser?.stories?.size ?: 0)) {
                                                storyIdx = i
                                                progressMs = 0
                                            }
                                        }
                                    )
                                }
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(segProgress)
                                    .background(Color.White)
                            )
                        }
                    }
                }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = currentUser?.avatarUrl?.let {
                            if (it.startsWith("http")) it else "http://2.26.71.102:8010$it"
                        } ?: "http://2.26.71.102:8010/avatars/${currentUser?.username}/avatar.jpg",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            currentUser?.username ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("сейчас", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                    Icon(
                        if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        "mute",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp).padding(end = 12.dp)
                    )
                    Icon(
                        Icons.Filled.Close,
                        "close",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 4.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { onClose() })
                            }
                    )
                }
            }

                    // ─── Reply UI (текстовый ответ) ───
                    if (isReplyFocused) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.9f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.White,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                decorationBox = { inner ->
                                    Box {
                                        if (replyText.isEmpty()) {
                                            Text("Ответить...", color = Color.White.copy(alpha = 0.5f), fontSize = 15.sp)
                                        }
                                        inner()
                                    }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.Send,
                                "send",
                                tint = if (replyText.isNotBlank()) Color(0xFF2AABEE) else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .pointerInput(replyText) {
                                        detectTapGestures(
                                            onTap = {
                                                if (replyText.isNotBlank() && currentUser != null) {
                                                    val text = replyText
                                                    val toUser = currentUser.username
                                                    replyText = ""
                                                    replySent = true
                                                    isReplyFocused = false
                                                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                        try {
                                                            val url = java.net.URL("http://2.26.71.102:8004/api/chat/send")
                                                            val conn = url.openConnection() as java.net.HttpURLConnection
                                                            conn.requestMethod = "POST"
                                                            conn.setRequestProperty("Content-Type", "application/json")
                                                            conn.setRequestProperty("Authorization", "Bearer " + token)
                                                            conn.doOutput = true
                                                            val body = "{\"from\":\"" + myUsername + "\",\"to\":\"" + toUser + "\",\"text\":\"" + text + "\"}"
                                                            conn.outputStream.use { it.write(body.toByteArray()) }
                                                            val code = conn.responseCode
                                                            android.util.Log.d("StoryViewer", "Reply sent: " + code)
                                                            conn.disconnect()
                                                        } catch (e: Exception) {
                                                            android.util.Log.e("StoryViewer", "Reply error", e)
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                            )
                        }
                    }

                    // Кнопка «Ответить» (если reply не активен)
                    if (!isReplyFocused) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { isReplyFocused = true }
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ответить...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            }
                        }
                    }
        }
    }
}
