package com.feder.compose
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke

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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feder.compose.data.FederDatabase
import com.feder.compose.repository.ChatRepository
import kotlinx.coroutines.launch
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
                val hour = time[0].padStart(2, '0')
                val minute = time[1]
                "$hour:$minute"
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
    private var database: FederDatabase? = null
    var repository: ChatRepository? = null
    
    fun initDatabase(context: android.content.Context) {
        if (database == null) {
            database = FederDatabase.getInstance(context)
            repository = ChatRepository(database!!.messageDao(), database!!.chatDao())
        }
    }
    var token by mutableStateOf("")
    var chats by mutableStateOf<List<ChatItem>>(emptyList())
    var isLoading by mutableStateOf(true)
    var isRefreshing by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var selectedTab by mutableIntStateOf(0)
    var showStories by mutableStateOf(false)
    var isSearchVisible by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    
    val filteredChats: List<ChatItem>
        get() = if (searchQuery.isEmpty()) chats
                else chats.filter { it.name.contains(searchQuery, ignoreCase = true) }
    
    init { 
        loginAndLoad()
    }

    var wsManager: WebSocketManager? = null

    private fun connectWebSocket() {
        wsManager = WebSocketManager(serverUrl = "2.26.71.102", port = 8002)
        val ws = wsManager!!
        ws.onSend { toUser, msgText ->
            chats = chats.map { chat ->
                if (chat.username == toUser) {
                    chat.copy(
                        lastMessage = msgText,
                        timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                    )
                } else chat
            }
            chats = chats.sortedByDescending { it.timestamp }
            // Сохраняем отправленное сообщение в Room
            viewModelScope.launch {
                repository?.saveMessage(
                    com.feder.compose.data.entity.MessageEntity(
                        id = System.currentTimeMillis(),
                        fromUser = "demo",
                        toUser = toUser,
                        text = msgText,
                        timeVal = System.currentTimeMillis() / 1000,
                        isRead = false
                    )
                )
                repository?.updateLastMessage(toUser, msgText, System.currentTimeMillis() / 1000)
            }
        }
        ws.onMessage { sender, msgText, timeVal, msgId ->
            // Обновляем список чатов при получении сообщения
            val updatedChats = chats.map { chat ->
                if (chat.username == sender) {
                    chat.copy(
                        lastMessage = msgText,
                        timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(timeVal * 1000)),
                        unread = if (selectedChat != sender) chat.unread + 1 else chat.unread
                    )
                } else chat
            }
            chats = updatedChats.sortedByDescending { it.timestamp }
            // Сохраняем сообщение в Room
            viewModelScope.launch {
                repository?.let { repo ->
                    repo.saveMessage(
                        com.feder.compose.data.entity.MessageEntity(
                            id = msgId.toLong(),
                            fromUser = sender,
                            toUser = "demo",
                            text = msgText,
                            timeVal = timeVal,
                            isRead = selectedChat == sender
                        )
                    )
                    repo.updateLastMessage(sender, msgText, timeVal)
                }
            }
        }
        ws.connect("demo", token)
    }
    fun loginAndLoad() {
        viewModelScope.launch {
            // 1. Загружаем из Room (работает оффлайн)
            repository?.let { repo ->
                val cachedChats = repo.getChats()
                if (cachedChats.isNotEmpty()) {
                    chats = cachedChats.map { entity ->
                        ChatItem(
                            username = entity.username,
                            name = entity.name,
                            unread = entity.unread,
                            avatarColor = entity.avatarColor ?: "#FF6B6B",
                            avatarUrl = entity.avatarUrl,
                            online = entity.online,
                            isMuted = entity.isMuted,
                            lastSeen = entity.lastSeen ?: 0,
                            lastMessage = entity.lastMessage,
                            timestamp = entity.lastTime?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(it)) }
                        )
                    }
                    isLoading = false
                }
            }
            // 2. Пробуем обновить с сервера
            try {
                withContext(Dispatchers.IO) {
                    val json = gson.toJson(LoginRequest("demo", "demo"))
                    val body = json.toRequestBody("application/json".toMediaType())
                    val request = Request.Builder().url("$server/api/login").post(body).build()
                    val response = client.newCall(request).execute()
                    token = gson.fromJson(response.body?.string(), LoginResponse::class.java).accessToken
                }
                loadChats()
                connectWebSocket()
            } catch (e: Exception) { 
                if (chats.isEmpty()) error = "Сервер недоступен"
                isLoading = false 
            }
        }
    }
    private fun loadChats() {
        viewModelScope.launch {
            // 1. Сначала загружаем из Room (мгновенно)
            repository?.let { repo ->
                val cachedChats = repo.getChats()
                if (cachedChats.isNotEmpty()) {
                    chats = cachedChats.map { entity ->
                        ChatItem(
                            username = entity.username,
                            name = entity.name,
                            unread = entity.unread,
                            avatarColor = entity.avatarColor ?: "#FF6B6B",
                            avatarUrl = entity.avatarUrl,
                            online = entity.online,
                            isMuted = entity.isMuted,
                            lastSeen = entity.lastSeen ?: 0,
                            lastMessage = entity.lastMessage,
                            timestamp = entity.lastTime?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(it)) }
                        )
                    }
                    isLoading = false
                }
            }
            try {
                withContext(Dispatchers.IO) {
                val request = Request.Builder().url("$server/api/chat_settings/all?me=demo").header("Authorization", "Bearer $token").build()
                val response = client.newCall(request).execute()
                val json = response.body?.string() ?: "[]"
                val type = object : TypeToken<List<ChatItem>>() {}.type
                chats = gson.fromJson(json, type)
                }
                // Сохраняем в Room
                repository?.let { repo ->
                    val chatEntities = chats.map { chat ->
                        com.feder.compose.data.entity.ChatEntity(
                            username = chat.username,
                            name = chat.name,
                            avatarUrl = chat.avatarUrl,
                            avatarColor = chat.avatarColor,
                            lastMessage = chat.lastMessage,
                            lastTime = try { chat.timestamp?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(it)?.time } } catch (e: Exception) { null },
                            unread = chat.unread,
                            isMuted = chat.isMuted,
                            online = chat.online,
                            lastSeen = chat.lastSeen
                        )
                    }
                    repo.saveChats(chatEntities)
                }
                isLoading = false
            } catch (e: Exception) { error = "Ошибка загрузки"; isLoading = false }
        }
    }
    fun refresh() { isLoading = true; error = null; if (token.isEmpty()) loginAndLoad() else loadChats() }
    fun pullRefresh() { isRefreshing = true; if (token.isEmpty()) loginAndLoad() else loadChats(); isRefreshing = false }
    fun markChatRead(username: String) {
        viewModelScope.launch {
            repository?.markRead(username)
            // Отправляем read на сервер через HTTP
            try {
                withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("$server/api/mark_read/$username")
                        .header("Authorization", "Bearer $token")
                        .post("".toRequestBody("application/json".toMediaType()))
                        .build()
                    client.newCall(request).execute().close()
                }
            } catch (_: Exception) { }
            chats = chats.map { chat ->
                if (chat.username == username) chat.copy(unread = 0) else chat
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
        window.statusBarColor = android.graphics.Color.parseColor("#131313")
            // Прозрачные бары для Android 11+
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
    LaunchedEffect(Unit) { viewModel.initDatabase(context) }
    LaunchedEffect(viewModel.error) { viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    
    // Если открыт чат или настройки — показываем без шапки
    if (viewModel.selectedChat != null || viewModel.selectedProfile != null) {
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
                    allChats = viewModel.chats,
                    wsManager = viewModel.wsManager,
                    repository = viewModel.repository,
                    onBack = { viewModel.selectedChat = null },
                    onProfileClick = { viewModel.selectedProfile = viewModel.selectedChat; viewModel.selectedChat = null }
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
                if (viewModel.selectedTab == 0) {
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
                }
                Spacer(Modifier.width(12.dp))
                Text(if (viewModel.selectedTab == 1) "Contacts" else "Feder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    viewModel.isSearchVisible = !viewModel.isSearchVisible
                    if (!viewModel.isSearchVisible) viewModel.searchQuery = ""
                }) {
                    Icon(Icons.Filled.Search, "search", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Box {
                    var showMoreMenu by remember { mutableStateOf(false) }
                    Icon(Icons.Filled.MoreVert, "menu", tint = Color.White, modifier = Modifier.padding(start = 16.dp).size(24.dp).clickable { showMoreMenu = true })
                    if (showMoreMenu) {
                        Popup(alignment = Alignment.TopEnd, onDismissRequest = { showMoreMenu = false }, properties = PopupProperties(focusable = true)) {
                            Surface(modifier = Modifier.padding(top = 8.dp, end = 16.dp).width(IntrinsicSize.Max), shape = RoundedCornerShape(16.dp), color = SurfaceContainerHigh, shadowElevation = 8.dp, border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    // Если выбраны Contacts или Settings — показываем их
                    if (viewModel.selectedTab == 1) {
                        ContactsScreen(contacts = viewModel.chats.filter { it.username != "demo" && it.username != "123" }, onBack = { viewModel.selectedTab = 0 })
                    } else if (viewModel.selectedTab == 3) {
                        SettingsScreen(onBack = { viewModel.selectedTab = 0 }, avatarUrl = viewModel.chats.find { it.username == "demo" }?.avatarUrl, username = "Demo")
                    } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(
                                connection = object : NestedScrollConnection {
                                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                        if (available.y < -100f && source == NestedScrollSource.Drag) {
                                            viewModel.showStories = true; if (available.y < -300f) viewModel.pullRefresh()
                                        }
                                        return Offset.Zero
                                    }
                                }
                            ),
                        contentPadding = PaddingValues(bottom = 72.dp)
                    ) {
                        item { Spacer(Modifier.height(48.dp)) }
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
                                "Alex" to "http://2.26.71.102:8002/avatars/avatar_2.jpg",
                                "Elena" to "http://2.26.71.102:8002/avatars/avatar_5.jpg",
                                "Marcus" to "http://2.26.71.102:8002/avatars/avatar_1.jpg"
                            ).forEach { (name, url) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).diskCachePolicy(coil.request.CachePolicy.ENABLED).memoryCachePolicy(coil.request.CachePolicy.ENABLED).build(), contentDescription = name, modifier = Modifier.size(68.dp).clip(CircleShape), contentScale = ContentScale.Crop)
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
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.selectedChat = chat.username; viewModel.markChatRead(chat.username) }.padding(horizontal = 16.dp, vertical = 12.dp),
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
                                            Text(lastMsg.take(24), color = if (chat.unread > 0) OnSurface else Secondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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

            }
            // Bottom menu overlay
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp).navigationBarsPadding().padding(bottom = 8.dp)) {
                Surface(shape = RoundedCornerShape(28.dp), color = SurfaceContainerHigh, shadowElevation = 8.dp, tonalElevation = 2.dp, border = BorderStroke(0.1.dp, Color(0xFF3A3A3A))) {
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
@Composable
fun MenuRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, text, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = OnSurface, fontSize = 14.sp)
    }
}
