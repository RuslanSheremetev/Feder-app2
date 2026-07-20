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

// ============ DATA ============
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

// ============ COLORS (из HTML) ============
object FederColors {
    val Background = Color(0xFF131313)
    val Surface = Color(0xFF131313)
    val SurfaceContainer = Color(0xFF201F1F)
    val SurfaceContainerLow = Color(0xFF1C1B1B)
    val SurfaceContainerHigh = Color(0xFF2A2A2A)
    val SurfaceContainerHighest = Color(0xFF353534)
    val SurfaceVariant = Color(0xFF353534)
    val SurfaceBright = Color(0xFF393939)
    val SurfaceDim = Color(0xFF131313)
    
    val Primary = Color(0xFFA1C9FF)
    val PrimaryContainer = Color(0xFF339DFF)
    val OnPrimary = Color(0xFF00325A)
    val OnPrimaryContainer = Color(0xFF00335C)
    val OnPrimaryFixed = Color(0xFF001C37)
    val OnPrimaryFixedVariant = Color(0xFF00487F)
    val PrimaryFixed = Color(0xFFD2E4FF)
    val PrimaryFixedDim = Color(0xFFA1C9FF)
    val InversePrimary = Color(0xFF0061A7)
    
    val Secondary = Color(0xFFC8C6C5)
    val SecondaryContainer = Color(0xFF474746)
    val OnSecondary = Color(0xFF303030)
    val OnSecondaryContainer = Color(0xFFB7B5B4)
    val SecondaryFixed = Color(0xFFE5E2E1)
    val SecondaryFixedDim = Color(0xFFC8C6C5)
    val OnSecondaryFixed = Color(0xFF1B1B1C)
    val OnSecondaryFixedVariant = Color(0xFF474746)
    
    val Tertiary = Color(0xFFFFB877)
    val TertiaryContainer = Color(0xFFE38100)
    val TertiaryFixed = Color(0xFFFFDCC1)
    val TertiaryFixedDim = Color(0xFFFFB877)
    val OnTertiary = Color(0xFF4C2700)
    val OnTertiaryContainer = Color(0xFF4D2800)
    val OnTertiaryFixed = Color(0xFF2E1500)
    val OnTertiaryFixedVariant = Color(0xFF6C3A00)
    
    val Error = Color(0xFFFFB4AB)
    val ErrorContainer = Color(0xFF93000A)
    val OnError = Color(0xFF690005)
    val OnErrorContainer = Color(0xFFFFDAD6)
    
    val OnSurface = Color(0xFFE5E2E1)
    val OnSurfaceVariant = Color(0xFFC0C7D4)
    val Outline = Color(0xFF8A919E)
    val OutlineVariant = Color(0xFF404752)
    val InverseSurface = Color(0xFFE5E2E1)
    val InverseOnSurface = Color(0xFF313030)
    
    val OnlineGreen = Color(0xFF41B35D)
}

// ============ VIEWMODEL ============
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
                error = "Сервер недоступен: ${e.message}"
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
                error = "Ошибка загрузки: ${e.message}"
                isLoading = false
            }
        }
    }
    
    fun refresh() { isLoading = true; error = null; loginAndLoad() }
}

// ============ MAIN ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = FederColorScheme) {
                FederApp()
            }
        }
    }
}

private val FederColorScheme = darkColorScheme(
    primary = FederColors.Primary,
    onPrimary = FederColors.OnPrimary,
    primaryContainer = FederColors.PrimaryContainer,
    onPrimaryContainer = FederColors.OnPrimaryContainer,
    secondary = FederColors.Secondary,
    onSecondary = FederColors.OnSecondary,
    secondaryContainer = FederColors.SecondaryContainer,
    onSecondaryContainer = FederColors.OnSecondaryContainer,
    tertiary = FederColors.Tertiary,
    onTertiary = FederColors.OnTertiary,
    tertiaryContainer = FederColors.TertiaryContainer,
    onTertiaryContainer = FederColors.OnTertiaryContainer,
    error = FederColors.Error,
    errorContainer = FederColors.ErrorContainer,
    onError = FederColors.OnError,
    onErrorContainer = FederColors.OnErrorContainer,
    background = FederColors.Background,
    onBackground = FederColors.OnSurface,
    surface = FederColors.Surface,
    onSurface = FederColors.OnSurface,
    surfaceVariant = FederColors.SurfaceVariant,
    onSurfaceVariant = FederColors.OnSurfaceVariant,
    outline = FederColors.Outline,
    outlineVariant = FederColors.OutlineVariant,
    inverseSurface = FederColors.InverseSurface,
    inverseOnSurface = FederColors.InverseOnSurface,
)

@Composable
fun FederApp() {
    val viewModel: ChatViewModel = viewModel()
    
    Scaffold(
        containerColor = FederColors.Background,
        topBar = {
            // Header — точно как в HTML
            Surface(
                color = FederColors.Surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar 36dp
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FederColors.SurfaceContainerLow)
                            .then(
                                Modifier.border(1.dp, FederColors.OutlineVariant, CircleShape)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            "avatar",
                            tint = FederColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Text(
                        "Messenger",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FederColors.Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Search button
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Search,
                            "search",
                            tint = FederColors.Primary
                        )
                    }
                }
            }
        },
        bottomBar = {
            // BottomNavBar — точно как в HTML
            NavigationBar(
                containerColor = FederColors.Surface,
                tonalElevation = 0.dp
            ) {
                val tabs = listOf(
                    "Chats" to Icons.Filled.Chat,
                    "Stories" to Icons.Outlined.AutoAwesome,
                    "Contacts" to Icons.Outlined.Contacts,
                    "Settings" to Icons.Outlined.Settings
                )
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                icon,
                                label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                label,
                                fontSize = 11.sp,
                                letterSpacing = 0.1.sp
                            )
                        },
                        selected = viewModel.selectedTab == index,
                        onClick = { viewModel.selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FederColors.Primary,
                            selectedTextColor = FederColors.Primary,
                            unselectedIconColor = FederColors.OnSurfaceVariant,
                            unselectedTextColor = FederColors.OnSurfaceVariant,
                            indicatorColor = FederColors.Primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                viewModel.isLoading -> LoadingScreen()
                viewModel.error != null -> ErrorScreen(viewModel.error!!, viewModel::refresh)
                else -> ChatListScreen(viewModel.chats)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FederColors.Primary)
    }
}

@Composable
fun ErrorScreen(error: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(error, color = FederColors.Error, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = FederColors.Primary)
            ) {
                Text("Повторить", color = FederColors.OnPrimary)
            }
        }
    }
}

@Composable
fun ChatListScreen(chats: List<ChatItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        // Search bar — как в HTML
        item {
            SearchBar()
        }
        
        // Chat items — как в HTML
        items(chats) { chat ->
            ChatRow(chat)
        }
    }
}

@Composable
fun SearchBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = FederColors.SurfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                "search",
                tint = FederColors.Secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Search chats...",
                color = FederColors.Outline,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ChatRow(chat: ChatItem) {
    val avatarColor = try {
        Color(android.graphics.Color.parseColor(chat.avatarColor))
    } catch (e: Exception) {
        FederColors.Primary
    }
    
    // Divider
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar 56dp с онлайн-индикатором
            Box(modifier = Modifier.size(56.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chat.name.take(1).uppercase(),
                        color = avatarColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Online dot (как в HTML)
                if (chat.online) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(FederColors.OnlineGreen)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .then(
                                Modifier.border(2.dp, FederColors.Background, CircleShape)
                            )
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Name + Last message
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Baseline
                ) {
                    Text(
                        chat.name,
                        color = FederColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        chat.timestamp.ifEmpty { "" },
                        color = if (chat.unread > 0) FederColors.Primary else FederColors.OnSurfaceVariant,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Spacer(Modifier.height(2.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chat.lastMessage.ifEmpty { "Нет сообщений" },
                        color = if (chat.unread > 0) FederColors.OnSurface else FederColors.Secondary,
                        fontSize = 14.sp,
                        fontWeight = if (chat.unread > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Unread badge
                    if (chat.unread > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(FederColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                chat.unread.toString(),
                                color = FederColors.OnPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Bottom divider
    HorizontalDivider(
        color = FederColors.SurfaceContainerHigh,
        modifier = Modifier.padding(start = 88.dp, end = 16.dp)
    )
}
