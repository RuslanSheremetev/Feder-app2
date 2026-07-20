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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feder.compose.ui.theme.*
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
    val online: Boolean = false,
    @SerializedName("is_muted") val isMuted: Boolean = false,
    @SerializedName("last_message") val lastMessage: String = "",
    val timestamp: String = ""
)

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
            } catch (e: Exception) { error = "Ошибка: ${e.message}"; isLoading = false }
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
            } catch (e: Exception) { error = "Ошибка: ${e.message}"; isLoading = false }
        }
    }
    
    fun refresh() { isLoading = true; error = null; if (token.isEmpty()) loginAndLoad() else loadChats() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { setContent { FederTheme { FederApp() } } }
        catch (e: Exception) { Toast.makeText(this, "КРАШ: ${e.message}", Toast.LENGTH_LONG).show() }
    }
}

@Composable
fun FederApp() {
    val viewModel: ChatViewModel = viewModel()
    val context = LocalContext.current
    
    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            Surface(color = Surface) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainerLow), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, "avatar", tint = Primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Messenger", color = Primary, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Surface) {
                listOf("Chats" to Icons.Filled.Chat, "Stories" to Icons.Outlined.AutoAwesome, "Contacts" to Icons.Outlined.Contacts, "Settings" to Icons.Outlined.Settings).forEachIndexed { i, (l, ic) ->
                    NavigationBarItem(icon = { Icon(ic, l, modifier = Modifier.size(24.dp)) }, label = { Text(l, fontSize = 11.sp) }, selected = viewModel.selectedTab == i, onClick = { viewModel.selectedTab = i }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, unselectedIconColor = OnSurfaceVariant))
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(viewModel.error!!, color = Error) }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(viewModel.chats) { chat ->
                            // СУПЕР-МИНИМУМ: только одна строка текста
                            Text(
                                chat.name,
                                color = OnSurface,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
