package com.feder.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

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
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val online: Boolean = false,
    @SerializedName("is_muted") val isMuted: Boolean = false,
    @SerializedName("last_message") val lastMessage: String = "",
    val timestamp: String = ""
)

object FederColors {
    val Background = Color(0xFF131313)
    val Surface = Color(0xFF131313)
    val SurfaceContainerLow = Color(0xFF1C1B1B)
    val SurfaceContainerHigh = Color(0xFF2A2A2A)
    val Primary = Color(0xFFA1C9FF)
    val PrimaryContainer = Color(0xFF339DFF)
    val OnPrimary = Color(0xFF00325A)
    val OnPrimaryContainer = Color(0xFF00335C)
    val Secondary = Color(0xFFC8C6C5)
    val OnSurface = Color(0xFFE5E2E1)
    val OnSurfaceVariant = Color(0xFFC0C7D4)
    val Outline = Color(0xFF8A919E)
    val OutlineVariant = Color(0xFF404752)
    val OnlineGreen = Color(0xFF41B35D)
    val Muted = Color(0xFFFFB4AB)
}

class ChatViewModel : ViewModel() {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val server = "http://2.26.71.102:8002"
    
    var token by mutableStateOf("")
    var chats by mutableStateOf<List<ChatItem>>(emptyList())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var selectedTab by mutableIntStateOf(0)
    
    init { loginAndLoad() }
    
    fun loginAndLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = gson.toJson(LoginRequest("demo", "demo"))
                val body = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("$server/api/login").post(body).build()
                val response = client.newCall(request).execute()
                token = gson.fromJson(response.body?.string(), LoginResponse::class.java).accessToken
                loadChats()
            } catch (e: Exception) {
                error = "Сервер недоступен"
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
                error = "Ошибка загрузки"
                isLoading = false
            }
        }
    }
    
    fun refresh() { isLoading = true; error = null; loginAndLoad() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                MaterialTheme(colorScheme = darkColorScheme(
                    primary = FederColors.Primary,
                    background = FederColors.Background,
                    surface = FederColors.Surface,
                    onSurface = FederColors.OnSurface,
                )) {
                    FederApp()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

@Composable
fun FederApp() {
    val viewModel: ChatViewModel = viewModel()
    
    Scaffold(
        containerColor = FederColors.Background,
        topBar = {
            Surface(color = FederColors.Surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(FederColors.SurfaceContainerLow).border(1.dp, FederColors.OutlineVariant, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, "avatar", tint = FederColors.Primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Messenger", color = FederColors.Primary, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { }) { Icon(Icons.Filled.Search, "search", tint = FederColors.Primary) }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = FederColors.Surface) {
                val tabs = listOf("Chats" to Icons.Filled.Chat, "Stories" to Icons.Outlined.AutoAwesome, "Contacts" to Icons.Outlined.Contacts, "Settings" to Icons.Outlined.Settings)
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, label, modifier = Modifier.size(24.dp)) },
                        label = { Text(label, fontSize = 11.sp) },
                        selected = viewModel.selectedTab == index,
                        onClick = { viewModel.selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = FederColors.Primary, selectedTextColor = FederColors.Primary, unselectedIconColor = FederColors.OnSurfaceVariant, unselectedTextColor = FederColors.OnSurfaceVariant)
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FederColors.Primary) }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.error!!, color = Color(0xFFFFB4AB), fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = FederColors.Primary)) { Text("Повторить", color = FederColors.OnPrimary) }
                    }
                }
                else -> ChatListScreen(viewModel.chats)
            }
        }
    }
}

@Composable
fun ChatListScreen(chats: List<ChatItem>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
        item {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(20.dp), color = FederColors.SurfaceContainerHigh) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, "search", tint = FederColors.Outline, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Search chats...", color = FederColors.Outline, fontSize = 14.sp)
                }
            }
        }
        items(chats) { chat -> ChatRow(chat) }
    }
}

@Composable
fun ChatRow(chat: ChatItem) {
    val avatarColor = try { Color(android.graphics.Color.parseColor(chat.avatarColor)) } catch (e: Exception) { FederColors.Primary }
    
    Surface(Modifier.fillMaxWidth().clickable { }, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp)) {
                if (!chat.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(chat.avatarUrl).crossfade(true).build(),
                        contentDescription = chat.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                    )
                } else {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(avatarColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text(chat.name.take(1).uppercase(), color = avatarColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (chat.online) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(FederColors.OnlineGreen).align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).border(2.dp, FederColors.Background, CircleShape))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(chat.name, color = FederColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (chat.isMuted) {
                            Icon(Icons.Filled.VolumeOff, "muted", tint = FederColors.Muted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(chat.timestamp.ifEmpty { "" }, color = if (chat.unread > 0) FederColors.Primary else FederColors.OnSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(chat.lastMessage.ifEmpty { "Нет сообщений" }, color = if (chat.unread > 0) FederColors.OnSurface else FederColors.Secondary, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    if (chat.unread > 0) {
                        Box(Modifier.size(20.dp).clip(CircleShape).background(FederColors.PrimaryContainer), contentAlignment = Alignment.Center) {
                            Text(chat.unread.toString(), color = FederColors.OnPrimaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider(color = FederColors.SurfaceContainerHigh, modifier = Modifier.padding(start = 88.dp, end = 16.dp))
}
