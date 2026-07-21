package com.feder.compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.feder.compose.ui.theme.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class MsgItem(
    val from_user: String,
    val to_user: String,
    val text: String,
    val timestamp: String
)

data class WsMsg(
    val type: String = "",
    val text: String = "",
    val from_user: String = "",
    val time: Long = 0
)

@Composable
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, onBack: () -> Unit, onProfileClick: () -> Unit = {}) {
    var messages by remember { mutableStateOf<List<MsgItem>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var token by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val client = remember { OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build() }
    val gson = remember { Gson() }
    var ws by remember { mutableStateOf<WebSocket?>(null) }
    
    // Шаг 1: Загружаем историю через POST
    LaunchedEffect(chatUsername) {
        withContext(Dispatchers.IO) {
            try {
                val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
                val body = authJson.toRequestBody("application/json".toMediaType())
                val resp = client.newCall(Request.Builder().url("http://2.26.71.102:8002/api/login").post(body).build()).execute()
                token = JsonParser.parseString(resp.body?.string() ?: "").asJsonObject.get("access_token")?.asString ?: ""
                
                val msgResp = client.newCall(Request.Builder()
                    .url("http://2.26.71.102:8002/api/messages/$chatUsername")
                    .header("Authorization", "Bearer $token").build()).execute()
                val type = object : TypeToken<List<MsgItem>>() {}.type
                messages = gson.fromJson(msgResp.body?.string() ?: "[]", type)
            } catch (e: Exception) { }
            isLoading = false
        }
    }
    
    // Шаг 2: Подключаем WebSocket вручную через Dispatchers.Main
    LaunchedEffect(token) {
        if (token.isEmpty()) return@LaunchedEffect
        try {
            val url = "ws://2.26.71.102:8002/ws/$myUsername?token=$token"
            ws = client.newWebSocket(Request.Builder().url(url).build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    println("WS: connected")
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val msg = gson.fromJson(text, WsMsg::class.java)
                        if (msg.type == "message" && msg.text.isNotEmpty()) {
                            val sender = msg.from_user.ifEmpty { chatUsername }
                            val timeStr = if (msg.time > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.time * 1000)) else "now"
                            messages = messages + MsgItem(sender, myUsername, msg.text, timeStr)
                        }
                    } catch (e: Exception) { }
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { }
            })
        } catch (e: Exception) { }
    }
    
    DisposableEffect(Unit) {
        onDispose { ws?.close(1000, "close") }
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    
    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty()) return
        
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages = messages + MsgItem(myUsername, chatUsername, text, now)
        inputText = ""
        
        // Отправляем через WebSocket
        val json = gson.toJson(mapOf("type" to "message", "text" to text))
        ws?.send(json)
    }
    
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Surface(color = Surface, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "back", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                Box(Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant, CircleShape)) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text(chatName.take(1).uppercase(), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(chatName, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium); Text("online", color = Primary, fontSize = 11.sp) }
                IconButton(onClick = { }) { Icon(Icons.Filled.Videocam, "video", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = { }) { Icon(Icons.Filled.Call, "call", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = onProfileClick) { Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
            }
        }
        if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        else LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState) {
            item { Spacer(Modifier.height(16.dp)) }
            items(messages) { msg -> MessageBubble(msg.text, msg.timestamp.takeLast(8), msg.from_user == myUsername) }
            item { Spacer(Modifier.height(16.dp)) }
        }
        Surface(color = Surface, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(24.dp), color = SurfaceContainerHigh) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.Add, "add", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                        BasicTextField(value = inputText, onValueChange = { inputText = it }, singleLine = true, textStyle = TextStyle(color = OnSurface, fontSize = 14.sp), cursorBrush = SolidColor(Primary), modifier = Modifier.weight(1f).padding(vertical = 10.dp), decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) Text("Message", color = OnSurfaceVariant, fontSize = 14.sp); innerTextField()
                        })
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.AttachFile, "attach", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(48.dp).clip(CircleShape).background(PrimaryContainer).clickable { sendMessage() }, contentAlignment = Alignment.Center) {
                    Icon(if (inputText.isEmpty()) Icons.Filled.Mic else Icons.Filled.Send, "send", tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, time: String, isMine: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        Surface(Modifier.widthIn(max = 340.dp), shape = if (isMine) RoundedCornerShape(20,20,4,20) else RoundedCornerShape(20,20,20,4), color = if (isMine) PrimaryContainer else SecondaryContainer) {
            Text(text, color = if (isMine) OnPrimaryContainer else OnSurface, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
        Text(time, color = OnSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp))
    }
}
