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
import com.feder.compose.SimpleWebSocket
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
    val from: String,
    val to: String,
    val text: String,
    val time: String,
    var status: String = "sent",
    val timeVal: Long = 0L,
    val id: Int = 0,
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
fun MessageBubble(msg: MsgItem, text: String, time: String, isMine: Boolean, position: Int = 3, onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null, onPositioned: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null, selectionMode: Boolean = false, selectedMessages: Set<String> = emptySet()) {
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
                if (msg.imageUrls.isNotEmpty()) {
                    android.util.Log.d("PhotoDisplay", "Rendering photo: ${msg.imageUrls.first()}")
                    Column(Modifier.widthIn(max = 250.dp)) {
                        msg.imageUrls.forEachIndexed { index, url ->
                            Box {
                                if (uploadingPhotos && msg.status == "pending") {
                                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                    }
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data("http://2.26.71.102:8002/uploads/${url}")
                                        .crossfade(true)
                                        .diskCacheKey(url)
                                        .memoryCacheKey(url)
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { fullScreenPhoto = "http://2.26.71.102:8002/uploads/${url}" }
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
                            model = ImageRequest.Builder(LocalContext.current).data("http://2.26.71.102:8002/uploads/${msg.imageUrl}")
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
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, token: String, avatarUrl: String? = null, lastSeen: Long = 0, isOnline: Boolean = false, allChats: List<ChatItem> = emptyList(), wsManager: SimpleWebSocket? = null, repository: com.feder.compose.repository.ChatRepository? = null, onBack: () -> Unit, onProfileClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<MsgItem>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isFirstNewMessage by remember { mutableStateOf(true) }
    // token passed from MainActivity
    val listState = rememberLazyListState()
    val gson = remember { Gson() }
    var wsStatus by remember { mutableStateOf("") }
    val ws = wsManager ?: remember(token) {
        SimpleWebSocket().also { it.connect("demo", token) }
    }
    android.util.Log.d("WS_CHAT", "ws=$ws wsManager=$wsManager")
    val httpClient = remember { OkHttpClient() }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var emojiExpanded by remember { mutableStateOf(false) }
    var attachExpanded by remember { mutableStateOf(false) }
    var selectedPhotos by remember { mutableStateOf<Set<android.net.Uri>>(emptySet()) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPhotos = uris.toSet()
            showAttachSheet = true
            try {
                val logJson = gson.toJson(mapOf("log" to "PHOTO_SELECT: ${uris.size} photos"))
                val logBody = logJson.toRequestBody("application/json".toMediaType())
                httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                })
            } catch (_: Exception) {}
        }
    }
    var expandInput by remember { mutableStateOf(false) }
    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<MsgItem?>(null) }
    var selectedMessageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var replyMessage by remember { mutableStateOf<MsgItem?>(null) }
    var editMessage by remember { mutableStateOf<MsgItem?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedMessages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteSub by remember { mutableStateOf(false) }
    var showForward by remember { mutableStateOf(false) }
    var forwardContacts by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var clickedMsgOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var forwardSearch by remember { mutableStateOf("") }
    var forwardSelected by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(Unit) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url("http://2.26.71.102:8002/api/chat_settings/all?me=$myUsername").build()
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            val body = response.body?.string()
            if (body != null) {
                val jsonArray = JsonParser.parseString(body).asJsonArray
                val loaded = jsonArray.map { el ->
                    val obj = el.asJsonObject
                    val uname = obj.get("username")?.asString ?: ""
                    val name = obj.get("name")?.asString ?: uname
                    val avatar = obj.get("avatar_url")?.asString ?: ""
                    mapOf<String, Any?>("username" to uname, "name" to name, "avatar_url" to avatar)
                }
                forwardContacts = loaded
            }
        } catch (_: Exception) {}
    }

        LaunchedEffect(selectedMessage) {
        selectedMessage?.let {
            clickedMsgOffset = selectedMessageOffset
            android.util.Log.d("ChatScreen", "OFFSET: x=${selectedMessageOffset.x.toInt()} y=${selectedMessageOffset.y.toInt()}")
        }
    }

    val msgPositions = remember { mutableMapOf<Int, androidx.compose.ui.geometry.Offset>() }

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
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/message_pos").header("Authorization", "Bearer $internalToken").post(body).build()).execute().close()
                } catch (_: Exception) {}
            }
        }
    }
    LaunchedEffect(chatUsername) {
        android.util.Log.d("ChatScreen", "Chat opened: $chatUsername")
        withContext(Dispatchers.IO) {
            // 1. Загружаем из Room (мгновенно, работает оффлайн)
            repository?.let { repo ->
                val cachedMessages = repo.getMessages(myUsername, chatUsername)
                android.util.Log.d("ChatScreen", "Room: ${cachedMessages.size} messages for $chatUsername")
                if (cachedMessages.isNotEmpty()) {
                    messages = cachedMessages.map { entity ->
                        MsgItem(
                            imageUrls = if (entity.text.contains(".jpg")) listOf(entity.text) else if (entity.text.contains(",")) entity.text.split(",") else emptyList(),
                            from = entity.fromUser,
                            to = entity.toUser,
                            text = if (entity.text.contains(".jpg") || entity.text.contains(",")) "" else entity.text,
                            time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(entity.timeVal * 1000)),
                            status = if (entity.isRead) "read" else "sent",
                            timeVal = entity.timeVal,
                            id = entity.id.toInt(),
                            posX = entity.posX ?: 0f,
                            posY = entity.posY ?: 0f
                        )
                    }
                }
            }
            // 2. Пробуем обновить с API (если интернет есть)
            try {
                if (internalToken.isEmpty()) {
                    val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
                    val authBody = authJson.toRequestBody("application/json".toMediaType())
                    val authResp = httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/login").post(authBody).build()).execute()
                    internalToken = JsonParser.parseString(authResp.body?.string() ?: "").asJsonObject.get("access_token")?.asString ?: ""
                }
                val msgResp = httpClient.newCall(Request.Builder()
                    .url("http://2.26.71.102:8002/api/messages/$chatUsername")
                    .header("Authorization", "Bearer $internalToken").build()).execute(); android.util.Log.d("ChatScreen", "Token: ${internalToken.take(20)}...")
                val type = object : TypeToken<List<MsgItem>>() {}.type
                val body = msgResp.body?.string() ?: "[]"
                val loaded = gson.fromJson<List<MsgItem>>(body, type)
                messages = loaded.map { msg ->
                    val urls = if (msg.text.contains(".jpg")) listOf(msg.text) else if (msg.text.contains(",")) msg.text.split(",") else emptyList()
                    val cleanText = if (urls.isNotEmpty()) "" else msg.text
                    msg.copy(status = msg.status?.ifEmpty { "sent" } ?: "sent", imageUrls = urls, text = cleanText)
                }
                // Сохраняем загруженные сообщения в Room
                repository?.let { repo ->
                    repo.saveMessages(loaded.map { msg ->
                        com.feder.compose.data.entity.MessageEntity(
                            id = msg.id.toLong(),
                            fromUser = msg.from,
                            toUser = msg.to,
                            text = if (msg.imageUrls.isNotEmpty()) msg.imageUrls.joinToString(",") else msg.text,
                            timeVal = msg.timeVal,
                            isRead = msg.status == "read",
                            posX = msg.posX,
                            posY = msg.posY
                        )
                    })
                }
                try {
                    val logBody = "{\"log\":\"Loaded \${loaded.size} messages for \$chatUsername, first=\${loaded.firstOrNull()?.text?.take(20)}\"}".toRequestBody("application/json".toMediaType())
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/chat/send").header("Authorization", "Bearer $internalToken").post(logBody).build()).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                    })
                } catch (_: Exception) { }
                isFirstNewMessage = true
                // Отмечаем как прочитанные
                try {
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/mark_read/$chatUsername").header("Authorization", "Bearer $internalToken").post(RequestBody.create("application/json".toMediaType(), "")).build()).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                    })
                } catch (_: Exception) { }
            } catch (e: Exception) {
                // Если уже есть сообщения из Room — не показываем ошибку
                if (messages.isEmpty()) {
                    messages = listOf(MsgItem("system", chatUsername, "Error: ${e.message}", "", "error", 0L, id = 0, imageUrls = emptyList()))
                }
                try {
                    val logBody = "{\"log\":\"ChatScreen error: ${e.message}\"}".toRequestBody("application/json".toMediaType())
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/chat/send").header("Authorization", "Bearer $internalToken").post(logBody).build()).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                    })
                } catch (_: Exception) { }
            }
            isLoading = false
        }
    }
    // WebSocket отдельно
    LaunchedEffect(internalToken) {
        if (internalToken.isEmpty()) return@LaunchedEffect
        ws.onRead { from ->
            messages = messages.map { msg ->
                if (msg.from == myUsername && msg.to == from) msg.copy(status = "read", imageUrls = msg.imageUrls ?: emptyList()) else msg
            }
        }
        ws.onReceived { from ->
            messages = messages.map { msg ->
                if (msg.from == myUsername && msg.to == from) msg.copy(status = "received", imageUrls = msg.imageUrls ?: emptyList()) else msg
            }
        }
        ws.onMessage { sender, text, timeVal, msgId ->
            val timeStr = if (timeVal > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeVal * 1000)) else "now"
            // Для своих сообщений - обновляем pending
            if (sender == myUsername) {
                messages = messages.map { msg ->
                    val isPhotoMsg = msg.imageUrls.isNotEmpty() && text.contains(".jpg")
                    if (msg.from == myUsername && (msg.text == text || isPhotoMsg) && msg.status == "pending") {
                        msg.copy(time = timeStr, status = "sent", timeVal = if (timeVal > 0) timeVal else msg.timeVal, imageUrls = msg.imageUrls)
                    } else msg
                }
            } else {
                val existing = messages.find { it.from == sender && it.text == text }
                if (existing == null) {
                    val urls = if (text.contains(".jpg")) listOf(text) else if (text.contains(",")) text.split(",") else emptyList()
val cleanText = if (urls.isNotEmpty()) "" else text
val newMsg = MsgItem(sender, myUsername, cleanText, timeStr, "received", if (timeVal > 0) timeVal else System.currentTimeMillis() / 1000, id = -(java.util.UUID.randomUUID().hashCode()), imageUrls = urls)
                    messages = messages + newMsg
                }
            }
        }
        ws.connect(myUsername, internalToken)
    }

    // Обновляем дату в шапке при прокрутке
            LaunchedEffect(listState.isScrollInProgress, listState.firstVisibleItemIndex) {
                if (listState.isScrollInProgress) {
                    val items = listState.layoutInfo.visibleItemsInfo
                    if (items.isNotEmpty()) {
                        val firstVisibleIdx = items.first().index
                        val firstDt = formatHeaderDate(messages[firstVisibleIdx].timeVal)
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
        // Отправка выбранных фото
        if (selectedPhotos.isNotEmpty()) {
            android.util.Log.d("PhotoSend", "Sending ${selectedPhotos.size} photos")
            android.util.Log.d("PhotoSend", "Sending ${selectedPhotos.size} photos, ws=${if (ws != null) "OK" else "NULL"}")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val urls = mutableListOf<String>()
                    for (photo in selectedPhotos) {
                        val bytes = context.contentResolver.openInputStream(photo)?.readBytes()
                        if (bytes != null) {
                            // Multipart form-data
                            val boundary = "boundary${System.currentTimeMillis()}"
                            val mediaType = "multipart/form-data; boundary=$boundary".toMediaType()
                            val bodyBuilder = StringBuilder()
                            bodyBuilder.append("--$boundary\r\n")
                            bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n")
                            bodyBuilder.append("Content-Type: image/jpeg\r\n\r\n")
                            val headerBytes = bodyBuilder.toString().toByteArray()
                            val footerBytes = "\r\n--$boundary--\r\n".toByteArray()
                            
                            val multipart = ByteArray(headerBytes.size + bytes.size + footerBytes.size)
                            System.arraycopy(headerBytes, 0, multipart, 0, headerBytes.size)
                            System.arraycopy(bytes, 0, multipart, headerBytes.size, bytes.size)
                            System.arraycopy(footerBytes, 0, multipart, headerBytes.size + bytes.size, footerBytes.size)
                            
                            val body = multipart.toRequestBody(mediaType)
                            val request = Request.Builder()
                                .url("http://2.26.71.102:8002/api/upload")
                                .header("Authorization", "Bearer $internalToken")
                                .post(body)
                                .build()
                            val response = httpClient.newCall(request).execute()
                            val respJson = JsonParser.parseString(response.body?.string() ?: "{}").asJsonObject
                            val url = respJson.get("url")?.asString
                            android.util.Log.d("PhotoSend", "Uploaded: $url")
                            try {
                                val logJson = gson.toJson(mapOf("log" to "PHOTO_UPLOADED: $url"))
                                val logBody = logJson.toRequestBody("application/json".toMediaType())
                                httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                                })
                            } catch (_: Exception) {}
                            try {
                                val logJson = gson.toJson(mapOf("log" to "PhotoSend: url=$url"))
                                val logBody = logJson.toRequestBody("application/json".toMediaType())
                                httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                                })
                            } catch (_: Exception) {}
                            if (url != null) {
                                urls.add(url)
                                android.util.Log.d("PhotoSend", "Added URL: $url, total: ${urls.size}")
                            }
                        }
                    }
                    if (urls.isNotEmpty()) {
                        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        // Берём текст из inputText (если есть)
                        val caption = inputText.trim()
                        // Отправляем все фото + подпись в одном сообщении
                        val combinedText = if (caption.isNotEmpty()) {
                            caption + "\n" + urls.joinToString(",")
                        } else {
                            urls.joinToString(",")
                        }
                        val newMsg = MsgItem(myUsername, chatUsername, caption, now, "pending", System.currentTimeMillis() / 1000, id = -(java.util.UUID.randomUUID().hashCode()), imageUrls = urls)
                        withContext(Dispatchers.Main) {
                            messages = messages + newMsg
                            android.util.Log.d("PhotoSend", "NEW_MSG imageUrls=${newMsg.imageUrls} text=${newMsg.text} total=${messages.size}")
                            try {
                                val logJson = gson.toJson(mapOf("log" to "PHOTO_NEW_MSG: urls=${newMsg.imageUrls} text='${newMsg.text}'"))
                                val logBody = logJson.toRequestBody("application/json".toMediaType())
                                httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                                })
                            } catch (_: Exception) {}
                            try {
                                val logJson = gson.toJson(mapOf("log" to "PHOTO_NEW_MSG: imageUrls=${newMsg.imageUrls} text=${newMsg.text}"))
                                val logBody = logJson.toRequestBody("application/json".toMediaType())
                                httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                                })
                            } catch (_: Exception) {}
                        }
                        val photoText = if (caption.isNotEmpty()) caption + "\n" + urls.joinToString(",") else urls.joinToString(",")
                        // Отправляем через HTTP API
                        try {
                            val logJson = gson.toJson(mapOf("log" to "PHOTO_SEND_HTTP: to=$chatUsername text=$photoText"))
                            val logBody = logJson.toRequestBody("application/json".toMediaType())
                            httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                            })
                        } catch (_: Exception) {}
                        try {
                            val sendJson = gson.toJson(mapOf("to" to chatUsername, "text" to photoText))
                            val sendBody = sendJson.toRequestBody("application/json".toMediaType())
                            val sendRequest = Request.Builder()
                                .url("http://2.26.71.102:8002/api/chat/send")
                                .header("Authorization", "Bearer $internalToken")
                                .post(sendBody)
                                .build()
                            httpClient.newCall(sendRequest).execute()
                        } catch (_: Exception) {}
                        withContext(Dispatchers.Main) {
                            inputText = ""
                        }
                        try {
                            val logJson = gson.toJson(mapOf("log" to "PhotoSend: ws.send text=$combinedText"))
                            val logBody = logJson.toRequestBody("application/json".toMediaType())
                            httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/logs").post(logBody).build()).enqueue(object : okhttp3.Callback {
                                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                            })
                        } catch (_: Exception) {}
                        withContext(Dispatchers.Main) {
                            selectedPhotos = emptySet()
                            showAttachSheet = false
                            uploadingPhotos = false
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PhotoSend", "Error: ${e.message}", e)
                }
            }
            return
        }
        val text = inputText.trim()
        if (text.isEmpty()) return
        if (editMessage != null) {
            // Отправляем edit
            ws?.send(gson.toJson(mapOf("type" to "edit", "text" to text, "to_user" to chatUsername)))
            editMessage = null
            inputText = ""
            return
        }
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = MsgItem(myUsername, chatUsername, text, now, "pending", System.currentTimeMillis() / 1000, id = -(java.util.UUID.randomUUID().hashCode()), imageUrls = emptyList())
        messages = messages + newMsg
        inputText = ""
        // Отправляем через HTTP API (WebSocket не работает для отправки)
        android.util.Log.d("WS", "ChatScreen: sending text='$text' to='$chatUsername'")
        android.util.Log.d("WS", "Send text: ws=${ws != null} text=$text")
        ws?.send(gson.toJson(mapOf("type" to "message", "text" to text, "to_user" to chatUsername)))
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(color = Surface, shadowElevation = 2.dp) {
            if (selectionMode) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectionMode = false; selectedMessages = emptySet() }) { Icon(Icons.Filled.Close, "close", tint = Primary, modifier = Modifier.size(24.dp)) }
                    Text("${selectedMessages.size} selected", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { }) { Icon(Icons.Filled.Forward, "forward", tint = Color.White, modifier = Modifier.size(24.dp)) }
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
                        Box(Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant, CircleShape).clickable { onProfileClick() }) {
                            if (avatarUrl != null) {
                                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).diskCachePolicy(coil.request.CachePolicy.ENABLED).memoryCachePolicy(coil.request.CachePolicy.ENABLED).build(), contentDescription = chatName, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
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

            if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            else {
                LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, contentPadding = PaddingValues(bottom = 80.dp)) {
                    item { Spacer(Modifier.height(16.dp)) }
                    val grouped = messages.groupBy { formatHeaderDate(it.timeVal) }
                    grouped.forEach { (date, msgs) ->
                        if (date.isNotEmpty()) {
                @OptIn(ExperimentalFoundationApi::class)
                            stickyHeader(key = "sticky_$date") {
                                Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh, shadowElevation = 2.dp) {
                                        Text(date, color = OnSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
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
                            position = position,
                            selectionMode = selectionMode, selectedMessages = selectedMessages,
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
                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(avatar).crossfade(true).diskCachePolicy(coil.request.CachePolicy.ENABLED).memoryCachePolicy(coil.request.CachePolicy.ENABLED).build(), contentDescription = name, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
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
                                        Box(Modifier.size(36.dp).clip(CircleShape).clickable { selectedMessage = null }, contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
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
                                                Box(Modifier.size(40.dp).clip(CircleShape).clickable { selectedMessage = null }, contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
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
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showAttachSheet = false })
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
            Box(modifier = Modifier.fillMaxWidth().then(if (expandInput) Modifier.fillMaxHeight() else Modifier).align(if (expandInput) Alignment.TopCenter else Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 4.dp).imePadding().navigationBarsPadding().padding(bottom = if (expandInput) 16.dp else 8.dp)) {
            Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
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
                    Row(Modifier.fillMaxWidth().then(if (expandInput) Modifier.fillMaxHeight() else Modifier).padding(horizontal = 8.dp, vertical = 4.dp).padding(4.dp), verticalAlignment = Alignment.Bottom) {
                    IconButton(onClick = { showAttachSheet = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Add, "add", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Box(modifier = Modifier.weight(1f).then(if (expandInput) Modifier.fillMaxHeight() else Modifier)) {
                        BasicTextField(value = inputText, onValueChange = { inputText = it }, singleLine = false, maxLines = if (expandInput) Int.MAX_VALUE else 4,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp), cursorBrush = SolidColor(Primary),
                            modifier = Modifier.fillMaxWidth().then(if (expandInput) Modifier.fillMaxHeight() else Modifier).padding(vertical = 6.dp).padding(end = 32.dp).heightIn(max = if (expandInput) 1000.dp else 80.dp),
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
                    Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryContainer).clickable { android.util.Log.d("ChatScreen", "CLICKED send"); sendMessage() }, contentAlignment = Alignment.Center) {
                        Icon(if (inputText.isEmpty() && selectedPhotos.isEmpty()) Icons.Filled.Mic else Icons.Filled.Send, "send", tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
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
            visible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it < messages.size - 2 } ?: false,
            modifier = Modifier.padding(end = 28.dp, bottom = 76.dp).align(Alignment.BottomEnd),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(messages.size - 1) } },
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

