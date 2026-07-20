package com.feder.compose.ui.screen

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
import android.widget.Toast
import com.feder.compose.ui.theme.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class MessageItem(
    val from_user: String,
    val to_user: String,
    val text: String,
    val timestamp: String
)

data class SendRequest(val from_user: String, val to_user: String, val text: String)

@Composable
fun ChatScreen(chatName: String, chatUsername: String, myUsername: String, onBack: () -> Unit, onProfileClick: () -> Unit = {}) {
    val client = remember { OkHttpClient() }
    val gson = remember { Gson() }
    var messages by remember { mutableStateOf<List<MessageItem>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var token by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // Загружаем сообщения и получаем токен
    LaunchedEffect(chatUsername) {
        withContext(Dispatchers.IO) {
            try {
                // Логинимся
                val loginJson = gson.toJson(LoginRequest(myUsername, myUsername))
                val loginBody = loginJson.toRequestBody("application/json".toMediaType())
                val loginRequest = Request.Builder().url("http://2.26.71.102:8002/api/login").post(loginBody).build()
                val loginResponse = client.newCall(loginRequest).execute()
                token = gson.fromJson(loginResponse.body?.string(), LoginResponse::class.java).accessToken
                
                // Загружаем сообщения
                loadMessages(client, gson, token, chatUsername) { messages = it }
            } catch (e: Exception) {
                messages = listOf(
                    MessageItem(chatUsername, myUsername, "Hey! Did you have a chance to look at the latest UI proposal?", "10:42 AM"),
                    MessageItem(myUsername, chatUsername, "Just finished reviewing it. Looking solid!", "10:45 AM"),
                    MessageItem(chatUsername, myUsername, "Awesome! Should we sync at 2 PM?", "10:46 AM")
                )
            }
            isLoading = false
        }
    }
    
    // Автоскролл вниз
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    
    // Отправка сообщения
    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty()) return
        
        // Добавляем сообщение сразу в список (оптимистично)
        val newMsg = MessageItem(myUsername, chatUsername, text, "now")
        messages = messages + newMsg
        inputText = ""
        
        // Отправляем на сервер
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sendJson = gson.toJson(SendRequest(myUsername, chatUsername, text))
                val body = sendJson.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("http://2.26.71.102:8002/api/chat/send")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()
                client.newCall(request).execute()
                // Обновляем сообщения с сервера
                loadMessages(client, gson, token, chatUsername) { messages = it }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка отправки: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Header
        Surface(color = Surface, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "back", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant, CircleShape)) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text(chatName.take(1).uppercase(), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(chatName, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("online", color = Primary, fontSize = 11.sp)
                }
                IconButton(onClick = { }) { Icon(Icons.Filled.Videocam, "video", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = { }) { Icon(Icons.Filled.Call, "call", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = onProfileClick) { Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
            }
        }
        
        // Messages
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { Spacer(Modifier.height(16.dp)) }
                items(messages) { msg ->
                    val isMine = msg.from_user == myUsername
                    MessageBubble(text = msg.text, time = msg.timestamp.takeLast(8), isMine = isMine)
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
        
        // Input
        Surface(color = Surface, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(24.dp), color = SurfaceContainerHigh) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Add, "add", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) Text("Message", color = OnSurfaceVariant, fontSize = 14.sp)
                                innerTextField()
                            }
                        )
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.AttachFile, "attach", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryContainer).clickable { sendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (inputText.isEmpty()) Icons.Filled.Mic else Icons.Filled.Send,
                        "send", tint = OnPrimaryContainer, modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

suspend fun loadMessages(client: OkHttpClient, gson: Gson, token: String, chatUsername: String, onResult: (List<MessageItem>) -> Unit) {
    try {
        val request = Request.Builder()
            .url("http://2.26.71.102:8002/api/messages/$chatUsername")
            .header("Authorization", "Bearer $token")
            .build()
        val response = client.newCall(request).execute()
        val json = response.body?.string() ?: "[]"
        val type = object : TypeToken<List<MessageItem>>() {}.type
        onResult(gson.fromJson(json, type))
    } catch (e: Exception) {
        // Оставляем текущие сообщения
    }
}

@Composable
fun MessageBubble(text: String, time: String, isMine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 340.dp),
            shape = if (isMine) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                    else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            color = if (isMine) PrimaryContainer else SecondaryContainer
        ) {
            Text(
                text,
                color = if (isMine) OnPrimaryContainer else OnSurface,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        Text(time, color = OnSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp))
    }
}
