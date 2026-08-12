package com.feder.compose

import android.os.Bundle
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.feder.compose.ui.theme.*
import com.feder.compose.ui.theme.LocalDarkTheme
import com.feder.compose.ui.theme.updateThemeColors
import com.feder.compose.ui.theme.ThemeController
import com.feder.compose.ui.screen.ContactsScreen
import com.feder.compose.ui.screen.SettingsScreen
import com.feder.compose.ui.screen.ContactProfileScreen
import com.feder.compose.ui.screen.ChatScreen
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
    @SerializedName("last_seen") val lastSeen: Long = 0,
    @SerializedName("last_message") val lastMessage: String? = null,
    val timestamp: String? = null
)

fun formatTimestamp(timestamp: String): String {
    return try {
        val parts = timestamp.split(" ")
        if (parts.size >= 2) {
            val time = parts[1].split(":")
            if (time.size >= 2) {
                val hour = time[0].toInt()
                val minute = time[1]
                val ampm = if (hour >= 12) "PM" else "AM"
                val hour12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                "$hour12:$minute $ampm"
            } else timestamp.takeLast(8)
        } else timestamp.takeLast(8)
    } catch (e: Exception) { timestamp.takeLast(8) }
}

class ChatViewModel : ViewModel() {
    var selectedChat by mutableStateOf<String?>(null)
    var selectedProfile by mutableStateOf<String?>(null)
    private val client = OkHttpClient()
    private val gson = Gson()
    private val server = "http://2.26.71.102:8002"
    var token by mutableStateOf("")
    var chats by mutableStateOf<List<ChatItem>>(emptyList())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var selectedTab by mutableIntStateOf(0)
    var showStories by mutableStateOf(false)
    var isSearchVisible by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    
    val filteredChats: List<ChatItem>
        get() = if (searchQuery.isEmpty()) chats
                else chats.filter { it.name.contains(searchQuery, ignoreCase = true) }
    
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
            } catch (e: Exception) { error = "Сервер недоступен"; isLoading = false }
        }
    }
    private fun loadChats() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url("$server/api/chat_settings/all?me=demo").header("Authorization", "Bearer $token").build()
                val response = client.newCall(request).execute()
                val json = response.body?.string() ?: "[]"
                val type = object : TypeToken<List<ChatItem>>() {}.type
                chats = gson.fromJson(json, type)
                isLoading = false
            } catch (e: Exception) { error = "Ошибка загрузки"; isLoading = false }
        }
    }
    fun refresh() { isLoading = true; error = null; if (token.isEmpty()) loginAndLoad() else loadChats() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Прозрачные бары для Android 11+
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            setContent {
        val prefs = this@MainActivity.getSharedPreferences("feder_theme", Context.MODE_PRIVATE)
        var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }
        CompositionLocalProvider(
            LocalDarkTheme provides ThemeController(
                isDark = isDarkMode,
                onToggle = { val newValue = !isDarkMode; isDarkMode = newValue; com.feder.compose.ui.theme.updateThemeColors(newValue); prefs.edit().putBoolean("dark_mode", newValue).apply() }
            )
        ) {
            FederTheme(darkTheme = isDarkMode) {
                FederApp()
            }
        }
    }
        }
        catch (e: Exception) { Toast.makeText(this, "Ошибка запуска", Toast.LENGTH_LONG).show() }
    }
}

@Composable
fun FederApp() {
    val viewModel: ChatViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(viewModel.error) { viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    
    // Если открыт чат или настройки — показываем без шапки
    if (viewModel.selectedChat != null || viewModel.selectedTab == 1 || viewModel.selectedTab == 3 || viewModel.selectedProfile != null) {
        Box(Modifier.fillMaxSize().background(Background)) {
            when {
                viewModel.selectedChat != null -> ChatScreen(
                    chatName = viewModel.chats.find { it.username == viewModel.selectedChat }?.name ?: "",
                    chatUsername = viewModel.selectedChat ?: "",
                    myUsername = "demo",
                    token = viewModel.token,
                    avatarUrl = viewModel.chats.find { it.username == viewModel.selectedChat }?.avatarUrl,
                    lastSeen = viewModel.chats.find { it.username == viewModel.selectedChat }?.lastSeen ?: 0,
                    isOnline = viewModel.chats.find { it.username == viewModel.selectedChat }?.online ?: false,
                    onBack = { viewModel.selectedChat = null },
                    onProfileClick = { viewModel.selectedProfile = viewModel.selectedChat; viewModel.selectedChat = null }
                )
                viewModel.selectedTab == 1 -> ContactsScreen(onBack = { viewModel.selectedTab = 0 })
                viewModel.selectedTab == 3 -> SettingsScreen(onBack = { viewModel.selectedTab = 0 }, avatarUrl = viewModel.chats.find { it.username == "demo" }?.avatarUrl, username = "Demo")
                viewModel.selectedProfile != null -> ContactProfileScreen(
                    contactName = viewModel.selectedProfile ?: "",
                    avatarUrl = viewModel.chats.find { it.username == viewModel.selectedProfile }?.avatarUrl,
                    phone = "",
                    bio = "",
                    birthday = "",
                    onBack = { viewModel.selectedProfile = null; viewModel.selectedChat = viewModel.selectedProfile }
                )
            }
        }
        return
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Surface).padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.showStories = !viewModel.showStories }) {
                    if (viewModel.showStories) {
                        Icon(Icons.Filled.Close, "close", tint = Primary, modifier = Modifier.size(24.dp))
                    } else {
                        Box(Modifier.width(56.dp).height(36.dp)) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).align(Alignment.CenterStart).padding(1.dp)) {
                                AsyncImage(model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDuNxtFN8QgoFWaxAdth_nymXAojX3zs2F0phj38lCAALFXLlHR5wQkQHvvp7Ijs8cn07pMKAzpwJ3PY71kyLh9bzJi7D5Ui4Khgzia777qiyZaLIVwnTv_BRYuyYuCgulf3SX7K7ie_n2uxGSLB7QXDX7bo8saxst0_Fy0oB_KJL6LjwtvW3iGzJL4YLB1RkW0q67Zad0NVZGVc6Ogk2LkB7EYA1NXjB8n2f1uaYP8ben5ngMB5NmZWr9TKV8qoSW57_JAukaw59i8", contentDescription = "Alex", modifier = Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).align(Alignment.Center).padding(1.dp)) {
                                AsyncImage(model = "https://lh3.googleusercontent.com/aida-public/AB6AXuClWvgFAouTnhqQbweQ49FaJbyVJwawq-7pKz5bGAbh_cBROtSacwRzHmKE2ay5Mlsh-C6jmlcq6A7eoQ3yfEJrvQ1gO5wh7JG864dfm_Wxjks8VcubZBg-lBE4t1lYSgBFJgjenpbvcTMU4joROW0PVnbn2tpVF2gsOTDOcLAMwM4bMLrmCAOlVEVN8SCCMrEqKKyxvb4gBGcxW8oOo058zEpnS9iYXjpeFmyG8wzL1UgwiloSRQkxbNzHb2bRx9Hf2rm9fOhGvVao", contentDescription = "Jordan", modifier = Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).align(Alignment.CenterEnd).padding(1.dp)) {
                                AsyncImage(model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBy0v2YVVuf-SeoVYPWBeY2-ffFo7RPJHmIeSvquaMFgx9xfUY5adDAjRoJ6pICNqrGDDcxEz9lQg_gJdsfa9QjtvcxUfybQwwuOSL9a7IbO5OPp7TKyr4G1_ZkEq6UdOnulqhOqK63zAg0DC-REgSGHX84d7_4UTs8rT6mXAqX4KFLFcBZ9GnoH5KMdVsLos1SAG9vvLincN0XIVBEXBWsMu5lUj6zSiTxDAJI5hxk4TS8Fx8z6994xBJJ6pYtbBIXFkWtGWX8ppJ6", contentDescription = "Riley", modifier = Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Feder", color = Primary, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    viewModel.isSearchVisible = !viewModel.isSearchVisible
                    if (!viewModel.isSearchVisible) viewModel.searchQuery = ""
                }) {
                    Icon(Icons.Filled.Search, "search", tint = Primary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { /* меню */ }) {
                    Icon(Icons.Filled.MoreVert, "menu", tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Surface).align(Alignment.TopCenter))
            when {
                viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.error!!, color = Error, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Повторить", color = OnPrimary) }
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 64.dp, bottom = 80.dp)) {
                        // Поиск — появляется по нажатию на лупу
                        item {
                            AnimatedVisibility(
                                visible = viewModel.isSearchVisible,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 8.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = SurfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.height(40.dp).padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Search, "search", tint = Outline, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        BasicTextField(
                                            value = viewModel.searchQuery,
                                            onValueChange = { viewModel.searchQuery = it },
                                            singleLine = true,
                                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                                            cursorBrush = SolidColor(Primary),
                                            modifier = Modifier.weight(1f),
                                            decorationBox = { innerTextField ->
                                                Box {
                                                    if (viewModel.searchQuery.isEmpty()) {
                                                        Text("Search chats...", color = Outline, fontSize = 14.sp)
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
            // Stories Block
            item {
            AnimatedVisibility(
                visible = viewModel.showStories && viewModel.selectedTab == 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // My Story
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(68.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest).border(2.dp, MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("My story", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // Contact stories
                            listOf(
                                "Alex" to "https://lh3.googleusercontent.com/aida-public/AB6AXuB-7OGxpXvSOENmBkDwACF_n7xv-wYr4cX1No9LRD5MPy6AnJEWq62jhZb2V3J7qA2I5_v00YN5KMQ-ecv_ZeY2hOj1Pk9ZoPf0K9SNLp8BNF3e_YNJRQ2uVv_5S0AkYpPZMHBWqvHW0rZMXL1BaFBtOL3VrBQlSmli6O5gMDjAliZa58m6J1087jR9qfqKTzd4-r2MzHhwf8ybZ9gVp4pqZjUmOxXXjlmJ9LZ93MR7c3iF8v1lXORUheBe5EgGYEXNXwJUzZlqYF4",
                                "Elena" to "https://lh3.googleusercontent.com/aida-public/AB6AXuCzdntS3OSTjRCd_WonMXkD6f7dnsy_jKn-FWZd2SdCPn88fJg6oexJDcK-FH313KXdTdALokEf042Q-_WsWp4FgknGk4DGyDUkLuLPUaDhrCTAsAxAl39H8dTuEFj6QWAXSxtIbM898_1igDHAKscdd0lHmL6GJmu59Ui1_7yG0gOKP6zQxqh5z0Mqo_YQmNPYA3iYxJ8hRgNWQVDE_wyHEurVt2r-96oZhFFZhf4gUwgiXKk4WBqkgRcH0swe4sIkZ1A9FvLNpbU",
                                "Marcus" to "https://lh3.googleusercontent.com/aida-public/AB6AXuBnXXSStn8X5F3jR3jIhUKSH-ZxwlSP9Sph9Nda13HroL4saNlBJlakIvPB9W8sRBSLRlVW7KZ-ylD_qrOqaSzuQFKM4mZEZSzDwRDGuU5vjue5Qtd90QRGQhIdVEaFcVnTpDTXsr0PELAK089usKk58JGMutSG_tjqtgOSSMFij52cxSIHxbPflq3SQsNRjRQ6WBJQ3M7pbRM87LXKaTVPJXSbo-xmyFTtFOkUMzZDsebV7GhFoqe2cuBK7D9xGOqUgAVcEs94eXY"
                            ).forEach { (name, url) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(model = url, contentDescription = name, modifier = Modifier.size(68.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                    Spacer(Modifier.height(4.dp))
                                    Text(name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
            }
            }

                        // Список чатов
                        items(viewModel.filteredChats) { chat ->
                            val avColor = try { Color(android.graphics.Color.parseColor(chat.avatarColor)) } catch (e: Exception) { Primary }
                            val lastMsg = chat.lastMessage ?: ""
                            val time = chat.timestamp ?: ""
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.selectedChat = chat.username }.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Аватар + онлайн-точка
                                Box(Modifier.size(56.dp)) {
                                    if (chat.avatarUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current).data(chat.avatarUrl).crossfade(true).build(),
                                            contentDescription = chat.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(56.dp).clip(CircleShape)
                                        )
                                    } else if (chat.username == "saved_messages" || chat.name == "Saved Messages") {
                                        Box(Modifier.size(56.dp).clip(CircleShape).background(avColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Bookmarks, "saved", tint = avColor, modifier = Modifier.size(28.dp))
                                        }
                                    } else {
                                        Box(Modifier.size(56.dp).clip(CircleShape).background(avColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                            Text(chat.name.take(1).uppercase(), color = avColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (chat.online && chat.username != "demo") {
                                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF41B35D)).align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp))
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(chat.name, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        if (time.isNotEmpty()) {
                                            Text(formatTimestamp(time), color = if (chat.unread > 0) Primary else OnSurfaceVariant, fontSize = 12.sp)
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        if (lastMsg.isNotEmpty()) {
                                            Text(lastMsg, color = if (chat.unread > 0) OnSurface else Secondary, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                        }
                                        if (chat.unread > 0) {
                                            Box(Modifier.size(22.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                                                Text(chat.unread.toString(), color = OnPrimaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }

            // Bottom menu overlay
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp).navigationBarsPadding().padding(bottom = 8.dp)) {
                Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 4.dp) {
                    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        listOf("Chats" to Icons.Filled.ChatBubble, "Contacts" to Icons.Filled.Contacts, "Discovery" to Icons.Filled.Explore, "Settings" to Icons.Outlined.Settings).forEachIndexed { i, (label, icon) ->
                            val selected = viewModel.selectedTab == i
                            Column(Modifier.weight(1f).clickable { viewModel.selectedTab = i }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, label, tint = if (selected) Primary else OnSurfaceVariant, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.W500, color = if (selected) Primary else OnSurfaceVariant)
                            }
                        }
                    }
                }
        }
            }

}
    }
 
