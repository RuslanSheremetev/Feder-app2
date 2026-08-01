package com.feder.compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feder.compose.WebSocketManager
import com.feder.compose.ui.theme.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class MsgItem(
    val from: String,
    val to: String,
    val text: String,
    val time: String,
    var status: String = "sent",
    val timeVal: Long = 0L
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, token: String, onBack: () -> Unit, onProfileClick: () -> Unit = {}) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<MsgItem>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isFirstNewMessage by remember { mutableStateOf(true) }
    // token passed from MainActivity
    val listState = rememberLazyListState()
    val gson = remember { Gson() }
    var wsStatus by remember { mutableStateOf("") }
    val wsManager = remember { WebSocketManager() }
    val httpClient = remember { OkHttpClient() }
    var showAttachSheet by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MsgItem?>(null) }
    var selectedMessageOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var replyMessage by remember { mutableStateOf<MsgItem?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedMessages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteSub by remember { mutableStateOf(false) }
    var showForward by remember { mutableStateOf(false) }
    var forwardSearch by remember { mutableStateOf("") }

    var internalToken = token
    LaunchedEffect(chatUsername) {
        withContext(Dispatchers.Main) { Toast.makeText(context, "Chat opened: $chatUsername", Toast.LENGTH_SHORT).show() }
        withContext(Dispatchers.IO) {
            try {
                if (internalToken.isEmpty()) {
                    val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
                    val authBody = authJson.toRequestBody("application/json".toMediaType())
                    val authResp = httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/login").post(authBody).build()).execute()
                    internalToken = JsonParser.parseString(authResp.body?.string() ?: "").asJsonObject.get("access_token")?.asString ?: ""
                }
                val msgResp = httpClient.newCall(Request.Builder()
                    .url("http://2.26.71.102:8002/api/messages/$chatUsername")
                    .header("Authorization", "Bearer $internalToken").build()).execute(); withContext(Dispatchers.Main) { Toast.makeText(context, "Token: ${internalToken.take(20)}... Body: ${msgResp.body?.contentLength()}", Toast.LENGTH_SHORT).show() }
                val type = object : TypeToken<List<MsgItem>>() {}.type
                val body = msgResp.body?.string() ?: "[]"
                val loaded = gson.fromJson<List<MsgItem>>(body, type)
                messages = loaded.map { it.copy(status = it.status?.ifEmpty { "sent" } ?: "sent") }.map { it.copy(status = it.status.ifEmpty { "sent" }) }; withContext(Dispatchers.Main) { val m1 = loaded.getOrNull(0); val m2 = loaded.getOrNull(1); Toast.makeText(context, "f1=${m1?.from} f2=${m2?.from} tv1=${m1?.timeVal} tv2=${m2?.timeVal}", Toast.LENGTH_LONG).show() }
                try {
                    val logBody = "{\"log\":\"Loaded \${loaded.size} messages for \$chatUsername, first=\${loaded.firstOrNull()?.text?.take(20)}\"}".toRequestBody("application/json".toMediaType())
                    httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/chat/send").header("Authorization", "Bearer $internalToken").post(logBody).build()).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                    })
                } catch (_: Exception) { }
                isFirstNewMessage = true
            } catch (e: Exception) {
                messages = listOf(MsgItem("system", chatUsername, "Error: ${e.message}", "", "error", 0L))
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
        wsManager.onMessage { sender, text, timeVal ->
            val timeStr = if (timeVal > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeVal * 1000)) else "now"
            // Для своих сообщений - обновляем pending
            if (sender == myUsername) {
                messages = messages.map { msg ->
                    if (msg.from == myUsername && msg.text == text && msg.status == "pending") msg.copy(time = timeStr, status = "sent", timeVal = if (timeVal > 0) timeVal else msg.timeVal)
                    else msg
                }
            } else {
                val existing = messages.find { it.from == sender && it.text == text }
                if (existing == null) {
                    val newMsg = MsgItem(sender, myUsername, text, timeStr, "received", if (timeVal > 0) timeVal else System.currentTimeMillis() / 1000)
                    messages = messages + newMsg
                }
            }
        }
        wsManager.connect(myUsername, internalToken)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty()) return
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = MsgItem(myUsername, chatUsername, text, now, "pending", System.currentTimeMillis() / 1000)
        messages = messages + newMsg
        inputText = ""
        // HTTP fallback если WebSocket не подключён
        if (wsStatus != "connected") {
            try {
                val json = gson.toJson(mapOf("to" to chatUsername, "text" to text))
                val body = json.toRequestBody("application/json".toMediaType())
                httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/chat/send").header("Authorization", "Bearer $internalToken").post(body).build()).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                })
            } catch (e: Exception) { }
        }
        wsManager.send("message", text, chatUsername)
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Selection Bar
            if (selectionMode) {
                Surface(color = Color(0xFF201F1F), shadowElevation = 4.dp) {
                    Row(
                        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectionMode = false; selectedMessages = emptySet() }) {
                            Icon(Icons.Filled.Close, "close", tint = Primary, modifier = Modifier.size(24.dp))
                        }
                        Text(
                            "${selectedMessages.size} selected",
                            color = OnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { /* Forward */ }) {
                            Icon(Icons.Filled.Forward, "forward", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        IconButton(onClick = { /* Delete */ }) {
                            Icon(Icons.Filled.Delete, "delete", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            // Header
            Surface(color = Surface, shadowElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "back", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    Box(Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant, CircleShape)) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Text(chatName.take(1).uppercase(), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chatName, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Text(wsStatus.ifEmpty { "online" }, color = if (wsStatus.startsWith("error")) Error else Primary, fontSize = 11.sp)
                    }
                    if (chatUsername != myUsername) {
                        IconButton(onClick = { }) { Icon(Icons.Filled.Videocam, "video", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                        IconButton(onClick = { }) { Icon(Icons.Filled.Call, "call", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    }
                    IconButton(onClick = onProfileClick) { Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                }
            }

            if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            else {
                LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, contentPadding = PaddingValues(bottom = 72.dp)) {
                    item { Spacer(Modifier.height(16.dp)) }
                    itemsIndexed(messages) { index, msg ->
                        val isMine = msg.from == myUsername
                        val prevMsg = if (index > 0) messages[index - 1] else null
                        val sameAsPrev = prevMsg != null && prevMsg.from == msg.from && kotlin.math.abs((msg.timeVal ?: 0) - (prevMsg.timeVal ?: 0)) < 300 && 
                            kotlin.math.abs(msg.timeVal - prevMsg.timeVal) < 300
                        val nextMsg = if (index < messages.size - 1) messages[index + 1] else null
                        val sameAsNext = nextMsg != null && nextMsg.from == msg.from && kotlin.math.abs((nextMsg.timeVal ?: 0) - (msg.timeVal ?: 0)) < 300
                        val position = when { sameAsPrev && sameAsNext -> 1; sameAsPrev && !sameAsNext -> 2; !sameAsPrev && sameAsNext -> 0; else -> 3 }
                        Box(modifier = Modifier.onGloballyPositioned { selectedMessageOffset = it.positionInRoot() }) {
                        MessageBubble(
                            msg,
                            msg.text,
                            msg.time.takeLast(8).take(5),
                            isMine,
                            position = position,
                            onClick = { selectedMessage = msg },
                            onLongClick = { selectionMode = true; selectedMessages = selectedMessages + msg.time }
                        )
                    }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            // Reply Preview
            if (replyMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(4.dp).height(40.dp).background(Primary, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                replyMessage?.from ?: "",
                                color = Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                replyMessage?.text?.take(50) ?: "",
                                color = OnSurfaceVariant,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { replyMessage = null }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, "close", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            // Input
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).imePadding()) {
                Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAttachSheet = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Add, "add", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        BasicTextField(value = inputText, onValueChange = { inputText = it }, singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp), cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) Text("Message", color = OnSurfaceVariant, fontSize = 14.sp)
                                innerTextField()
                            })
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.EmojiEmotions, "sticker", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryContainer).clickable { sendMessage() }, contentAlignment = Alignment.Center) {
                            Icon(if (inputText.isEmpty()) Icons.Filled.Mic else Icons.Filled.Send, "send", tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        // Forward screen
        if (showForward && selectedMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().background(Background)
            ) {
                // Header
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showForward = false }) { Icon(Icons.Filled.Close, "close", tint = Color.White, modifier = Modifier.size(24.dp)) }
                    Spacer(Modifier.width(8.dp))
                    Text("Forward message", color = OnSurface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
                
                // Preview
                Surface(Modifier.fillMaxWidth().padding(16.dp), RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Preview", color = Primary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(selectedMessage!!.text, color = OnSurface, fontSize = 14.sp, maxLines = 2)
                    }
                }
                
                // Search
                Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(28.dp), color = SurfaceContainerHigh) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Search, "search", tint = Outline, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(value = forwardSearch, onValueChange = { forwardSearch = it }, singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp), cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { if (forwardSearch.isEmpty()) Text("Search chats...", color = Outline, fontSize = 14.sp); it() }
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text("Recipients", color = OnSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                
                // Chat list for forward
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    items(listOf("Alex Rivera", "Sarah Jenkins", "David Miller", "Elena Rodriguez")) { name ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Text(name.take(1), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(name, color = OnSurface, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.CheckCircle, "selected", tint = Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                // Input field like in chat
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).imePadding()) {
                    Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            var forwardText by remember { mutableStateOf("") }
                            BasicTextField(
                                value = forwardText,
                                onValueChange = { forwardText = it },
                                singleLine = true,
                                textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                                cursorBrush = SolidColor(Primary),
                                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                                decorationBox = { if (forwardText.isEmpty()) Text("Add comment...", color = OnSurfaceVariant, fontSize = 14.sp); it() }
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).background(PrimaryContainer).clickable { showForward = false; selectedMessage = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Send, "send", tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Message action menu — DropdownMenu рядом с сообщением
        Box {
            DropdownMenu(
                expanded = selectedMessage != null && !showForward,
                onDismissRequest = { selectedMessage = null; showDeleteSub = false },
                offset = androidx.compose.ui.unit.DpOffset(
                    x = with(LocalDensity.current) { selectedMessageOffset.x.toDp() },
                    y = with(LocalDensity.current) { selectedMessageOffset.y.toDp() }
                )
            ) {
                DropdownMenuItem(text = { Text("Ответить") }, onClick = { replyMessage = selectedMessage; selectedMessage = null }, leadingIcon = { Icon(Icons.Filled.Reply, null) })
                DropdownMenuItem(text = { Text("Копировать") }, onClick = { selectedMessage = null }, leadingIcon = { Icon(Icons.Filled.ContentCopy, null) })
                DropdownMenuItem(text = { Text("Переслать") }, onClick = { showForward = true }, leadingIcon = { Icon(Icons.Filled.Forward, null) })
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Удалить", color = MaterialTheme.colorScheme.error) }, onClick = { selectedMessage = null }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) })
            }
        }
        // Attach Sheet
        if (showAttachSheet) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showAttachSheet = false })
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(SurfaceContainerLow, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                // Gallery grid
                Text("Недавние фото", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(6) { i ->
                        Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh), contentAlignment = Alignment.Center) {
                            if (i < 5) Icon(Icons.Filled.Image, "photo", tint = Outline, modifier = Modifier.size(32.dp))
                            else Icon(Icons.Filled.PhotoCamera, "camera", tint = Outline, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Attach options
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    AttachOption(Icons.Filled.Image, "Галерея", true)
                    AttachOption(Icons.Filled.PhotoCamera, "Камера")
                    AttachOption(Icons.Filled.Description, "Файл")
                    AttachOption(Icons.Filled.LocationOn, "Локация")
                    AttachOption(Icons.Filled.Person, "Контакт")
                }

                Spacer(Modifier.height(16.dp))

                // Input like in chat
                var attachCaption by remember { mutableStateOf("") }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
                    Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EmojiEmotions, "emoji", tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = attachCaption,
                                onValueChange = { attachCaption = it },
                                singleLine = true,
                                textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                                cursorBrush = SolidColor(Primary),
                                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                                decorationBox = { if (attachCaption.isEmpty()) Text("Добавить подпись...", color = OnSurfaceVariant, fontSize = 14.sp); it() }
                            )
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryContainer).clickable { showAttachSheet = false }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Send, "send", tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachOption(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
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
fun MessageBubble(msg: MsgItem, text: String, time: String, isMine: Boolean, position: Int = 3, onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null) {
    val topRadius = when (position) { 0 -> 20.dp; 1 -> 4.dp; 2 -> 4.dp; else -> 20.dp }
    val bottomRadius = when (position) { 0 -> 4.dp; 1 -> 4.dp; 2 -> 20.dp; else -> 20.dp }
    val vertPad = when (position) { 0 -> 8.dp; 1 -> 1.dp; 2 -> 1.dp; else -> 8.dp }
    val ts = if (isMine) 20.dp else topRadius
    val te = if (isMine) topRadius else 20.dp
    val bs = if (isMine) 20.dp else bottomRadius
    val be = if (isMine) bottomRadius else 20.dp
    Column(Modifier.fillMaxWidth().padding(top = vertPad), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        Surface(Modifier.widthIn(max = 280.dp).then(if (onClick != null) Modifier.combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick ?: {}) else Modifier), shape = RoundedCornerShape(ts, te, be, bs), color = if (isMine) PrimaryContainer else SecondaryContainer) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.Bottom) {
                Text("[$position] $text", color = if (isMine) OnPrimaryContainer else OnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false))
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
