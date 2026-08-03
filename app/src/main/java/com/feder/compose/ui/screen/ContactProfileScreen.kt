package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.*
import com.google.gson.JsonParser
import com.feder.compose.ui.theme.*

@Composable
fun ContactProfileScreen(contactName: String, onBack: () -> Unit, avatarUrl: String? = null, phone: String = "", bio: String = "", birthday: String = "") {
    var isMuted by remember { mutableStateOf(false) }
    var showMedia by remember { mutableStateOf(false) }
    if (showMedia) { MediaScreen(onBack = { showMedia = false }); return }
    var userPhone by remember { mutableStateOf(phone) }
    var userBio by remember { mutableStateOf(bio) }
    var userBirthday by remember { mutableStateOf(birthday) }
    var userAvatar by remember { mutableStateOf(avatarUrl) }
    var userName by remember { mutableStateOf(contactName) }
    
    LaunchedEffect(contactName) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url("http://2.26.71.102:8002/api/user/$contactName").build()
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            val body = response.body?.string()
            if (body != null) {
                val json = JsonParser.parseString(body).asJsonObject
                userPhone = json.get("phone")?.asString ?: phone
                userBio = json.get("bio")?.asString ?: bio
                userBirthday = json.get("birthday")?.asString ?: birthday
                userAvatar = json.get("avatar_url")?.asString ?: avatarUrl
                userName = json.get("name")?.asString ?: contactName
            }
        } catch (_: Exception) { }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "back", tint = Primary) }
            Text("Contact Info", color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
        }
        
        Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(120.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        if (avatarUrl != null) {
                            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(userAvatar ?: "").crossfade(true).build(), contentDescription = "avatar", modifier = Modifier.size(120.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Filled.Person, "avatar", tint = Primary, modifier = Modifier.size(64.dp))
                        }
                }
            Spacer(Modifier.height(24.dp))
            Text(userName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, color = OnSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("@$contactName", color = Secondary, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.QrCode, "qr", tint = Secondary, modifier = Modifier.size(18.dp))
            }
            if (userPhone.isNotEmpty()) { Text(userPhone, color = Secondary, fontSize = 15.sp) }
            if (userBirthday.isNotEmpty()) { Text(userBirthday, color = Secondary, fontSize = 15.sp) }
            if (userBio.isNotEmpty()) { Text(userBio, color = OnSurface, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                QuickAction(Icons.Filled.Chat, "Message")
                Spacer(Modifier.width(32.dp))
                QuickAction(Icons.Filled.Call, "Call")
                Spacer(Modifier.width(32.dp))
                QuickAction(Icons.Filled.Videocam, "Video")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            
            Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
                Column {
                    Row(Modifier.fillMaxWidth().clickable { isMuted = !isMuted }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NotificationsOff, "mute", tint = Secondary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) { Text("Mute Notifications", color = OnSurface, fontSize = 17.sp); Text("Silence alerts for this chat", color = Secondary, fontSize = 11.sp) }
                        Switch(checked = isMuted, onCheckedChange = { isMuted = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f))
                    ProfileRow(Icons.Filled.Image, "Media") { showMedia = true }
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f))
                    ProfileRow(Icons.Filled.Wallpaper, "Wallpaper & Sound")
                }
            }
            
            Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
                Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Block, "block", tint = Error, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Block $contactName", color = Error, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text("Groups in Common", color = Secondary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 8.dp))
            Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
                Column(Modifier.padding(20.dp)) {
                    GroupRow("Project Phoenix", "12 members")
                    Spacer(Modifier.height(16.dp))
                    GroupRow("Weekend Hikers", "8 members")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuickAction(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(PrimaryContainer.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(icon, label, tint = Primary, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Primary, fontSize = 13.sp)
    }
}

@Composable
fun ProfileRow(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, title, tint = Secondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = OnSurface, fontSize = 17.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, "next", tint = Outline, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun GroupRow(name: String, members: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHighest), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Groups, "group", tint = OnSurface, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column { Text(name, color = OnSurface, fontSize = 14.sp); Text(members, color = Secondary, fontSize = 11.sp) }
    }
}
