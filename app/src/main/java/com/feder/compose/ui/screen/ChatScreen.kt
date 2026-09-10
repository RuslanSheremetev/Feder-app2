package com.feder.compose.ui.screen

import com.feder.compose.ChatItem
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import coil.request.ImageRequest
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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

var fullScreenPhoto: String? = null
var uploadingPhotos: Boolean = false

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
fun MessageBubble(msg: MsgItem, text: String, time: String, isMine: Boolean, token: String = "", position: Int = 3, onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null, onPositioned: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null, selectionMode: Boolean = false, selectedMessages: Set<String> = emptySet()) {
    val topRadius = when (position) { 0 -> 20.dp; 1 -> 4.dp; 2 -> 4.dp; else -> 20.dp }
    val bottomRadius = when (position) { 0 -> 4.dp; 1 -> 4.dp; 2 -> 20.dp; else -> 20.dp }
    val vertPad = when (position) { 0 -> 8.dp; 1 -> 1.dp; 2 -> 1.dp; else -> 8.dp }
    val ts = if (isMine) 20.dp else topRadius
    val te = if (isMine) topRadius else 20.dp
    val bs = if (isMine) 20.dp else bottomRadius
    val be = if (isMine) bottomRadius else 20.dp
    Column(Modifier.fillMaxWidth().padding(top = vertPad).onGloballyPositioned { coords -> onPositioned?.invoke(coords.positionInRoot()) }, horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        Surface(Modifier.widthIn(max = 280.dp).then(if (onClick != null) Modifier.combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick ?: {}) else Modifier), shape = RoundedCornerShape(ts, te, be, bs), color = if (isMine) PrimaryContainer else SecondaryContainer) {
            Column(Modifier.padding(4.dp)) {
                if (msg.imageUrls != null && msg.imageUrls.isNotEmpty()) {
                    android.util.Log.d("PhotoDisplay", "Rendering photo: ${msg.imageUrls.first()}, count=${msg.imageUrls.size}")
                    Column(Modifier.widthIn(max = 250.dp)) {
                        msg.imageUrls.forEachIndexed { index, url ->
                            Box {
                                if (uploadingPhotos && msg.status == "pending") {
                                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                    }
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(
                                        if (url.contains("?")) url 
                                        else if (url.startsWith("http")) "$url?token=$token" 
                                        else "http://2.26.71.102:8012/uploads/$url?token=$token"
                                    )
                                        .crossfade(true)
                                        .diskCacheKey("${msg.id}_$url")
                                        .memoryCacheKey("${msg.id}_$url")
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { fullScreenPhoto = if (url.contains("?")) url else if (url.startsWith("http")) "$url?token=$token" else "http://2.26.71.102:8012/uploads/$url?token=$token" }
                                        .then(if (msg.imageUrls.size > 1) Modifier.aspectRatio(1f) else Modifier)
                                        .clip(RoundedCornerShape(if (index == 0) 16.dp else 8.dp))
                                        .border(0.1.dp, OutlineVariant.copy(alpha = 0.04f), RoundedCornerShape(if (index == 0) 16.dp else 8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (index == msg.imageUrls.lastIndex) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(time, color = Color.White, fontSize = 11.sp)
                                            if (isMine) {
                                                Spacer(Modifier.width(3.dp))
                                                val checkText = when (msg.status) {
                                                    "read" -> "✓✓"
                                                    "received" -> "✓✓"
                                                    else -> "✓"
                                                }
                                                val checkColor = when (msg.status) {
                                                    "read" -> Color(0xFF4CAF50)
                                                    else -> Color.White
                                                }
                                                Text(checkText, color = checkColor, fontSize = 11.sp)
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
                } else if (msg.imageUrl != null) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(if (msg.imageUrl?.startsWith("http") == true) "${msg.imageUrl}?token=$token" else "http://2.26.71.102:8012/uploads/${msg.imageUrl}?token=$token")
                            .crossfade(true)
                            .diskCacheKey(msg.imageUrl ?: "")
                            .memoryCacheKey(msg.imageUrl ?: "")
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                            contentDescription = "photo",
                            modifier = Modifier.widthIn(max = 250.dp).aspectRatio(1f).clip(RoundedCornerShape(16.dp)).border(0.1.dp, OutlineVariant.copy(alpha = 0.04f), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (time.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(time, color = Color.White, fontSize = 11.sp)
                                    if (isMine) {
                                        Spacer(Modifier.width(3.dp))
                                        val checkText = when (msg.status) {
                                            "read" -> "✓✓"
                                            "received" -> "✓✓"
                                            else -> "✓"
                                        }
                                        val checkColor = when (msg.status) {
                                            "read" -> Color(0xFF4CAF50)
                                            else -> Color.White
                                        }
                                        Text(checkText, color = checkColor, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.Bottom) {
                if (msg.imageUrls.isEmpty() && msg.imageUrl == null) { Text("[$position][id=${msg.id} x=${msg.posX.toInt()} y=${msg.posY.toInt()}] $text", color = if (isMine) OnPrimaryContainer else OnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false))
                if (time.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(time, color = if (isMine) OnPrimaryContainer.copy(alpha = 0.6f) else OnSurfaceVariant, fontSize = 10.sp, modifier = Modifier.offset(y = 2.dp))
                    if (isMine) {
                        Spacer(Modifier.width(2.dp))
                        val checkText = when (msg.status) {
                            "pending" -> "✓"
                            "sent" -> "✓"
                            "received" -> "✓✓"
                            else -> "✓"
                        }
                        val checkColor = when (msg.status) {
                            "read" -> Color(0xFF4CAF50)
                            else -> OnPrimaryContainer.copy(alpha = 0.6f)
                        }
                        Text(checkText, color = checkColor, fontSize = 12.sp, modifier = Modifier.offset(y = 2.dp))
                        }
                    }
                }
                }
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
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, token: String, avatarUrl: String? = null, lastSeen: Long = 0, isOnline: Boolean = false, allChats: List<ChatItem> = emptyList(), wsManager: ProWebSocket? = null, repository: com.feder.compose.repository.ChatRepository? = null, onBack: () -> Unit, onProfileClick: () -> Unit = {}, onMessageSent: ((String, String) -> Unit)? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<MsgItem>>(emptyList()) }
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
    var isFirstNewMessage by remember { mutableStateOf(true) }
    // token passed from MainActivity
    val listState = rememberLazyListState()
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
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8004/api/message_pos").header("Authorization", "Bearer $token").post(body).build()).execute().close()
                } catch (_: Exception) {}
            }
        }
    }
    LaunchedEffect(chatUsername) {
        android.util.Log.d("ChatScreen", "Chat opened: $chatUsername")
        repository?.markRead(chatUsername)

        withContext(Dispatchers.IO) {
            var loadedFromRoom = false

            repository?.let { repo ->
                val cachedMessages = repo.getMessages(myUsername, chatUsername)
                android.util.Log.d("ChatScreen", "Room: ${cachedMessages.size} messages")
                if (cachedMessages.isNotEmpty()) {
                    loadedFromRoom = true
                    val list = cachedMessages.map { entity ->
                        MsgItem(
                            imageUrls = entity.imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                            from = entity.fromUser ?: "unknown",
                            to = entity.toUser ?: "unknown",
                            text = if (entity.imageUrls.isNullOrEmpty()) entity.text else "",
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
                }
            }

            if (!loadedFromRoom) {
                try {
                    if (internalToken.isEmpty()) {
                        val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
                        val authBody = authJson.toRequestBody("application/json".toMediaType())
                        val authResp = httpClient.newCall(Request.Builder()
                            .url("http://2.26.71.102:8004/api/login").post(authBody).build()).execute()
                        internalToken = JsonParser.parseString(authResp.body?.string() ?: "")
                            .asJsonObject.get("access_token")?.asString ?: ""
                    }
                    val msgResp = httpClient.newCall(Request.Builder()
                        .url("http://2.26.71.102:8004/api/messages/$chatUsername")
                        .header("Authorization", "Bearer $token").build()).execute()
                    val body = msgResp.body?.string() ?: "[]"
                    val type = object : com.google.gson.reflect.TypeToken<List<MsgItem>>() {}.type
                    val loaded = gson.fromJson<List<MsgItem>>(body, type)

                    val apiList = loaded.reversed().map { msg ->
                        val urls = msg.imageUrls ?: emptyList()
                        val timeStr = try {
                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(msg.time.toLong() * 1000))
                        } catch (e: Exception) { msg.time }
                        msg.copy(
                            status = msg.status?.ifEmpty { "sent" } ?: "sent",
                            imageUrls = urls,
                            text = msg.text,
                            timeVal = try { msg.time.toLong() } catch (e: Exception) { 0L },
                            time = timeStr
                        )
                    }
                    withContext(Dispatchers.Main) {
                        messages = apiList
                    }

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
                        .header("Authorization", "Bearer $token")
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

        withContext(Dispatchers.Main) {
            isLoading = false
        }
    }
        LaunchedEffect(internalToken) {
        if (internalToken.isEmpty()) return@LaunchedEffect
        wsManager?.onMessage = { json ->
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
                    val cleanText = if (urls.isNotEmpty()) "" else text
                    val newMsg = MsgItem(sender ?: "unknown", myUsername, cleanText, timeStr, "received", if (timeVal > 0) timeVal else System.currentTimeMillis() / 1000, id = System.currentTimeMillis(), imageUrls = urls)
                    messages = messages + newMsg
                }
            }
        }
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
            LaunchedEffect(chatUsername) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }


    
    fun sendMessage() {
        if (isSending) {
            android.widget.Toast.makeText(context, "⏳ Уже отправляется...", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        isSending = true
        
        // Отправка фото
        if (selectedPhotos.isNotEmpty()) {
            showAttachSheet = false
            attachExpanded = false
            val uri = selectedPhotos.first()
            selectedPhotos = emptySet()
            val tempUrl = "uploading_${System.currentTimeMillis()}"
            val tempId = System.currentTimeMillis()
            uploadingPhotos = true
            messages = messages + MsgItem(myUsername, chatUsername, "", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), "pending", System.currentTimeMillis() / 1000, id = tempId, imageUrls = listOf(tempUrl))
            
            CoroutineScope(Dispatchers.IO).launch {
                val uploadedUrl = try {
                    val input = context.applicationContext.contentResolver.openInputStream(uri)
                    if (input != null) PhotoUploader.uploadPhoto(input, "photo.jpg", token, chatUsername) else null
                } catch (e: Exception) { null }
                
                if (uploadedUrl != null) {
                    val fullUrl = if (uploadedUrl.startsWith("http")) uploadedUrl else "http://2.26.71.102:8012/uploads/$uploadedUrl"
                    
                    // Сохраняем в Room в фоне
                    try {
                        repository?.saveMessage(com.feder.compose.data.entity.MessageEntity(
                            id = tempId,
                            fromUser = myUsername,
                            toUser = chatUsername,
                            text = "",
                            timeVal = System.currentTimeMillis() / 1000,
                            imageUrls = fullUrl,
                            isRead = false
                        ))
                    } catch (e: Exception) {}
                    
                    // Обновляем UI через handler.post (гарантированно в Main потоке)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        var updated = false
                        val newList = ArrayList<MsgItem>(messages.size)
                        for (m in messages) {
                            if (!updated && m.id == tempId) {
                                newList.add(m.copy(imageUrls = listOf(fullUrl), status = "sent"))
                                updated = true
                            } else newList.add(m)
                        }
                        messages = newList
                        uploadingPhotos = false
                        isSending = false
                    }
                    
                    // Отправляем на сервер и ЧИТАЕМ server id
                    try {
                        val sendJson = gson.toJson(mapOf("to" to chatUsername, "text" to "", "imageUrls" to listOf(fullUrl)))
                        val sendBody = sendJson.toRequestBody("application/json".toMediaType())
                        val resp = httpClient.newCall(Request.Builder()
                            .url("http://2.26.71.102:8004/api/chat/send")
                            .header("Authorization", "Bearer $token")
                            .post(sendBody).build()).execute()
                        val respText = resp.body?.string() ?: ""
                        resp.close()
                        val serverId = try {
                            org.json.JSONObject(respText).optLong("id", tempId)
                        } catch (_: Exception) { tempId }
                        android.util.Log.d("ChatScreen", "server id=$serverId tempId=$tempId")

                        // Обновляем Room с серверным id (сохраняем СНАЧАЛА, потом удаляем temp)
                        try {
                            repository?.saveMessage(com.feder.compose.data.entity.MessageEntity(
                                id = serverId,
                                fromUser = myUsername,
                                toUser = chatUsername,
                                text = "",
                                timeVal = System.currentTimeMillis() / 1000,
                                imageUrls = fullUrl,
                                isRead = false
                            ))
                            // Теперь безопасно удалить tempId
                            repository?.deleteMessage(tempId)
                        } catch (e: Exception) {
                            android.util.Log.e("ChatScreen", "room update: ${e.message}")
                        }

                        // Обновляем UI с серверным id
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            val newList = ArrayList<MsgItem>(messages.size)
                            for (m in messages) {
                                if (m.id == tempId) {
                                    newList.add(m.copy(id = serverId, imageUrls = listOf(fullUrl), status = "sent"))
                                } else newList.add(m)
                            }
                            messages = newList
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "send error: ${e.message}")
                    }
                } else {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        messages = messages.map { m ->
                            if (m.id == tempId) m.copy(status = "error") else m
                        }.toList()
                        uploadingPhotos = false
                        isSending = false
                        android.util.Log.e("ChatScreen", "Upload FAILED for tempId=$tempId")
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

