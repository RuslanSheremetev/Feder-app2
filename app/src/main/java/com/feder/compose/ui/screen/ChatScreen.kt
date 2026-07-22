package com.feder.compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
    var status: String = "sent"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, onBack: () -> Unit, onProfileClick: () -> Unit = {}) {
    var messages by remember { mutableStateOf<List<MsgItem>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var token by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val gson = remember { Gson() }
    var wsStatus by remember { mutableStateOf("") }
    val wsManager = remember { WebSocketManager() }
    val httpClient = remember { OkHttpClient() }
    var showAttachSheet by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MsgItem?>(null) }
    var showDeleteSub by remember { mutableStateOf(false) }
    var showForward by remember { mutableStateOf(false) }
    var forwardSearch by remember { mutableStateOf("") }

    LaunchedEffect(chatUsername) {
        withContext(Dispatchers.IO) {
            try {
                val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
                val body = authJson.toRequestBody("application/json".toMediaType())
                val resp = httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/login").post(body).build()).execute()
                token = JsonParser.parseString(resp.body?.string() ?: "").asJsonObject.get("access_token")?.asString ?: ""
                val msgResp = httpClient.newCall(Request.Builder()
                    .url("http://2.26.71.102:8002/api/messages/$chatUsername")
                    .header("Authorization", "Bearer $token").build()).execute()
                val type = object : TypeToken<List<MsgItem>>() {}.type
                messages = gson.fromJson(msgResp.body?.string() ?: "[]", type)
            } catch (e: Exception) { }
            isLoading = false
        }
    }

    LaunchedEffect(token) {
        if (token.isEmpty()) return@LaunchedEffect
        wsManager.onMessage { sender, text, timeVal ->
            val timeStr = if (timeVal > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeVal * 1000)) else "now"
            if (sender == myUsername) {
                messages = messages.map { msg ->
                    if (msg.from == myUsername && msg.text == text && msg.time == "pending") msg.copy(time = timeStr)
                    else msg
                }
            } else {
                messages = messages + MsgItem(sender, myUsername, text, timeStr, "received")
            }
        }
        wsManager.onStatus { wsStatus = it }
        wsManager.connect(myUsername, token)
    }


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty()) return
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages = messages + MsgItem(myUsername, chatUsername, text, now, "pending")
        inputText = ""
        wsManager.send("message", text, chatUsername)
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        Text(chatName, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(wsStatus.ifEmpty { "online" }, color = if (wsStatus.startsWith("error")) Error else Primary, fontSize = 11.sp)
                    }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Videocam, "video", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Call, "call", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    IconButton(onClick = onProfileClick) { Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                }
            }

            if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            else {
                LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, contentPadding = PaddingValues(bottom = 72.dp)) {
                    item { Spacer(Modifier.height(16.dp)) }
                    items(messages) { msg ->
                        val isMine = msg.from == myUsername
                        MessageBubble(msg.text, msg.time.takeLast(8), msg.from == myUsername)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            // Input
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAttachSheet = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Add, "add", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                        BasicTextField(value = inputText, onValueChange = { inputText = it }, singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp), cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) Text("Message", color = OnSurfaceVariant, fontSize = 14.sp)
                                innerTextField()
                            })
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.AttachFile, "attach", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
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
                    IconButton(onClick = { showForward = false }) { Icon(Icons.Filled.Close, "close", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
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
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding()) {
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
        
        // Message action menu
        if (selectedMessage != null) {
            // Backdrop
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { selectedMessage = null; showDeleteSub = false })
            
            // Actions menu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                // Reaction row
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
                        Text(emoji, fontSize = 24.sp, modifier = Modifier.clickable { selectedMessage = null })
                    }
                }
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f))
                // Actions
                MenuAction(Icons.Filled.Reply, "Ответить") { selectedMessage = null }
                MenuAction(Icons.Filled.ContentCopy, "Копировать") { selectedMessage = null }
                MenuAction(Icons.Filled.Forward, "Переслать") { showForward = true }
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f))
                // Delete
                Column {
                    Row(Modifier.fillMaxWidth().clickable { showDeleteSub = !showDeleteSub }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, "delete", tint = Error, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Удалить", color = Error, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Icon(if (showDeleteSub) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, "expand", tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    if (showDeleteSub) {
                        MenuAction(null, "Удалить у меня", indent = true) { selectedMessage = null; showDeleteSub = false }
                        MenuAction(null, "Удалить у всех", textColor = Error, indent = true) { selectedMessage = null; showDeleteSub = false }
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
                            Icon(Icons.Filled.EmojiEmotions, "emoji", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
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

@Composable
fun MessageBubble(text: String, time: String, isMine: Boolean, onClick: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        Surface(Modifier.widthIn(max = 340.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick!!) else Modifier), shape = if (isMine) RoundedCornerShape(20,20,4,20) else RoundedCornerShape(20,20,20,4), color = if (isMine) PrimaryContainer else SecondaryContainer) {
            Text(text, color = if (isMine) OnPrimaryContainer else OnSurface, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
        Text(time, color = OnSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp))
    }
}
