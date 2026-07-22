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
import com.feder.compose.WebSocketManager
import com.feder.compose.ui.theme.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class MsgItem(
    val from: String,
    val to: String,
    val text: String,
    val time: String,
    var status: String = "sent"
)

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

    // Загрузка истории через enqueue (асинхронно, без корутин)
    LaunchedEffect(chatUsername) {
        val authJson = gson.toJson(mapOf("username" to myUsername, "password" to myUsername))
        val body = authJson.toRequestBody("application/json".toMediaType())
        
        httpClient.newCall(Request.Builder().url("http://2.26.71.102:8002/api/login").post(body).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isLoading = false
            }

            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string() ?: ""
                token = try { JsonParser.parseString(respBody).asJsonObject.get("access_token")?.asString ?: "" } catch (e: Exception) { "" }
                
                // Загружаем историю
                httpClient.newCall(Request.Builder()
                    .url("http://2.26.71.102:8002/api/messages/$chatUsername")
                    .header("Authorization", "Bearer $token")
                    .build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        isLoading = false
                    }
                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string() ?: "[]"
                        val type = object : TypeToken<List<MsgItem>>() {}.type
                        messages = gson.fromJson(json, type)
                        isLoading = false
                    }
                })
            }
        })
    }

    // WebSocket через менеджер
    LaunchedEffect(token) {
        if (token.isEmpty()) return@LaunchedEffect
        
        wsManager.onMessage { sender, text, timeVal ->
            val timeStr = if (timeVal > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeVal * 1000)) else "now"
            // Не добавляем свои сообщения (эхо) — они уже есть
            if (sender != myUsername) {
                messages = messages + MsgItem(sender, myUsername, text, timeStr, "received")
            }
        }
        wsManager.onStatus { wsStatus = it }
        wsManager.connect(myUsername, token)
    }

    DisposableEffect(Unit) {
        onDispose { wsManager.disconnect() }
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
        else LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, contentPadding = PaddingValues(bottom = 72.dp)) {
            item { Spacer(Modifier.height(16.dp)) }
            items(messages) { msg -> MessageBubble(msg.text, msg.time.takeLast(8), msg.from == myUsername) }
            item { Spacer(Modifier.height(16.dp)) }
        }

        // Floating input like pill
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding()
        ) {
            Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
fun MessageBubble(text: String, time: String, isMine: Boolean, status: String = "") {
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        Surface(Modifier.widthIn(max = 340.dp), shape = if (isMine) RoundedCornerShape(20,20,4,20) else RoundedCornerShape(20,20,20,4), color = if (isMine) PrimaryContainer else SecondaryContainer) {
            Text(text, color = if (isMine) OnPrimaryContainer else OnSurface, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp)) {
            Text(time, color = OnSurfaceVariant, fontSize = 11.sp)
            if (isMine && status.isNotEmpty()) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = when (status) {
                        "pending" -> "✓"
                        "sent" -> "✓✓"
                        else -> ""
                    },
                    color = if (status == "sent") Primary else OnSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
