package com.feder.compose.ui.screen

import com.feder.compose.ChatItem
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.withTimeout
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import android.provider.MediaStore
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.window.Popup
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.feder.compose.video.VideoBubble
import com.feder.compose.video.FullscreenVideoPlayer
import coil.request.ImageRequest
import coil.imageLoader
import com.feder.compose.ProWebSocket
import com.feder.compose.PhotoUploader
import com.feder.compose.ui.theme.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit



// ══════════════════════════════════════════════════════════════════
// Remote logger → FederHttpClient.sendLog() → /api/logs → ws_logs
// ══════════════════════════════════════════════════════════════════
private val rlogClient by lazy { com.feder.compose.FederHttpClient() }

fun rlog(tag: String, message: String) {
    android.util.Log.d(tag, message)
    Thread {
        try {
            rlogClient.sendLog("[$tag] $message")
        } catch (_: Exception) { }
    }.start()
}



private val loggerClient: okhttp3.OkHttpClient by lazy {
    okhttp3.OkHttpClient.Builder().build()
}



var fullScreenPhoto: String? = null
var uploadingPhotos: Boolean = false

data class Reaction(
    val emoji: String = "",
    val count: Int = 0,
    val users: List<String> = emptyList(),
    val me: Boolean = false
)

data class MsgItem(
    @com.google.gson.annotations.SerializedName("from_user")
    val from: String = "unknown",
    val to: String = "unknown",
    val text: String,
    val time: String = "",
    var status: String = "sent",
    val timeVal: Long = 0L,
    val id: Long = 0L,
    var posX: Float = 0f,
    var posY: Float = 0f,
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val replyToStoryId: Int = 0,
    val replyToStoryFilename: String? = null,
    val replyToStoryAuthor: String? = null,
    val reactions: List<Reaction> = emptyList(),
)

@Composable
fun AttachOption(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
                .background(if (selected) PrimaryContainer else SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = if (selected) OnPrimaryContainer else OnSurfaceVariant, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = if (selected) Primary else OnSurfaceVariant, fontSize = 12.sp)
    }
}


@Composable
fun MenuAction(icon: androidx.compose.ui.graphics.vector.ImageVector?, text: String, textColor: Color = OnSurface, indent: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp).padding(start = if (indent) 24.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, text, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(text, color = textColor, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(msg: MsgItem, text: String, time: String, isMine: Boolean, token: String = "", position: Int = 3, onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null, onPositioned: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null, selectionMode: Boolean = false, selectedMessages: Set<String> = emptySet(), allChats: List<ChatItem> = emptyList(), myUsername: String = "demo") {
    val topRadius = when (position) { 0 -> 20.dp; 1 -> 4.dp; 2 -> 4.dp; else -> 20.dp }
    val bottomRadius = when (position) { 0 -> 4.dp; 1 -> 4.dp; 2 -> 20.dp; else -> 20.dp }
    val vertPad = when (position) { 0 -> 8.dp; 1 -> 1.dp; 2 -> 1.dp; else -> 8.dp }
    val ts = if (isMine) 20.dp else topRadius
    val te = if (isMine) topRadius else 20.dp
    val bs = if (isMine) 20.dp else bottomRadius
    val be = if (isMine) bottomRadius else 20.dp
    Box(Modifier.fillMaxWidth().padding(top = vertPad).onGloballyPositioned { coords -> onPositioned?.invoke(coords.positionInRoot()) }, contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart) {
        // Получаем Vibrator ОДИН раз вне лямбды
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
        // Динамический размер фото через Coil painter
        val density = androidx.compose.ui.platform.LocalDensity.current
        val firstUrl = msg.imageUrls.firstOrNull() ?: msg.imageUrl
        val fullUrl = if (firstUrl != null) {
            if (firstUrl.startsWith("content://") || firstUrl.startsWith("file://")) firstUrl
            else if (firstUrl.contains("?")) firstUrl
            else if (firstUrl.startsWith("http")) "$firstUrl?token=$token"
            else "http://2.26.71.102:8012/uploads/$firstUrl?token=$token"
        } else null
        val measurePainter = if (fullUrl != null)
            coil.compose.rememberAsyncImagePainter(model = fullUrl) else null
        val painterState = measurePainter?.state
        rlog("PhotoDebug", "id=${msg.id} state=${painterState?.javaClass?.simpleName} fullUrl=${fullUrl?.take(80)}")
        val painterSuccess = painterState as? coil.compose.AsyncImagePainter.State.Success
        val computedSize = remember(painterSuccess, msg.id) {
            val drawable = painterSuccess?.result?.drawable
            if (drawable != null) {
                val w = drawable.intrinsicWidth.toFloat()
                val h = drawable.intrinsicHeight.toFloat()
                if (w > 0 && h > 0) {
                    val ratio = w / h
                    val maxW = 280f
                    val maxH = 720f
                    var newW: Float
                    var newH: Float
                    when {
                        ratio >= 2.0f -> { newW = maxW; newH = maxW / ratio }
                        ratio >= 1.0f -> { newW = maxW * 0.95f; newH = newW / ratio }
                        ratio >= 0.8f -> { newW = 260f; newH = 260f }
                        ratio >= 0.5f -> { newW = 240f; newH = 240f / ratio }
                        else -> { newW = 220f; newH = 220f / ratio }
                    }
                    if (newH > maxH) { newH = maxH; newW = maxH * ratio }
                    rlog("PhotoSize", "id=${msg.id} src=${w.toInt()}x${h.toInt()} ratio=$ratio new=${newW}x${newH}")
                    newW to newH
                } else 200f to 200f
            } else 200f to 200f
        }
        val photoWidth = computedSize.first.dp
        val photoHeight = computedSize.second.dp
        var imageAspectRatio by remember { mutableStateOf<Float?>(null) }
        Surface(Modifier.then(if (msg.imageUrls.isNotEmpty() || msg.imageUrl != null) Modifier.width(280.dp) else Modifier.width(androidx.compose.foundation.layout.IntrinsicSize.Max).widthIn(max = 280.dp)).then(if (onClick != null) Modifier.combinedClickable(
            onClick = onClick ?: {},
            onLongClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onLongClick?.invoke()
            }
        ) else Modifier), shape = RoundedCornerShape(ts, te, be, bs), color = if (isMine) PrimaryContainer else SecondaryContainer) {
            Column(Modifier.padding(if (msg.imageUrls.isNotEmpty() || msg.imageUrl != null) 2.dp else 1.dp)) {
                        // ═══ Reply to story preview ═══
                        if (msg.replyToStoryId > 0 && !msg.replyToStoryFilename.isNullOrEmpty()) {
                            Row(
                                Modifier
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMine) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.12f))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                coil.compose.AsyncImage(
                                    model = "http://2.26.71.102:8020/stories/${msg.replyToStoryFilename}",
                                    contentDescription = "story",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                androidx.compose.material3.Text(
                                    "Ответ на story",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                androidx.compose.material3.Text(
                                    msg.replyToStoryAuthor ?: "",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        }
                rlog("PhotoDebug", "BUBBLE_START id=${msg.id} hasUrls=${msg.imageUrls != null} size=${msg.imageUrls.size} imageUrl=${msg.imageUrl}")
                if (msg.imageUrls != null && msg.imageUrls.isNotEmpty()) {
                    val firstUrl = msg.imageUrls.first()
                    val isAudio = com.feder.compose.audio.IsAudio.isAudioFile(firstUrl)
                    rlog("PhotoDisplay", "Rendering ${if (isAudio) "audio" else "photo"}: $firstUrl, count=${msg.imageUrls.size}")
                    val isVideo = com.feder.compose.audio.IsVideo.isVideoFile(firstUrl)
                    if (isAudio) {
                        // ═══ AUDIO BUBBLE ═══
                        com.feder.compose.audio.AudioBubble(
                            audioUrl = firstUrl,
                            token = token,
                            isMine = isMine,
                            time = time,
                            msgStatus = msg.status,
                            onLongClick = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onClick?.invoke() }
                        )
                    } else if (isVideo) {
                        VideoBubble(
                            videoUrl = firstUrl,
                            token = token,
                            isMine = isMine,
                            time = time,
                            msgStatus = msg.status,
                            thumbUrl = firstUrl.substringBeforeLast(".").replace("videos/", "video_thumbs/") + ".jpg",
                            isVisible = true,
                            onLongPress = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onClick?.invoke() },
                            onOpenFullscreen = { onClick?.invoke() }
                        )
                    } else {
                    Column(Modifier.fillMaxWidth()) {
                        msg.imageUrls.forEachIndexed { index, url ->
                            Box {
                                if (uploadingPhotos && msg.status == "pending") {
                                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                    }
                                }
                                AsyncImage(
                                    imageLoader = LocalContext.current.imageLoader,
                                    placeholder = null,  // показываем только когда загрузилось
                                    model = ImageRequest.Builder(LocalContext.current).data(
                                        if (url.startsWith("content://") || url.startsWith("file://")) url
                                        else if (url.contains("?")) url 
                                        else if (url.startsWith("http")) "$url?token=$token" 
                                        else "http://2.26.71.102:8012/uploads/$url?token=$token"
                                    )
                                        .size(360, 480)
                                        .crossfade(true)
                                        .diskCacheKey(url)
                                        .memoryCacheKey(url)
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "photo",
                                    modifier = Modifier
                                        .sizeIn(maxWidth = 280.dp, maxHeight = 600.dp)
                                        .aspectRatio(if (imageAspectRatio != null && imageAspectRatio!! > 0.05f) imageAspectRatio!! else 0.75f)
                                        .combinedClickable(
                                            onClick = {
                                                // короткий тап на фото → fullscreen
                                                fullScreenPhoto = if (url.startsWith("content://") || url.startsWith("file://")) url else if (url.contains("?")) url else if (url.startsWith("http")) "$url?token=$token" else "http://2.26.71.102:8012/uploads/$url?token=$token"
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                onClick?.invoke()
                                            }
                                        )
                                        .then(if (msg.imageUrls.size > 1) Modifier.aspectRatio(1f) else Modifier)
                                        .clip(RoundedCornerShape(ts, te, be, bs))
                                        .border(0.5.dp, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f), RoundedCornerShape(if (index == 0) 16.dp else 8.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                                if (index == msg.imageUrls.lastIndex && msg.reactions.isEmpty()) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceContainerHigh
                                    ) {
                                        Row(Modifier.padding(horizontal = 5.dp, vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                            Text(time, color = Color.White, fontSize = 11.sp, maxLines = 1, softWrap = false)
                                            if (isMine) {
                                                Spacer(Modifier.width(1.dp))
                                                val checkText = when (msg.status) {
                                                    "read" -> "✓✓"
                                                    "received" -> "✓✓"
                                                    else -> "✓"
                                                }
                                                val checkColor = when (msg.status) {
                                                    "read" -> Color(0xFF4CAF50)
                                                    else -> Color.White.copy(alpha = 0.7f)
                                                }
                                                DrawCheck(double = checkText.contains("✓✓"), tint = checkColor, size = 12.dp, modifier = Modifier.alignByBaseline().offset(y = 4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            if (index < (msg.imageUrls.lastIndex ?: 0)) Spacer(Modifier.height(2.dp))
                        }
                        // Подпись (текст) под фото
                        if (msg.text.isNotEmpty()) {
                            Text(
                                msg.text,
                                color = if (isMine) OnPrimaryContainer else OnSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    }  // end else (photo)
                } else if (msg.imageUrl != null) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(if (msg.imageUrl?.startsWith("content://") == true || msg.imageUrl?.startsWith("file://") == true) msg.imageUrl else if (msg.imageUrl?.startsWith("http") == true) "${msg.imageUrl}?token=$token" else "http://2.26.71.102:8012/uploads/${msg.imageUrl}?token=$token")
                            .size(360, 480)
                            .crossfade(true)
                            .diskCacheKey((msg.imageUrl ?: "").substringBefore("?"))
                            .memoryCacheKey((msg.imageUrl ?: "").substringBefore("?"))
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                            contentDescription = "photo",
                            modifier = Modifier.width(photoWidth).height(photoHeight).clip(RoundedCornerShape(ts, te, be, bs)).border(0.1.dp, OutlineVariant.copy(alpha = 0.04f), RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (time.isNotEmpty() && msg.reactions.isEmpty()) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceContainerHigh
                            ) {
                                Row(Modifier.padding(horizontal = 5.dp, vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                    Text(time, color = Color.White, fontSize = 11.sp, maxLines = 1, softWrap = false)
                                    if (isMine) {
                                        Spacer(Modifier.width(1.dp))
                                        val checkText = when (msg.status) {
                                            "read" -> "✓✓"
                                            "received" -> "✓✓"
                                            else -> "✓"
                                        }
                                        val checkColor = when (msg.status) {
                                            "read" -> Color(0xFF4CAF50)
                                            else -> Color.White.copy(alpha = 0.7f)
                                        }
                                        DrawCheck(double = checkText.contains("✓✓"), tint = checkColor, size = 12.dp, modifier = Modifier.alignByBaseline().offset(y = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            if (msg.imageUrls.isEmpty() && msg.imageUrl == null) {
                Row(Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.Top) {
                    Text(text, color = if (isMine) Color.White else OnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false).alignByBaseline())
                    if (time.isNotEmpty() && msg.reactions.isEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(time, color = if (isMine) Color.White.copy(alpha = 0.7f) else OnSurfaceVariant, fontSize = 10.sp, maxLines = 1, softWrap = false, modifier = Modifier.alignByBaseline().offset(y = 4.dp))
                        if (isMine) {
                            Spacer(Modifier.width(2.dp))
                            val checkText = when (msg.status) {
                                "pending" -> "✓"
                                "sent" -> "✓"
                                "received" -> "✓✓"
                                "read" -> "✓✓"
                                else -> "✓"
                            }
                            val checkColor = when (msg.status) {
                                "read" -> Color(0xFF4CAF50)
                                else -> Color.White.copy(alpha = 0.7f)
                            }
                            DrawCheck(double = checkText.contains("✓✓"), tint = checkColor, size = 12.dp, modifier = Modifier.alignByBaseline().offset(y = 4.dp))
                        }
                    }
                }
            }
            if (msg.reactions.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    msg.reactions.forEach { r ->
                        ReactionPill(r = r, allChats = allChats, myUsername = myUsername, onClick = { }, onLongClick = { })
                        Spacer(Modifier.width(4.dp))
                    }
                    // Если фото-сообщение — время справа в этом же ряду
                    if (time.isNotEmpty()) {
                        Spacer(Modifier.weight(1f))
                        Text(time, color = if (isMine) Color.White.copy(alpha = 0.7f) else OnSurfaceVariant, fontSize = 10.sp, maxLines = 1, softWrap = false)
                        if (isMine) {
                            Spacer(Modifier.width(2.dp))
                            val checkText = when (msg.status) {
                                "pending" -> "✓"
                                "sent" -> "✓"
                                "received" -> "✓✓"
                                "read" -> "✓✓"
                                else -> "✓"
                            }
                            val checkColor = when (msg.status) {
                                "read" -> Color(0xFF4CAF50)
                                else -> if (isMine) OnPrimaryContainer.copy(alpha = 0.6f) else OnSurfaceVariant
                            }
                            DrawCheck(double = checkText.contains("✓✓"), tint = checkColor, size = 12.dp, modifier = Modifier.alignByBaseline().offset(y = 4.dp))
                        }
                    }
                }
            }
            }
            }
        }
}
@Composable
fun MiniAvatar(url: String?, username: String) {
    val bg = remember(username) {
        val palette = listOf(
            Color(0xFF339DFF), Color(0xFFE17076),
            Color(0xFF7BC862), Color(0xFFE5CA77),
            Color(0xFFA695E7), Color(0xFFEE7AAE)
        )
        palette[kotlin.math.abs(username.hashCode()) % palette.size]
    }
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, SecondaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank()) {
            Text(
                text = username.take(1).uppercase(),
                fontSize = 9.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (url.startsWith("/")) "http://2.26.71.102:8010$url" else url)
                    .crossfade(true)
                    .build(),
                contentDescription = username,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MenuRow(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color(0xFFD1D5DB), lineHeight = 17.sp)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, token: String, avatarUrl: String? = null, lastSeen: Long = 0, isOnline: Boolean = false, allChats: List<ChatItem> = emptyList(), wsManager: ProWebSocket? = null, repository: com.feder.compose.repository.ChatRepository? = null, onBack: () -> Unit, onProfileClick: () -> Unit = {}, onMessageSent: ((String, String) -> Unit)? = null, reactionUpdates: kotlinx.coroutines.flow.SharedFlow<Pair<Long, String>>? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<MsgItem>>(emptyList()) }

    // ═══ Запись голосовых сообщений ═══
    val audioRecorder = remember { com.feder.compose.AudioRecorder(context.applicationContext) }
    var isRecording by remember { mutableStateOf(false) }
    val isRecordingState = rememberUpdatedState(isRecording)
    var recordLocked by remember { mutableStateOf(false) }
    var recordTimeSec by remember { mutableStateOf(0) }
    var recordAmplitude by remember { mutableStateOf(0f) }
    var recordOffsetX by remember { mutableStateOf(0f) }
    var recordReleasedBeforeStart by remember { mutableStateOf(false) }
    val recordPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        android.util.Log.d("ChatScreen", "PERMISSION callback granted=$granted")
        if (granted) {
            val f = audioRecorder.start()
            android.util.Log.d("ChatScreen", "AUDIO start file=${f?.absolutePath}")
            if (f != null) {
                if (recordReleasedBeforeStart) {
                    audioRecorder.cancel()
                    recordReleasedBeforeStart = false
                    android.util.Log.d("ChatScreen", "Record start but user already released — cancel")
                } else {
                    isRecording = true
                    recordTimeSec = 0
                    recordOffsetX = 0f
                }
            }
        }
    }
    // Таймер + амплитуда
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                kotlinx.coroutines.delay(100L)
                recordTimeSec = audioRecorder.durationSec
                recordAmplitude = (audioRecorder.getAmplitude().toFloat() / 32767f).coerceIn(0f, 1f)
            }
        }
    }
    // Анимация пульсации и свечения
    val audioPulse = rememberInfiniteTransition()
    val audioPulseScale by audioPulse.animateFloat(
        initialValue = 1f, targetValue = if (isRecording) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse)
    )
    val audioGlowAlpha by audioPulse.animateFloat(
        initialValue = 0.25f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse)
    )
    // ═══ /Запись голосовых ═══

    // === Приём реакций из WS через ViewModel (SharedFlow) ===
    if (reactionUpdates != null) {
        LaunchedEffect(reactionUpdates) {
            reactionUpdates.collect { pair ->
                val (mid, rxStr) = pair
                android.util.Log.d("ChatScreen", "REACTION_UPDATE: mid=$mid rx=$rxStr")
                val newList = try {
                    val arr = org.json.JSONArray(rxStr)
                    val list = mutableListOf<Reaction>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val users = mutableListOf<String>()
                        val ua = o.optJSONArray("users")
                        if (ua != null) for (j in 0 until ua.length()) users.add(ua.getString(j))
                        list.add(Reaction(
                            emoji = o.optString("emoji", ""),
                            count = o.optInt("count", 0),
                            users = users,
                            me = users.contains(myUsername)
                        ))
                    }
                    list
                } catch (e: Exception) { emptyList() }
                messages = messages.map { m -> if (m.id == mid) m.copy(reactions = newList) else m }
            }
        }
    }
    var inputText by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<MsgItem?>(null) }
    var selectedMessageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var clickedMsgOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var editMessage by remember { mutableStateOf<MsgItem?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedMessages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf<MsgItem?>(null) }
    var expandInput by remember { mutableStateOf(false) }
    var forwardContacts by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var showForward by remember { mutableStateOf(false) }
    var showDeleteSub by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var preloading by remember { mutableStateOf(false) }
    var isFirstNewMessage by remember { mutableStateOf(true) }
    // token passed from MainActivity
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = Int.MAX_VALUE)
    val gson = remember { Gson() }
    var wsStatus by remember { mutableStateOf("") }
    val ws = wsManager ?: remember(token) { ProWebSocket() }
    LaunchedEffect(token, ws) {
        if (token.isNotEmpty() && wsManager == null) { ws.connect(myUsername, token) }
    }
    android.util.Log.d("WS_CHAT", "ws=$ws wsManager=$wsManager")
    val httpClient = remember { OkHttpClient() }
    
    fun logToDb(message: String) {
        try {
            val logJson = gson.toJson(mapOf("log" to message))
            val logBody = logJson.toRequestBody("application/json".toMediaType())
            httpClient.newCall(Request.Builder().url("http://2.26.71.102:8004/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
            })
        } catch (_: Exception) {}
    }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var emojiExpanded by remember { mutableStateOf(false) }
    var attachExpanded by remember { mutableStateOf(false) }
    var selectedPhotos by remember { mutableStateOf<Set<android.net.Uri>>(emptySet()) }
    var isSending by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotos = setOf(uri)
            // Меню остаётся открытым до нажатия Send

        }
    }
    var forwardSearch by remember { mutableStateOf("") }
    var forwardSelected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var forwardMessage by remember { mutableStateOf("") }

        LaunchedEffect(selectedMessage) {
        selectedMessage?.let {
            clickedMsgOffset = selectedMessageOffset
            android.util.Log.d("ChatScreen", "OFFSET: x=${selectedMessageOffset.x.toInt()} y=${selectedMessageOffset.y.toInt()}")
        }
    }

    val msgPositions = remember { mutableMapOf<Long, androidx.compose.ui.geometry.Offset>() }

    val dateInHeader = remember { mutableStateOf("") }
    
    // Функция для получения даты из timeVal
    fun formatLastSeen(timestamp: Long): String {
        val now = System.currentTimeMillis() / 1000
        val diff = now - timestamp
        return when {
            diff < 60 -> "just now"
            diff < 3600 -> "${diff / 60} min ago"
            diff < 86400 -> "${diff / 3600} h ago"
            diff < 172800 -> "yesterday"
            diff < 604800 -> "${diff / 86400} d ago"
            diff < 2592000 -> "${diff / 604800} wk ago"
            else -> "long ago"
        }
    }

    fun formatHeaderDate(timeVal: Long): String {
        if (timeVal == 0L) return ""
        val msgDate = java.util.Date(timeVal * 1000)
        val today = java.util.Calendar.getInstance()
        val msgCal = java.util.Calendar.getInstance().apply { time = msgDate }
        val sdf = SimpleDateFormat("d MMMM", java.util.Locale("en"))
        val sdfYear = SimpleDateFormat("d MMMM yyyy", java.util.Locale("en"))
        return when {
            today.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR) &&
            today.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) -> "Today"
            today.get(java.util.Calendar.DAY_OF_YEAR) - 1 == msgCal.get(java.util.Calendar.DAY_OF_YEAR) &&
            today.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) -> "Yesterday"
            today.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) -> sdf.format(msgDate)
            else -> sdfYear.format(msgDate)
        }
    }

    var internalToken = token

    fun saveMsgPosition(msg: MsgItem) {
        if (msg.id > 0 && msg.posY > 0f) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val json = """{"id":${msg.id},"x":${msg.posX},"y":${msg.posY}}"""
                    val body = json.toRequestBody("application/json".toMediaType())
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8004/api/message_pos").header("Authorization", "Bearer ${internalToken.ifEmpty { token }}").post(body).build()).execute().close()
                } catch (_: Exception) {}
            }
        }
    }
    LaunchedEffect(chatUsername) {
        rlog("ChatScreen", "OPEN chat=$chatUsername me=$myUsername")
        repository?.markRead(chatUsername)

        withContext(Dispatchers.IO) {
            var loadedFromRoom = false

            repository?.let { repo ->
                val cachedMessages = repo.getMessages(myUsername, chatUsername)
                rlog("ChatScreen", "ROOM read: ${cachedMessages.size} msgs")
                if (cachedMessages.isNotEmpty()) {
                    loadedFromRoom = true
                    val list = cachedMessages.map { entity ->
                        MsgItem(
                            imageUrls = entity.imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                            from = entity.fromUser ?: "unknown",
                            to = entity.toUser ?: "unknown",
                            text = entity.text ?: "",
                            time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(entity.timeVal * 1000)),
                            status = if (entity.isRead) "read" else "sent",
                            timeVal = entity.timeVal,
                            id = entity.id,
                            posX = entity.posX ?: 0f,
                            posY = entity.posY ?: 0f
                        )
                    }
                    withContext(Dispatchers.Main) {
                        messages = list
                    }
                    rlog("ChatScreen", "ROOM applied: ${list.size} msgs")
                }
            }

            // ВСЕГДА грузим с API, даже если есть Room-кэш
            run {
                try {
                    // ВСЕГДА логинимся заново — токен из MainActivity может быть просрочен
                    try {
                        val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
                        val authBody = authJson.toRequestBody("application/json".toMediaType())
                        val authResp = httpClient.newCall(Request.Builder()
                            .url("http://2.26.71.102:8004/api/login").post(authBody).build()).execute()
                        val freshToken = JsonParser.parseString(authResp.body?.string() ?: "")
                            .asJsonObject.get("access_token")?.asString ?: ""
                        if (freshToken.isNotEmpty()) {
                            internalToken = freshToken
                            rlog("ChatScreen", "RELOGIN ok token_len=${freshToken.length}")
                        } else {
                            rlog("ChatScreen", "RELOGIN empty token")
                        }
                    } catch (e: Exception) {
                        rlog("ChatScreen", "RELOGIN fail: ${e.message}")
                    }
                    rlog("ChatScreen", "API request /api/messages/$chatUsername")
                    val msgResp = httpClient.newCall(Request.Builder()
                        .url("http://2.26.71.102:8004/api/messages/$chatUsername")
                        .header("Authorization", "Bearer ${internalToken.ifEmpty { token }}").build()).execute()
                    val body = msgResp.body?.string() ?: "[]"
                    rlog("ChatScreen", "API response code=${msgResp.code} body_len=${body.length}")
                    val type = object : com.google.gson.reflect.TypeToken<List<MsgItem>>() {}.type
                    val loaded = gson.fromJson<List<MsgItem>>(body, type)
                    rlog("ChatScreen", "JSON parsed: ${loaded.size} msgs")
                    val withPhotosCount = loaded.count { it.imageUrls.isNotEmpty() || it.imageUrl != null }
                    rlog("ChatScreen", "JSON_WITH_PHOTOS: $withPhotosCount / ${loaded.size}")
                    loaded.filter { it.imageUrls.isNotEmpty() }.take(3).forEach { m ->
                        rlog("ChatScreen", "JSON_PHOTO id=${m.id} urls=${m.imageUrls}")
                    }

                    rlog("ChatScreen", "BEFORE_MAP loaded.size=${loaded.size}")
                    rlog("ChatScreen", "BEFORE_MAP loaded.size=${loaded.size}")
                    val apiList = try {
                        loaded.reversed().mapIndexed { idx, msg ->
                            try {
                                val urls = msg.imageUrls ?: emptyList()
                                val timeStr = try {
                                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(msg.time.toLong() * 1000))
                                } catch (e: Exception) { msg.time ?: "" }
                                msg.copy(
                                    status = msg.status?.ifEmpty { "sent" } ?: "sent",
                                    imageUrls = urls,
                                    text = msg.text ?: "",
                                    timeVal = try { msg.time.toLong() } catch (e: Exception) { 0L },
                                    time = timeStr,
                                    reactions = msg.reactions ?: emptyList(),
                                            replyToStoryId = msg.replyToStoryId,
                                            replyToStoryFilename = msg.replyToStoryFilename,
                                            replyToStoryAuthor = msg.replyToStoryAuthor,
                                )
                            } catch (e: Exception) {
                                rlog("ChatScreen", "MAP_ITEM_FAIL idx=$idx id=${msg.id} err=${e.message}")
                                MsgItem(
                                    id = msg.id,
                                    from = msg.from ?: "unknown",
                                    to = msg.to ?: "unknown",
                                    text = msg.text ?: "",
                                    timeVal = 0L,
                                    time = "",
                                    status = "sent",
                                    imageUrls = emptyList(),
                                    posX = 0f,
                                    posY = 0f
                                )
                            }
                        }
                    } catch (e: Exception) {
                        rlog("ChatScreen", "MAP_FAIL: ${e.message}")
                        emptyList()
                    }
                    rlog("ChatScreen", "AFTER_MAP apiList.size=${apiList.size}")
                    val apiWithPhotos = apiList.count { it.imageUrls.isNotEmpty() }
                    rlog("ChatScreen", "AFTER_MAP_PHOTOS: $apiWithPhotos / ${apiList.size}")
                    // Мержим: Room-кэш + API, убираем дубли по id
                    // withContext убран — LaunchedEffect уже на Main
                    val mergedMap = LinkedHashMap<Long, MsgItem>()
                    messages.forEach { mergedMap[it.id] = it }
                    apiList.forEach { mergedMap[it.id] = it }
                    val merged = mergedMap.values.sortedBy { it.timeVal }
                    rlog("ChatScreen", "MERGE done: total=${merged.size} (room+api)")
                    rlog("ChatScreen", "BEFORE_ASSIGN merged.size=${merged.size}")
                    messages = merged
                    rlog("ChatScreen", "AFTER_ASSIGN messages.size=${messages.size}")

                    repository?.let { r ->
                        r.saveMessages(apiList.map { m ->
                            com.feder.compose.data.entity.MessageEntity(
                                id = m.id,
                                fromUser = m.from,
                                toUser = m.to,
                                text = m.text,
                                timeVal = m.timeVal,
                                imageUrls = m.imageUrls.joinToString(","),
                                isRead = m.status == "read"
                            )
                        })
                    }

                    httpClient.newCall(Request.Builder()
                        .url("http://2.26.71.102:8004/api/mark_read/$chatUsername")
                        .header("Authorization", "Bearer ${internalToken.ifEmpty { token }}")
                        .post(RequestBody.create("application/json".toMediaType(), "")).build()
                    ).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                    })
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "API load error: ${e.message}")
                }
            }
        }

        // Preload последних 10 фото — ПАРАЛЛЕЛЬНО
        val photosToLoad = withContext(Dispatchers.Main) {
            messages.flatMap { it.imageUrls }
                .filter { it.isNotBlank() && !it.startsWith("content://") && !it.startsWith("file://") && !it.startsWith("uploading_") }
                .distinct()
                .takeLast(10)
        }
        if (photosToLoad.isNotEmpty()) {
            withContext(Dispatchers.Main) { preloading = true }
            val ctx = context.applicationContext
            withContext(Dispatchers.IO) {
                photosToLoad.map { url ->
                    async {
                        val fullUrl = if (url.startsWith("http")) {
                            if (url.startsWith("content://") || url.startsWith("file://")) url
                            else if (url.contains("?")) url else "$url?token=${internalToken.ifEmpty { token }}"
                        } else "http://2.26.71.102:8012/uploads/$url?token=${internalToken.ifEmpty { token }}"
                        try {
                            val cacheKey = fullUrl.substringBefore("?")
                            val req = ImageRequest.Builder(ctx)
                                .data(fullUrl)
                                .memoryCacheKey(cacheKey)
                                .diskCacheKey(cacheKey)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build()
                            val result = ctx.imageLoader.execute(req)
                            rlog("Preload", "loaded $cacheKey success=${result is coil.request.SuccessResult}")
                        } catch (e: Exception) {
                            android.util.Log.e("Preload", "fail $url: ${e.message}")
                        }
                    }
                }.awaitAll()
            }
            // Даём Coil время записать в память
            kotlinx.coroutines.delay(100)
            withContext(Dispatchers.Main) { preloading = false }
        }

        withContext(Dispatchers.Main) {
            isLoading = false
        }
    }
        LaunchedEffect(internalToken) {
        if (internalToken.isEmpty()) return@LaunchedEffect
        /* ws.onMessage = { json ->
            val sender = try { com.google.gson.JsonParser.parseString(json).asJsonObject.get("from_user")?.asString ?: "unknown" } catch (e: Exception) { "unknown" }
            val text = try { com.google.gson.JsonParser.parseString(json).asJsonObject.get("text")?.asString ?: "" } catch (e: Exception) { "" }
            val timeVal = try { com.google.gson.JsonParser.parseString(json).asJsonObject.get("time")?.asLong ?: System.currentTimeMillis() / 1000 } catch (e: Exception) { System.currentTimeMillis() / 1000 }
            val msgId = try { com.google.gson.JsonParser.parseString(json).asJsonObject.get("id")?.asInt ?: 0 } catch (e: Exception) { 0 }
            val timeStr = if (timeVal > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeVal * 1000)) else "now"
            // Для своих сообщений - обновляем pending
            if (sender == myUsername) {
                messages = messages.map { item ->
                    val isPhotoMsg = item.imageUrls.isNotEmpty() && text.contains(".jpg")
                    if (item.from == myUsername && (item.text == text || isPhotoMsg) && item.status == "pending") {
                        item.copy(time = timeStr, status = "sent", timeVal = if (timeVal > 0) timeVal else item.timeVal, imageUrls = item.imageUrls)
                    } else item
                }
            } else {
                val existing = messages.find { it.from == sender && it.text == text }
                if (existing == null) {
                    val urls = if (text.contains(".jpg")) listOf(text) else if (text.contains(",")) text.split(",") else emptyList()
                    val cleanText = text   // ← сохраняем подпись
                    val newMsg = MsgItem(sender ?: "unknown", myUsername, cleanText, timeStr, "received", if (timeVal > 0) timeVal else System.currentTimeMillis() / 1000, id = System.currentTimeMillis(), imageUrls = urls)
                    messages = messages + newMsg
                }
            }
        } */
        // ws уже подключен из ViewModel
    }

    // Обновляем дату в шапке при прокрутке
            LaunchedEffect(listState.isScrollInProgress, listState.firstVisibleItemIndex) {
                if (listState.isScrollInProgress) {
                    val items = listState.layoutInfo.visibleItemsInfo
                    if (items.isNotEmpty()) {
                        val firstVisibleIdx = items.firstOrNull()?.index ?: 0
                        val firstDt = if (messages.isNotEmpty() && firstVisibleIdx < messages.size) formatHeaderDate(messages[firstVisibleIdx].timeVal) else ""
                        // Ищем границу: идём вперёд пока дата не изменится
                        var headerDate = firstDt
                        for (i in firstVisibleIdx until messages.size) {
                            val dt = formatHeaderDate(messages[i].timeVal)
                            if (dt.isNotEmpty() && dt != firstDt) {
                                headerDate = dt
                                break
                            }
                        }
                        val lastDate = formatHeaderDate(messages.lastOrNull()?.timeVal ?: 0L)
                        dateInHeader.value = if (headerDate.isNotEmpty() && headerDate != lastDate) headerDate else ""
                    }
                } else {
                    dateInHeader.value = ""
                }
            }
            // При открытии чата — мгновенно вниз к ПОСЛЕДНЕМУ элементу LazyColumn
    LaunchedEffect(chatUsername) {
        kotlinx.coroutines.delay(100)
        val total = listState.layoutInfo.totalItemsCount
        kotlinx.coroutines.delay(120)
                val total2 = listState.layoutInfo.totalItemsCount
                if (total2 > 0) listState.scrollToItem(total2 - 1)
    }


    
    
    // Автоскролл вниз при новом сообщении (как в Telegram)
    LaunchedEffect(messages.size, messages.lastOrNull()?.id) {
        if (messages.isEmpty()) return@LaunchedEffect
        kotlinx.coroutines.delay(50)
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) {
            try {
                listState.animateScrollToItem(total - 1)
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "autoscroll: ${e.message}")
            }
        }
    }
    
    // Загрузка реакций при открытии чата / обновлении сообщений
    LaunchedEffect(messages.size) {
        if (messages.isEmpty()) return@LaunchedEffect
        val ids = messages.takeLast(50).mapNotNull { it.id.takeIf { id -> id > 0 } }
        if (ids.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val reqJson = gson.toJson(mapOf("ids" to ids))
                val reqBody = reqJson.toRequestBody("application/json".toMediaType())
                val resp = httpClient.newCall(
                    Request.Builder()
                        .url("http://2.26.71.102:8016/api/reactions/batch")
                        .header("Authorization", "Bearer ${internalToken.ifEmpty { token }}")
                        .post(reqBody).build()
                ).execute()
                val respText = resp.body?.string() ?: "{}"
                resp.close()
                android.util.Log.d("ChatScreen", "BATCH_RESP: ${respText.take(200)}")
                val obj = org.json.JSONObject(respText)
                val updated = messages.map { m ->
                    val arr = obj.optJSONArray(m.id.toString())
                    if (arr != null) {
                        val list = mutableListOf<Reaction>()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val users = mutableListOf<String>()
                            val ua = o.optJSONArray("users")
                            if (ua != null) for (j in 0 until ua.length()) users.add(ua.getString(j))
                            list.add(Reaction(
                                emoji = o.optString("emoji", ""),
                                count = o.optInt("count", 0),
                                users = users,
                                me = users.contains(myUsername)
                            ))
                        }
                        m.copy(reactions = list)
                    } else m
                }
                withContext(Dispatchers.Main) { messages = updated }
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "BATCH_ERR: ${e.message}")
            }
        }
    }

// === REACTIONS API ===
    fun toggleReactionApi(messageId: Long, emoji: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = gson.toJson(mapOf("message_id" to messageId, "emoji" to emoji))
                val body = json.toRequestBody("application/json".toMediaType())
                val resp = httpClient.newCall(
                    Request.Builder()
                        .url("http://2.26.71.102:8016/api/reaction/toggle")
                        .header("Authorization", "Bearer ${internalToken.ifEmpty { token }}")
                        .post(body).build()
                ).execute()
                val respText = resp.body?.string() ?: ""
                resp.close()
                android.util.Log.d("ChatScreen", "TOGGLE_RESULT: $respText")
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "TOGGLE_ERROR: ${e.message}")
            }
        }
    }
    // === END REACTIONS API ===

    fun sendMessage() {
        if (isSending) {
            android.widget.Toast.makeText(context, "⏳ Уже отправляется...", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        isSending = true
        
        // Отправка фото (все выбранные)
        if (selectedPhotos.isNotEmpty()) {
            showAttachSheet = false
            attachExpanded = false
            val uris = selectedPhotos.toList()  // ВСЕ выбранные
            val caption = inputText.trim()       // ← подпись к фото
            selectedPhotos = emptySet()
            uploadingPhotos = true

            // Создаём пузырь для каждого фото сразу (с локальными Uri как превью)
            val tempIds = uris.map { uri ->
                val tempId = System.currentTimeMillis() + uri.hashCode().toLong()
                val localUriStr = uri.toString()
                messages = messages + MsgItem(
                    myUsername, chatUsername, caption,     // ← текст!
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    "pending", System.currentTimeMillis() / 1000,
                    id = tempId, imageUrls = listOf(localUriStr)
                )
                tempId to uri
            }

            // Скролл вниз после добавления всех пузырей
            scope.launch {
                kotlinx.coroutines.delay(100)
                val total = listState.layoutInfo.totalItemsCount
                kotlinx.coroutines.delay(120)
                val total2 = listState.layoutInfo.totalItemsCount
                if (total2 > 0) listState.scrollToItem(total2 - 1)
            }

            // Загружаем все фото ПАРАЛЛЕЛЬНО
            CoroutineScope(Dispatchers.IO).launch {
                val uploadResults = tempIds.map { (tempId, uri) ->
                    async {
                        val uploadedUrl = try {
                            val input = context.applicationContext.contentResolver.openInputStream(uri)
                            if (input != null) PhotoUploader.uploadPhoto(input, "photo.jpg", token, chatUsername) else null
                        } catch (e: Exception) { null }
                        tempId to uploadedUrl
                    }
                }.awaitAll()

                // Собираем все URL
                val serverUrls = mutableListOf<String>()
                uploadResults.forEach { (tempId, uploadedUrl) ->
                    if (uploadedUrl != null) {
                        val fullUrl = if (uploadedUrl.startsWith("http")) uploadedUrl
                                      else "http://2.26.71.102:8012/uploads/$uploadedUrl"
                        serverUrls.add(fullUrl)

                        // Обновляем UI
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            val newList = ArrayList<MsgItem>(messages.size)
                            for (m in messages) {
                                if (m.id == tempId) {
                                    newList.add(m.copy(imageUrls = listOf(fullUrl), status = "sent"))
                                } else newList.add(m)
                            }
                            messages = newList
                        }
                    } else {
                        // Ошибка загрузки
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            messages = messages.map { m ->
                                if (m.id == tempId) m.copy(status = "error") else m
                            }.toList()
                        }
                    }
                }

                // Отправляем ОДНО сообщение со всеми URL на сервер
                if (serverUrls.isNotEmpty()) {
                    try {
                        val sendJson = gson.toJson(mapOf(
                            "to" to chatUsername, "text" to caption,
                            "imageUrls" to serverUrls
                        ))
                        val sendBody = sendJson.toRequestBody("application/json".toMediaType())
                        val resp = httpClient.newCall(
                            Request.Builder()
                                .url("http://2.26.71.102:8004/api/chat/send")
                                .header("Authorization", "Bearer ${internalToken.ifEmpty { token }}")
                                .post(sendBody).build()
                        ).execute()
                        val respText = resp.body?.string() ?: ""
                        resp.close()
                        val serverId = try {
                            org.json.JSONObject(respText).optLong("id", 0L)
                        } catch (_: Exception) { 0L }

                        // Сохраняем в Room: одно сообщение со всеми URL
                        try {
                            repository?.saveMessage(com.feder.compose.data.entity.MessageEntity(
                                id = serverId,
                                fromUser = myUsername,
                                toUser = chatUsername,
                                text = caption,        // ← подпись
                                timeVal = System.currentTimeMillis() / 1000,
                                imageUrls = serverUrls.joinToString(","),
                                isRead = false
                            ))
                        } catch (_: Exception) {}

                        // Заменяем в UI: слить все пузыри в один с serverUrls
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            val firstTempId = uploadResults.firstOrNull()?.first ?: 0L
                            val newList = ArrayList<MsgItem>(messages.size)
                            var merged = false
                            for (m in messages) {
                                if (m.id == firstTempId && !merged) {
                                    newList.add(m.copy(
                                        id = serverId,
                                        text = caption,        // ← подпись
                                        imageUrls = serverUrls,
                                        status = "sent"
                                    ))
                                    merged = true
                                } else if (m.id in tempIds.map { it.first }) {
                                    // Пропускаем дубли
                                } else {
                                    newList.add(m)
                                }
                            }
                            messages = newList
                        }

                        // Удаляем временные записи из Room (если они там были)
                        tempIds.forEach { (tid, _) ->
                            try { repository?.deleteMessage(tid) } catch (_: Exception) {}
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "send multi: ${e.message}")
                    }
                }

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    uploadingPhotos = false
                    isSending = false
                    inputText = ""     // ← очищаем поле ввода
                    scope.launch {
                        kotlinx.coroutines.delay(80)
                        val total = listState.layoutInfo.totalItemsCount
                        kotlinx.coroutines.delay(120)
                val total2 = listState.layoutInfo.totalItemsCount
                if (total2 > 0) listState.scrollToItem(total2 - 1)
                    }
                }
            }
            return
        }

        // Отправка текста через WebSocket
        if (selectedPhotos.isEmpty() && inputText.isNotBlank()) {
            val txt = inputText.trim()
            val txtId = System.currentTimeMillis()
            val msgJson = gson.toJson(mapOf(
                "type" to "message",
                "text" to txt,
                "to" to chatUsername
            ))
            wsManager?.send(msgJson)
            android.util.Log.d("ChatScreen", "WS_SEND: $msgJson")
            onMessageSent?.invoke(chatUsername, txt)
            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val newMsg = MsgItem(myUsername, chatUsername, txt, now, "pending", System.currentTimeMillis() / 1000, id = txtId)
            messages = messages + newMsg
            // Сохраняем в Room
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository?.saveMessage(com.feder.compose.data.entity.MessageEntity(
                        id = txtId,
                        fromUser = myUsername,
                        toUser = chatUsername,
                        text = txt,
                        timeVal = System.currentTimeMillis() / 1000,
                        imageUrls = null,
                        isRead = false
                    ))
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "save text: ${e.message}")
                }
            }
            inputText = ""
            isSending = false
            return
        }
        
        
        val text = inputText.trim()
        if (text.isEmpty()) return
        if (editMessage != null) {
            wsManager?.send(gson.toJson(mapOf("type" to "edit", "text" to text, "to" to chatUsername)))
            editMessage = null
            inputText = ""
            return
        }
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val txtId = System.currentTimeMillis()
        val newMsg = MsgItem(myUsername, chatUsername, text, now, "pending", System.currentTimeMillis() / 1000, id = txtId, imageUrls = emptyList())
        messages = messages + newMsg
        inputText = ""
        // Автоскролл вниз
        scope.launch {
            kotlinx.coroutines.delay(50)
            kotlinx.coroutines.delay(80)
                val total = listState.layoutInfo.totalItemsCount
                kotlinx.coroutines.delay(120)
                val total2 = listState.layoutInfo.totalItemsCount
                if (total2 > 0) listState.scrollToItem(total2 - 1)
        }

        // Сохраняем в Room
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository?.saveMessage(com.feder.compose.data.entity.MessageEntity(
                    id = txtId,
                    fromUser = myUsername,
                    toUser = chatUsername,
                    text = text,
                    timeVal = System.currentTimeMillis() / 1000,
                    imageUrls = null,
                    isRead = false
                ))
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "save text: ${e.message}")
            }
        }

        wsManager?.send(gson.toJson(mapOf("type" to "message", "text" to text, "to" to chatUsername)))
        inputText = ""
        isSending = false
    }


    Box(modifier = Modifier.fillMaxSize().background(Background).imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(color = Surface, shadowElevation = 2.dp) {
            if (selectionMode) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectionMode = false; selectedMessages = emptySet() }) { Icon(Icons.Filled.Close, "close", tint = Primary, modifier = Modifier.size(24.dp)) }
                    Text("${selectedMessages.size} selected", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        if (selectedMessages.isNotEmpty()) {
                            showForward = true
                            forwardSelected = emptySet()
                        }
                    }) { Icon(Icons.Filled.Forward, "forward", tint = Color.White, modifier = Modifier.size(24.dp)) }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Delete, "delete", tint = Color.White, modifier = Modifier.size(24.dp)) }
                }
            } else {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (searchMode) { searchMode = false; searchQuery = "" } else onBack() }) { Icon(Icons.Filled.ArrowBack, "back", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    if (searchMode) {
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 16.sp),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh).padding(horizontal = 12.dp, vertical = 8.dp),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Search, null, tint = Outline, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    if (searchQuery.isEmpty()) Text("Search messages...", color = Outline, fontSize = 16.sp)
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Box(Modifier.size(40.dp).clip(CircleShape).clickable { onProfileClick() }) {
                            if (avatarUrl != null) {
                                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(if (avatarUrl?.startsWith("/") == true) "http://2.26.71.102:8004$avatarUrl" else avatarUrl).crossfade(true).diskCachePolicy(coil.request.CachePolicy.ENABLED).memoryCachePolicy(coil.request.CachePolicy.ENABLED).build(), contentDescription = chatName, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Text(chatName.take(1).uppercase(), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(chatName, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                when {
                                    isOnline -> "online"
                                    lastSeen > 0 -> formatLastSeen(lastSeen)
                                    else -> "offline"
                                },
                                color = if (isOnline) Color(0xFF41B35D) else OnSurfaceVariant, fontSize = 11.sp
                            )
                        }
                        if (chatUsername != myUsername) {
                            IconButton(onClick = { }) { Icon(Icons.Filled.Videocam, "video", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                            IconButton(onClick = { }) { Icon(Icons.Filled.Call, "call", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                        }
                    }
                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) { Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                        if (showMoreMenu) {
                            Popup(
                                alignment = Alignment.TopEnd,
                                onDismissRequest = { showMoreMenu = false },
                                properties = PopupProperties(focusable = true)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 16.dp)
                                        .width(IntrinsicSize.Max),
                                    shape = RoundedCornerShape(16.dp),
                                    color = SurfaceContainerHigh,
                                    shadowElevation = 8.dp,
                                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth().clickable { showMoreMenu = false; searchMode = true }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Search, "Search", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text("Search", color = OnSurface, fontSize = 14.sp) }
                                        Row(modifier = Modifier.fillMaxWidth().clickable { showMoreMenu = false }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Share, "Share contact", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text("Share contact", color = OnSurface, fontSize = 14.sp) }
                                        Row(modifier = Modifier.fillMaxWidth().clickable { showMoreMenu = false }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Notifications, "Notifications", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text("Notifications", color = OnSurface, fontSize = 14.sp) }
                                        Row(modifier = Modifier.fillMaxWidth().clickable { showMoreMenu = false }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CreateNewFolder, "Add to folder", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text("Add to folder", color = OnSurface, fontSize = 14.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                }

            if (isLoading || preloading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            else {
                LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, contentPadding = PaddingValues(bottom = 12.dp)) {
                    item { Spacer(Modifier.height(16.dp)) }
                    val grouped = messages.groupBy { formatHeaderDate(it.timeVal) }
                    grouped.forEach { (date, msgs) ->
                        if (date.isNotEmpty()) {
                @OptIn(ExperimentalFoundationApi::class)
                            stickyHeader(key = "sticky_$date") {
                                Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh, shadowElevation = 2.dp) {
                                        Text(date, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                        items(msgs, key = { it.id }) { msg ->
                            val index = messages.indexOf(msg)
                            val isMine = msg.from == myUsername
                        val prevMsg = if (index > 0) messages[index - 1] else null
                        val sameAsPrev = prevMsg != null && prevMsg.from == msg.from && kotlin.math.abs((msg.timeVal ?: 0) - (prevMsg.timeVal ?: 0)) < 300 && 
                            kotlin.math.abs(msg.timeVal - prevMsg.timeVal) < 300
                        val nextMsg = if (index < messages.size - 1) messages[index + 1] else null
                        val sameAsNext = nextMsg != null && nextMsg.from == msg.from && kotlin.math.abs((nextMsg.timeVal ?: 0) - (msg.timeVal ?: 0)) < 300
                        val position = when { sameAsPrev && sameAsNext -> 1; sameAsPrev && !sameAsNext -> 2; !sameAsPrev && sameAsNext -> 0; else -> 3 }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectionMode) {
                                Icon(
                                    if (selectedMessages.contains(msg.time)) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = "select",
                                    tint = if (selectedMessages.contains(msg.time)) Primary else OutlineVariant,
                                    modifier = Modifier.size(24.dp).clickable {
                                        if (selectedMessages.contains(msg.time)) {
                                            selectedMessages = selectedMessages - msg.time
                                        } else {
                                            selectedMessages = selectedMessages + msg.time
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Box(modifier = Modifier.onGloballyPositioned { coords -> msgPositions[msg.id] = coords.positionInRoot() }) {
                                MessageBubble(
                            msg,
                            msg.text,
                            msg.time.takeLast(8).take(5),
                            isMine,
                            token = token,
                            position = position,
                            selectionMode = selectionMode, selectedMessages = selectedMessages,
                            allChats = allChats, myUsername = myUsername,
                            onClick = {
                                    if (selectionMode) {
                                        if (selectedMessages.contains(msg.time)) {
                                            selectedMessages = selectedMessages - msg.time
                                        } else {
                                            selectedMessages = selectedMessages + msg.time
                                        }
                                    } else {
                                        val pos = msgPositions[msg.id]
                                        if (pos != null) {
                                            msg.posX = pos.x
                                            msg.posY = pos.y
                                        }
                                        selectedMessage = msg
                                    }
                                },
                            
                            onLongClick = { selectionMode = true; selectedMessages = selectedMessages + msg.time }
                        )
                            }
                        }
                    }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                    item { Spacer(Modifier.height(60.dp)) }
                }

            }


        }

        // Forward screen
        if (showForward && selectedMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().background(Background)
            ) {
                var forwardSearchMode by remember { mutableStateOf(false) }
                // Header
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (forwardSearchMode) { forwardSearchMode = false; forwardSearch = "" } else showForward = false }) {
                        Icon(Icons.Filled.ArrowBack, "back", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    if (forwardSearchMode) {
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = forwardSearch,
                            onValueChange = { forwardSearch = it },
                            singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 16.sp),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh).padding(horizontal = 12.dp, vertical = 8.dp),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Search, null, tint = Outline, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    if (forwardSearch.isEmpty()) Text("Search chats...", color = Outline, fontSize = 16.sp)
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text("Forward message", color = OnSurface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { forwardSearchMode = true }) {
                            Icon(Icons.Filled.Search, "search", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text("Recipients", color = OnSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                
                // Chat list for forward
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    items(allChats.filter { it.username != myUsername && it.username != "123" }) { contact ->
                        val name = contact.name
                        val avatar = contact.avatarUrl ?: ""
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp).combinedClickable(onClick = { val uname = contact.username; if (forwardSelected.isNotEmpty()) { if (forwardSelected.contains(uname)) forwardSelected = forwardSelected - uname else forwardSelected = forwardSelected + uname } }, onLongClick = { val uname = contact.username; forwardSelected = setOf(uname) }), verticalAlignment = Alignment.CenterVertically) {
                            if (forwardSelected.isNotEmpty()) { Icon(if (forwardSelected.contains(contact.username)) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, contentDescription = "select", tint = if (forwardSelected.contains(contact.username)) Primary else OutlineVariant, modifier = Modifier.size(24.dp)) }
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                if (avatar.isNotEmpty()) {
                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(if (avatar.startsWith("/")) "http://2.26.71.102:8004$avatar" else avatar).crossfade(true).diskCachePolicy(coil.request.CachePolicy.ENABLED).memoryCachePolicy(coil.request.CachePolicy.ENABLED).build(), contentDescription = name, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Text(name.take(1), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(name, color = OnSurface, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Spacer(Modifier.size(24.dp))
                        }
                    }
                }

                // ─── Плашка + поле ввода (в стиле чата) ───
                if (forwardSelected.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        color = SurfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Forward, "fwd", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                forwardSelected.size.toString() + " " + pluralMessages(forwardSelected.size) + " переслать",
                                color = OnSurfaceVariant, fontSize = 13.sp
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { showForward = false; forwardSelected = emptySet() }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Filled.Close, "close", tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    color = SurfaceContainerHigh,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        IconButton(onClick = { showAttachSheet = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Add, "add", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = forwardMessage,
                                onValueChange = { forwardMessage = it },
                                singleLine = false,
                                maxLines = 4,
                                textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                                cursorBrush = SolidColor(Primary),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).padding(end = 4.dp).heightIn(max = 80.dp),
                                decorationBox = { innerTextField ->
                                    if (forwardMessage.isEmpty()) Text("Message", color = OnSurfaceVariant, fontSize = 14.sp)
                                    innerTextField()
                                }
                            )
                        }
                        IconButton(onClick = { showEmojiSheet = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.EmojiEmotions, "sticker", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Primary)
                                .clickable {
                                    if (forwardSelected.isNotEmpty()) {
                                        val recipients = forwardSelected.toList()
                                        val text = forwardMessage.ifEmpty { "[пересланное сообщение]" }
                                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            recipients.forEach { to ->
                                                try {
                                                    val body = org.json.JSONObject().apply {
                                                        put("from", myUsername)
                                                        put("to", to)
                                                        put("text", text)
                                                        put("forwarded", true)
                                                    }
                                                    val conn = java.net.URL("http://2.26.71.102:8004/api/chat/send").openConnection() as java.net.HttpURLConnection
                                                    conn.requestMethod = "POST"
                                                    conn.setRequestProperty("Content-Type", "application/json")
                                                    if (token.isNotEmpty()) conn.setRequestProperty("Authorization", "Bearer $token")
                                                    conn.doOutput = true
                                                    conn.outputStream.use { it.write(body.toString().toByteArray()) }
                                                    conn.responseCode
                                                    conn.disconnect()
                                                } catch (e: Exception) {
                                                    android.util.Log.e("ChatScreen", "forward fail: ${e.message}")
                                                }
                                            }
                                        }
                                        forwardMessage = ""
                                        forwardSelected = emptySet()
                                        selectedMessages = emptySet()
                                        selectionMode = false
                                        showForward = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Send, "send", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                
            }
        }
        
        // Message action menu - Popup near message
        if (selectedMessage != null && !showForward) {
            Popup(
                onDismissRequest = { selectedMessage = null; showDeleteSub = false }
            ) {
                val screenHeight = context.resources.displayMetrics.heightPixels
                val rawY = selectedMessage!!.posY.toInt() - 300
                val clampedY = rawY.coerceIn(80, screenHeight - 650)
                val topPadding = with(LocalDensity.current) { clampedY.toDp() }
                Box(Modifier.fillMaxSize().clickable { selectedMessage = null; showDeleteSub = false }) {
                Column(Modifier.fillMaxWidth().padding(end = 16.dp).padding(top = topPadding), horizontalAlignment = Alignment.End) {
                        var showAllReactions by remember { mutableStateOf(false) }
                    val cornerRadius by animateDpAsState(if (showAllReactions) 20.dp else 50.dp, animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f))
                    Surface(shape = RoundedCornerShape(cornerRadius), color = SurfaceContainerHigh, shadowElevation = 16.dp, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
                        val allReactions = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "😍", "🤔", "😡", "👍🏻", "👎", "🔥", "🎉", "💯", "✅", "❤️‍🔥")
                        Column {
                            if (!showAllReactions) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    allReactions.take(6).forEach { emoji ->
                                        Box(Modifier.size(36.dp).clip(CircleShape).clickable { val mid = selectedMessage?.id ?: 0L; if (mid > 0L) toggleReactionApi(mid, emoji); selectedMessage = null }, contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
                                    }
                                    Box(Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerHigh).clickable { showAllReactions = true }, contentAlignment = Alignment.Center) {
                                        Text("›", color = OnSurfaceVariant, fontSize = 20.sp)
                                    }
                                }
                            } else {
                                Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
                                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Reactions", color = OnSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                        Box(Modifier.size(28.dp).clip(CircleShape).background(SurfaceContainerHigh).clickable { showAllReactions = false }, contentAlignment = Alignment.Center) {
                                            Text("✕", color = OnSurfaceVariant, fontSize = 14.sp)
                                        }
                                    }
                                    allReactions.chunked(6).forEach { row ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            row.forEach { emoji ->
                                                Box(Modifier.size(40.dp).clip(CircleShape).clickable { val mid = selectedMessage?.id ?: 0L; if (mid > 0L) toggleReactionApi(mid, emoji); selectedMessage = null }, contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
                                            }
                                            repeat(6 - row.size) { Spacer(Modifier.size(40.dp)) }
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
                    if (!showAllReactions) {
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow, shadowElevation = 16.dp, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
                        Column(Modifier.width(240.dp)) {
                            Row(Modifier.fillMaxWidth().clickable { replyMessage = selectedMessage; selectedMessage = null }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Reply, null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Reply", color = OnSurface, fontSize = 16.sp)
                            }
                            if (selectedMessage?.from == myUsername) {
                                Row(Modifier.fillMaxWidth().clickable { editMessage = selectedMessage; inputText = selectedMessage?.text ?: ""; selectedMessage = null }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Edit, null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Edit", color = OnSurface, fontSize = 16.sp)
                                }
                            }
                            Row(Modifier.fillMaxWidth().clickable { val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager; cm.setPrimaryClip(android.content.ClipData.newPlainText("msg", selectedMessage!!.text)); android.util.Log.d("ChatScreen", "Copied"); selectedMessage = null }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ContentCopy, null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Copy", color = OnSurface, fontSize = 16.sp)
                            }
                            Row(Modifier.fillMaxWidth().clickable { showForward = true }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Forward, null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Forward", color = OnSurface, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) { Text("Group", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = Primary) }
                            }
                            Row(Modifier.fillMaxWidth().clickable { selectionMode = true; selectedMessages = setOf(selectedMessage?.time ?: ""); selectedMessage = null }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckBox, null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Select", color = OnSurface, fontSize = 16.sp)
                            }
                            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                            Row(Modifier.fillMaxWidth().clickable { showDeleteSub = !showDeleteSub }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Delete, null, tint = Error, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("Delete", color = Error, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                Icon(if (showDeleteSub) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                            if (showDeleteSub) {
                                Column {
                                    Row(Modifier.fillMaxWidth().clickable { messages = messages.filter { it != selectedMessage }; selectedMessage = null }.padding(horizontal = 16.dp, vertical = 12.dp).padding(start = 32.dp)) { Text("Delete for me", color = OnSurface, fontSize = 14.sp) }
                                    Row(Modifier.fillMaxWidth().clickable { messages = messages.filter { it != selectedMessage }; selectedMessage = null }.padding(horizontal = 16.dp, vertical = 12.dp).padding(start = 32.dp)) { Text("Delete for all", color = Error, fontSize = 14.sp) }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
                }
        // Attach Sheet
        if (showAttachSheet) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showAttachSheet = false; selectedPhotos = emptySet() })
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (attachExpanded) Modifier.fillMaxHeight() else Modifier)
                    .align(Alignment.BottomCenter)
                    .background(SurfaceContainerLow, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -50) attachExpanded = true
                            if (dragAmount > 50 && attachExpanded) attachExpanded = false
                        }
                    }
            ) {
                // Selected photos preview
                if (selectedPhotos.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedPhotos.forEach { uri ->
                            Box(modifier = Modifier.size(48.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "selected",
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .background(PrimaryContainer, CircleShape)
                                        .clickable { 
                                            selectedPhotos = selectedPhotos - uri
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Close, null, tint = OnPrimaryContainer, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -50) attachExpanded = true
                                if (dragAmount > 50 && attachExpanded) attachExpanded = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(OnSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
                val context = LocalContext.current
                val photos = remember { mutableStateListOf<android.net.Uri>() }
                LaunchedEffect(Unit) {
                    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val projection = arrayOf(MediaStore.Images.Media._ID)
                    context.contentResolver.query(uri, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idCol)
                            val contentUri = android.net.Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                            photos.add(contentUri)
                        }
                    }
                }
                // Gallery grid
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.then(if (attachExpanded) Modifier.fillMaxHeight() else Modifier.height(200.dp)).pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -50) attachExpanded = true
                            if (dragAmount > 50 && attachExpanded) attachExpanded = false
                        }
                    }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos.size) { i ->
                        Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable {
                            val uri = photos[i]
                            selectedPhotos = if (uri in selectedPhotos) selectedPhotos - uri else selectedPhotos + uri
                            android.util.Log.d("ChatScreen", "Selected: ${selectedPhotos.size} photos")
                        }) {
                            AsyncImage(
                                model = photos[i],
                                contentDescription = "photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (photos[i] in selectedPhotos) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(PrimaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Check, null, tint = OnPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Attach options
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    AttachOption(Icons.Filled.Image, "Галерея", true) {
    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}
                    AttachOption(Icons.Filled.PhotoCamera, "Камера")
                    AttachOption(Icons.Filled.Description, "Файл")
                    AttachOption(Icons.Filled.LocationOn, "Локация")
                    AttachOption(Icons.Filled.Person, "Контакт")
                }

                Spacer(Modifier.height(80.dp))


            }
        }
        // Emoji Sheet
        if (showEmojiSheet) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showEmojiSheet = false })
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (emojiExpanded) Modifier.fillMaxHeight() else Modifier)
                    .align(Alignment.BottomCenter)
                    .background(SurfaceContainerLow, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -50) emojiExpanded = true
                                if (dragAmount > 50 && emojiExpanded) emojiExpanded = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(OnSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
                // Заглушка для эмодзи
                Spacer(Modifier.height(200.dp))
                Spacer(Modifier.height(80.dp))
            }
        }
        // Дата в овале — под шапкой по центру
        // Поле ввода поверх сообщений
        if (!showForward) {
            if (isRecording) {
                // ═══ ПАНЕЛЬ ЗАПИСИ ═══
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 4.dp).imePadding().navigationBarsPadding().padding(bottom = 8.dp)) {
                    Surface(shape = RoundedCornerShape(24.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
                        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Красная точка (пульсирует)
                            Box(Modifier.size(12.dp).clip(CircleShape).background(Color.Red.copy(alpha = audioGlowAlpha)))
                            Spacer(Modifier.width(10.dp))
                            // Таймер
                            Text(
                                "%d:%02d".format(recordTimeSec / 60, recordTimeSec % 60),
                                color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.weight(1f))
                            // Slide to cancel / Release to cancel
                            Text(
                                if (recordOffsetX < -100f) "Release to cancel" else "◀ Slide to cancel",
                                color = if (recordOffsetX < -100f) Color.Red else OnSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.weight(1f))
                            // Кнопка lock
                            IconButton(onClick = { recordLocked = !recordLocked }, modifier = Modifier.size(40.dp)) {
                                Icon(if (recordLocked) Icons.Filled.Lock else Icons.Filled.LockOpen, "lock", tint = if (recordLocked) Primary else OnSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            } else {
            Box(modifier = Modifier.fillMaxWidth().then(if (expandInput) Modifier.fillMaxHeight() else Modifier).align(if (expandInput) Alignment.TopCenter else Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 4.dp).imePadding().navigationBarsPadding().padding(bottom = if (expandInput) 16.dp else 8.dp)) {
            Surface(shape = RoundedCornerShape(24.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
                Column {
                    if (editMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            color = SurfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Image, "media", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Tap to add media", color = OnSurfaceVariant, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { editMessage = null; inputText = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Filled.Close, "close", tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    editMessage?.text?.take(80) ?: "",
                                    color = OnSurface,
                                    fontSize = 14.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    if (replyMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            color = SurfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(Modifier.width(3.dp).height(40.dp).background(replyMessage?.from?.hashCode()?.let { Color.hsl((it % 360).toFloat(), 0.7f, 0.6f) } ?: Primary, RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        replyMessage?.from ?: "",
                                        color = replyMessage?.from?.hashCode()?.let { Color.hsl((it % 360).toFloat(), 0.7f, 0.6f) } ?: Primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        replyMessage?.text?.take(80) ?: "",
                                        color = OnSurfaceVariant,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { replyMessage = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Close, "close", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().then(if (expandInput) Modifier.fillMaxHeight() else Modifier).padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.Bottom) {
                    IconButton(onClick = { showAttachSheet = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Add, "add", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Box(modifier = Modifier.weight(1f).then(if (expandInput) Modifier.fillMaxHeight() else Modifier)) {
                        BasicTextField(value = inputText, onValueChange = { inputText = it }, singleLine = false, maxLines = if (expandInput) Int.MAX_VALUE else 4,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp), cursorBrush = SolidColor(Primary),
                            modifier = Modifier.fillMaxWidth().then(if (expandInput) Modifier.fillMaxHeight() else Modifier).padding(vertical = 6.dp).padding(end = 4.dp).heightIn(max = if (expandInput) 1000.dp else 80.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) Text("Message", color = OnSurfaceVariant, fontSize = 14.sp)
                                innerTextField()
                            })
                        if (inputText.contains("\n")) {
                            IconButton(
                                onClick = { expandInput = !expandInput },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                            ) {
                                Icon(if (expandInput) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                    "expand", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (!inputText.contains("\n")) {
                        IconButton(onClick = { showEmojiSheet = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.EmojiEmotions, "sticker", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    if (inputText.contains("\n")) {
                        IconButton(onClick = { expandInput = !expandInput }, modifier = Modifier.size(32.dp)) {
                            Icon(if (expandInput) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp, "expand", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(if (isRecording) (48 * audioPulseScale).dp else 40.dp)
                            .drawBehind {
                                if (isRecording) {
                                    drawCircle(
                                        color = Color.Red.copy(alpha = audioGlowAlpha * 0.55f),
                                        radius = size.minDimension / 2 * 1.55f
                                    )
                                    drawCircle(
                                        color = Color.Red.copy(alpha = audioGlowAlpha * 0.25f),
                                        radius = size.minDimension / 2 * 2.1f
                                    )
                                }
                            }
                            .clip(CircleShape)
                            .background(if (isRecording) Color.Red else PrimaryContainer)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    val startTime = System.currentTimeMillis()
                                    var isLong = false
                                    var totalDx = 0f
                                    // Ждём отпускания, отслеживаем движение
                                    while (true) {
                                        val ev = awaitPointerEvent()
                                        val ch = ev.changes.firstOrNull() ?: break
                                        if (ch.pressed) {
                                            totalDx += ch.positionChange().x
                                            if (isRecordingState.value) recordOffsetX = totalDx
                                            // Long-press сработал?
                                            if (!isLong && !isRecordingState.value && System.currentTimeMillis() - startTime >= 400) {
                                                isLong = true
                                                android.util.Log.d("ChatScreen", "LONG-PRESS — request RECORD_AUDIO")
                                                recordPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                            }
                                        } else {
                                            // ОТПУСТИЛИ
                                            val elapsed = System.currentTimeMillis() - startTime
                                            android.util.Log.d("ChatScreen", "RELEASE isRec=${isRecordingState.value} isLong=$isLong elapsed=$elapsed offset=$recordOffsetX")
                                            if (isRecordingState.value) {
                                                // Мы записывали — отправить или отменить
                                                if (recordOffsetX < -100f) {
                                                    audioRecorder.cancel()
                                                    android.util.Log.d("ChatScreen", "CANCEL")
                                                } else {
                                                    val result = audioRecorder.stop()
                                                    android.util.Log.d("ChatScreen", "AUDIO stop result=${result?.first?.absolutePath} dur=${result?.second}")
                                                    if (result != null) {
                                                        val (file, _) = result
                                                        val localId = System.currentTimeMillis()
                                                        val now = localId / 1000
                                                        val nowStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                                        messages = messages + MsgItem(myUsername, chatUsername, "", nowStr, "pending", now, id = localId, imageUrls = listOf("LOCAL:" + file.absolutePath))
                                                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                            android.util.Log.d("ChatScreen", "UPLOAD start file=${file.name} size=${file.length()}")
                                                            val url = try { com.feder.compose.AudioUploader.uploadAudio(file.inputStream(), file.name, token, chatUsername) } catch (e: Exception) { null }
                                                            android.util.Log.d("ChatScreen", "UPLOAD done url=$url")
                                                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                if (url != null) messages = messages.map { if (it.id == localId) it.copy(status = "sent", imageUrls = listOf(url)) else it }
                                                                file.delete()
                                                            }
                                                        }
                                                    }
                                                }
                                                isRecording = false
                                                recordOffsetX = 0f
                                            } else {
                                                // Не записывали (лаунчер ещё не вернул) — это был tap ИЛИ быстрый long-press
                                                if (!isLong && elapsed < 500) {
                                                    // Обычный tap
                                                    if (inputText.isNotEmpty() || selectedPhotos.isNotEmpty()) {
                                                        sendMessage()
                                                    }
                                                } else if (isLong) {
                                                    // Long-press, но запись ещё не стартовала.
                                                    // Отменяем: помечаем что нужно остановить сразу после старта
                                                    recordReleasedBeforeStart = true
                                                    android.util.Log.d("ChatScreen", "Long-press but record not yet started — schedule cancel")
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(300)
                                                        if (isRecordingState.value) {
                                                            audioRecorder.cancel()
                                                            isRecording = false
                                                            recordOffsetX = 0f
                                                        }
                                                    }
                                                }
                                            }
                                            break
                                        }
                                        ch.consume()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when {
                                isRecording -> Icons.Filled.Stop
                                inputText.isNotEmpty() || selectedPhotos.isNotEmpty() -> Icons.Filled.Send
                                else -> Icons.Filled.Mic
                            },
                            "send",
                            tint = if (isRecording) Color.White else OnPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                }
            }
        }
            }
        }
        // Кнопка прокрутки вниз
        // Полноэкранный просмотр
        if (fullScreenPhoto != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullScreenPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullScreenPhoto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        AnimatedVisibility(
            visible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it < listState.layoutInfo.totalItemsCount - 2 } ?: false,
            modifier = Modifier.padding(end = 28.dp, bottom = 76.dp).align(Alignment.BottomEnd),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = { scope.launch {
                    kotlinx.coroutines.delay(50)
                    val total = listState.layoutInfo.totalItemsCount
                    if (total > 0) listState.animateScrollToItem(total - 1)
                } },
                containerColor = SurfaceContainerHigh,
                contentColor = Primary,
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, "scroll down", modifier = Modifier.size(24.dp))
            }
        }

        
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReactionPill(r: Reaction, allChats: List<ChatItem> = emptyList(), myUsername: String = "", onClick: () -> Unit, onLongClick: () -> Unit) {
    val bc = if (r.me) Primary else OutlineVariant.copy(alpha = 0.4f)
    val bg = if (r.me) Primary.copy(alpha = 0.12f) else SurfaceContainerHigh
    Surface(shape = RoundedCornerShape(50), color = if (r.me) Primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
        modifier = Modifier.height(26.dp).combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, onClick = onClick, onLongClick = onLongClick)) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Мини-аватарки тех, кто поставил реакцию (до 3, слева)
            if (r.users.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    r.users.take(3).forEach { uname ->
                        val chat = allChats.find { it.username == uname }
                        MiniAvatar(url = chat?.avatarUrl, username = uname)
                    }
                }
                Spacer(Modifier.width(2.dp))
            }
            Text(r.emoji, fontSize = 13.sp)
            if (r.count > 0) {
                Text(r.count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = if (r.me) Primary else OnSurfaceVariant)
            }
        }
    }
}







@Composable
fun DrawCheck(
    double: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    size: androidx.compose.ui.unit.Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        // Невидимый Text только чтобы дать Box baseline для alignByBaseline.
        // Цвет прозрачный, размер совпадает с размером шрифта времени (11.sp).
        Text(
            text = "A",
            color = androidx.compose.ui.graphics.Color.Transparent,
            fontSize = 11.sp,
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterStart)
        )
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .size(size)
                .align(androidx.compose.ui.Alignment.Center)
        ) {
            val w = this.size.width
            val h = this.size.height
            val stroke = 2.0f
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.13f, h * 0.52f),
                end = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.78f),
                strokeWidth = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.78f),
                end = androidx.compose.ui.geometry.Offset(w * 0.87f, h * 0.2f),
                strokeWidth = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            if (double) {
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.52f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.78f),
                    strokeWidth = stroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.78f),
                    end = androidx.compose.ui.geometry.Offset(w * 1.13f, h * 0.2f),
                    strokeWidth = stroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}


private fun pluralMessages(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "сообщение"
        mod10 in 2..4 && (mod100 !in 12..14) -> "сообщения"
        else -> "сообщений"
    }
}
