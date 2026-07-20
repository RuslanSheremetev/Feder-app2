package com.feder.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// ============ DATA MODELS ============
data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val username: String
)

data class ChatItem(
    val username: String,
    val name: String,
    val unread: Int = 0,
    @SerializedName("avatar_color") val avatarColor: String = "#339dff",
    val online: Boolean = false,
    @SerializedName("last_message") val lastMessage: String = "",
    val timestamp: String = ""
)

// ============ VIEWMODEL ============
class ChatViewModel : ViewModel() {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val server = "http://2.26.71.102:8002"
    
    var token by mutableStateOf("")
    var chats by mutableStateOf<List<ChatItem>>(emptyList())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    
    init {
        loginAndLoad()
    }
    
    private fun loginAndLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = gson.toJson(LoginRequest("demo", "demo"))
                val body = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$server/api/login")
                    .post(body)
                    .build()
                
                val response = client.newCall(request).execute()
                val loginResp = gson.fromJson(response.body?.string(), LoginResponse::class.java)
                token = loginResp.accessToken
                
                loadChats()
            } catch (e: Exception) {
                error = "Ошибка подключения: ${e.message}"
                isLoading = false
            }
        }
    }
    
    private fun loadChats() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("$server/api/chat_settings/all?me=demo")
                    .header("Authorization", "Bearer $token")
                    .build()
                
                val response = client.newCall(request).execute()
                val json = response.body?.string() ?: "[]"
                val type = object : TypeToken<List<ChatItem>>() {}.type
                chats = gson.fromJson(json, type)
                isLoading = false
            } catch (e: Exception) {
                error = "Ошибка загрузки чатов: ${e.message}"
                isLoading = false
            }
        }
    }
    
    fun refresh() {
        isLoading = true
        error = null
        if (token.isEmpty()) loginAndLoad() else loadChats()
    }
}

// ============ UI ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FederTheme {
                FederApp()
            }
        }
    }
}

// Цвета Feder
private val Background = Color(0xFF131313)
private val Surface = Color(0xFF1C1B1B)
private val SurfaceVariant = Color(0xFF2A2A2A)
private val Primary = Color(0xFFA1C9FF)
private val OnSurface = Color(0xFFE5E2E1)
private val Secondary = Color(0xFFC0C7D4)
private val OnlineGreen = Color(0xFF41B35D)

@Composable
fun FederTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Primary,
            background = Background,
            surface = Surface,
            surfaceVariant = SurfaceVariant,
            onSurface = OnSurface,
            onSurfaceVariant = Secondary,
        ),
        content = content
    )
}

@Composable
fun FederApp() {
    Scaffold(
        containerColor = Background,
        topBar = { FederTopBar() },
        bottomBar = { FederBottomBar() }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ChatListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FederTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, "avatar", tint = Primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Messenger", color = Primary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, "search", tint = Primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
    )
}

@Composable
fun FederBottomBar() {
    var selected by remember { mutableIntStateOf(0) }
    val items = listOf(
        "Чаты" to Icons.Filled.Chat,
        "Stories" to Icons.Outlined.AutoAwesome,
        "Контакты" to Icons.Outlined.Contacts,
        "Настройки" to Icons.Outlined.Settings
    )
    
    NavigationBar(containerColor = Surface) {
        items.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, label) },
                label = { Text(label, fontSize = 11.sp) },
                selected = selected == index,
                onClick = { selected = index },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = Secondary,
                    unselectedTextColor = Secondary,
                    indicatorColor = Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun ChatListScreen(viewModel: ChatViewModel = viewModel()) {
    when {
        viewModel.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
        viewModel.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.error!!, color = Color(0xFFFFB4AB), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                        Text("Повторить", color = Color(0xFF00325A))
                    }
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search bar
                item {
                    SearchBar()
                }
                
                items(viewModel.chats) { chat ->
                    ChatRow(chat)
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, "search", tint = Secondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Поиск чатов...", color = Secondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChatRow(chat: ChatItem) {
    val avatarColor = try {
        Color(android.graphics.Color.parseColor(chat.avatarColor))
    } catch (e: Exception) {
        Primary
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chat.name.take(1).uppercase(),
                        color = avatarColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Online indicator
                if (chat.online) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Name and last message
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    chat.name,
                    color = OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    chat.lastMessage.ifEmpty { "Нет сообщений" },
                    color = if (chat.unread > 0) OnSurface else Secondary,
                    fontSize = 14.sp,
                    fontWeight = if (chat.unread > 0) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1
                )
            }
            
            // Unread badge
            if (chat.unread > 0) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chat.unread.toString(),
                        color = Color(0xFF00325A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
