package com.feder.compose.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.*
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import com.feder.compose.ui.theme.*
import com.feder.compose.ui.theme.LocalDarkTheme

@Composable
fun SettingsScreen(onBack: () -> Unit = {}, isDarkMode: Boolean = true, onToggleTheme: (Boolean) -> Unit = {}, avatarUrl: String? = null, username: String = "Demo") {
    var showAccount by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showDataStorage by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf(username) }
    var phone by remember { mutableStateOf("") }
    var login by remember { mutableStateOf(username) }
    var birthday by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url("http://2.26.71.102:8002/api/user/demo").build()
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            val body = response.body?.string()
            if (body != null) {
                val json = JsonParser.parseString(body).asJsonObject
                displayName = json.get("name")?.asString ?: username
                phone = json.get("phone")?.asString ?: ""
                login = json.get("username")?.asString ?: username
                birthday = json.get("birthday")?.asString ?: ""
            }
        } catch (_: Exception) { }
    }

    if (showAccount) { AccountScreen(onBack = { showAccount = false }); return }
    if (showPrivacy) { PrivacyScreen(onBack = { showPrivacy = false }); return }
    if (showNotifications) { NotificationsScreen(onBack = { showNotifications = false }); return }
    if (showHelp) { HelpScreen(onBack = { showHelp = false }); return }
    if (showDataStorage) { DataStorageScreen(onBack = { showDataStorage = false }); return }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = OnSurfaceVariant)
            }
            Text("Settings", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 22.sp)
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp)) {
                Box(Modifier.size(96.dp).clip(CircleShape).background(SurfaceContainerLow).padding(4.dp)) {
                    Box(Modifier.size(88.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, "avatar", tint = Primary, modifier = Modifier.size(48.dp))
                    }
                }
                Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF41B35D)).border(4.dp, Background, CircleShape).align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(displayName, color = OnSurface, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text("$phone  |  @$login", color = Secondary, fontSize = 14.sp)
            if (birthday.isNotEmpty()) { Text(birthday, color = Secondary, fontSize = 14.sp) }
            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.clickable { }, shape = RoundedCornerShape(20.dp), color = Primary) {
                Text("Edit Profile", color = OnPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
        SettingsCard(Icons.Filled.Person, "Account", "Security, change number", onClick = { showAccount = true })
        SettingsCard(Icons.Filled.Lock, "Privacy", "Blocked contacts, status", onClick = { showPrivacy = true })
        SettingsCard(Icons.Filled.Notifications, "Notifications", "Message, group & call tones", onClick = { showNotifications = true })
        SettingsCard(Icons.Filled.DataUsage, "Data and Storage", "Network usage, auto-download", onClick = { showDataStorage = true })
        
        Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.DarkMode, "dark", tint = Primary, modifier = Modifier.size(24.dp)) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) { Text("Appearance", color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("Toggle dark mode", color = Secondary, fontSize = 11.sp) }
                val themeController = LocalDarkTheme.current
                Switch(checked = themeController.isDark, onCheckedChange = { themeController.onToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary, uncheckedThumbColor = Color.White, uncheckedTrackColor = SurfaceContainerHighest))
            }
        }
        SettingsCard(Icons.Filled.Help, "Help", "Help center, contact us", onClick = { showHelp = true })
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(icon, title, tint = Primary, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(title, color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Secondary, fontSize = 11.sp) }
            Icon(Icons.Filled.ChevronRight, "next", tint = OutlineVariant, modifier = Modifier.size(20.dp))
        }
    }
}
