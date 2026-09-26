package com.feder.compose.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun TelegramContactProfile(
    contactName: String,
    contactUsername: String = "",
    onBack: () -> Unit,
    avatarUrl: String? = null,
    phone: String = "",
    bio: String = "",
    lastSeen: String = "last seen recently",
    mediaUrls: List<String> = emptyList(),
    isMuted: Boolean = false,
    onMessage: () -> Unit = {},
    onCall: () -> Unit = {},
    onVideo: () -> Unit = {},
    onMuteToggle: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Media", "Files", "Links", "Music", "Polls")

    val avatarFullUrl = when {
        avatarUrl.isNullOrEmpty() -> "http://2.26.71.102:8010/avatars/$contactUsername/avatar.jpg"
        avatarUrl.startsWith("http") -> avatarUrl
        avatarUrl.startsWith("/") -> "http://2.26.71.102:8010$avatarUrl"
        else -> "http://2.26.71.102:8010/avatars/$contactUsername/avatar.jpg"
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF131313))
    ) {

        // ─── Top bar ───
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = Color(0xFFE5E2E1), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* more */ }) {
                Icon(Icons.Filled.MoreVert, "more", tint = Color(0xFFE5E2E1), modifier = Modifier.size(24.dp))
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {

            // ─── Аватарка + имя + статус ───
            Column(
                Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarFullUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = contactName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    contactName,
                    color = Color(0xFFE5E2E1),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    lastSeen,
                    color = Color(0xFFC0C7D4),
                    fontSize = 14.sp
                )
            }

            // ─── 4 кнопки ───
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Filled.ChatBubble,
                    label = "Message",
                    modifier = Modifier.weight(1f),
                    onClick = onMessage
                )
                ActionButton(
                    icon = if (isMuted) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                    label = if (isMuted) "Unmute" else "Mute",
                    modifier = Modifier.weight(1f),
                    onClick = onMuteToggle
                )
                ActionButton(
                    icon = Icons.Filled.Call,
                    label = "Call",
                    modifier = Modifier.weight(1f),
                    onClick = onCall
                )
                ActionButton(
                    icon = Icons.Filled.Videocam,
                    label = "Video",
                    modifier = Modifier.weight(1f),
                    onClick = onVideo
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── Инфо-карточка ───
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1F1F1F)
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (phone.isNotEmpty()) {
                        InfoRow(value = phone, label = "Mobile")
                    }
                    if (bio.isNotEmpty()) {
                        InfoRow(value = bio, label = "Bio")
                    }
                    if (contactUsername.isNotEmpty()) {
                        InfoRow(value = "@$contactUsername", label = "Username")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Табы (пилюли) ───
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    val active = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (active) Color(0xFFA1C9FF) else Color(0xFF1F1F1F),
                        modifier = Modifier.clickable { selectedTab = idx }
                    ) {
                        Text(
                            title,
                            color = if (active) Color(0xFF00325A) else Color(0xFFC0C7D4),
                            fontSize = 14.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ─── Медиа-сетка ───
            if (selectedTab == 0) {
                if (mediaUrls.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No media yet", color = Color(0xFFC0C7D4), fontSize = 14.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        userScrollEnabled = false
                    ) {
                        items(mediaUrls) { url ->
                            MediaThumb(url = url)
                        }
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Coming soon", color = Color(0xFFC0C7D4), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1F1F1F)
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, label, tint = Color(0xFFE5E2E1), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color(0xFFE5E2E1), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun InfoRow(value: String, label: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(value, color = Color(0xFFE5E2E1), fontSize = 16.sp)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Color(0xFFC0C7D4), fontSize = 13.sp)
    }
}

@Composable
private fun MediaThumb(url: String) {
    val fullUrl = when {
        url.startsWith("http") -> url
        url.startsWith("/") -> "http://2.26.71.102:8010$url"
        url.startsWith("LOCAL:") -> url.removePrefix("LOCAL:")
        else -> "http://2.26.71.102:8012/uploads/$url"
    }
    val context = LocalContext.current
    Box(
        Modifier.aspectRatio(1f).clip(RoundedCornerShape(4.dp)).background(Color(0xFF353534))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(fullUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
