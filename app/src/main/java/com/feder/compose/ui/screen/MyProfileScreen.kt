package com.feder.compose.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val Bg            = Color(0xFF131313)
private val CardBg        = Color(0xFF1F1F1F)
private val TextMain      = Color(0xFFE5E2E1)
private val TextSub       = Color(0xFF8A919E)
private val Accent        = Color(0xFFA1C9FF)
private val AccentDeep    = Color(0xFF00325A)
private val Online        = Color(0xFF41B35D)
private val Blue          = Color(0xFF339DFF)

@Composable
fun MyProfileScreen(
    username: String = "demo",
    displayName: String = "Demo User",
    phone: String = "",
    avatarUrl: String? = null,
    onEditInfo: () -> Unit = {},
    onSetPhoto: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAddPost: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Archived Posts")

    val avatarFullUrl = when {
        avatarUrl.isNullOrEmpty() -> "http://2.26.71.102:8010/avatars/$username/avatar.jpg"
        avatarUrl.startsWith("http") -> avatarUrl
        avatarUrl.startsWith("/") -> "http://2.26.71.102:8010$avatarUrl"
        else -> "http://2.26.71.102:8010/avatars/$username/avatar.jpg"
    }

    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize().background(Bg)
    ) {
        // ─── Скроллируемая часть ───
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            // ─── Аватар + имя + статус ───
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarFullUrl)
                        .crossfade(false)
                        .build(),
                    contentDescription = displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                )

            }
            Spacer(Modifier.height(16.dp))
            Text(displayName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Online))
                Spacer(Modifier.width(6.dp))
                Text("online", color = Accent, fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ─── 3 кнопки: Set Photo / Edit Info / Settings ───
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileActionButton(
                    icon = Icons.Filled.PhotoCamera,
                    label = "Set Photo",
                    modifier = Modifier.weight(1f),
                    onClick = onSetPhoto
                )
                ProfileActionButton(
                    icon = Icons.Filled.Edit,
                    label = "Edit Info",
                    modifier = Modifier.weight(1f),
                    onClick = onEditInfo
                )
                ProfileActionButton(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    modifier = Modifier.weight(1f),
                    onClick = onSettings
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── Инфо-карточка ───
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (phone.isNotEmpty()) {
                        ProfileInfoRow(value = phone, label = "Mobile")
                    }
                    ProfileInfoRow(value = "@$username", label = "Username")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Табы Posts / Archived Posts ───
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    val active = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (active) Accent else CardBg,
                        modifier = Modifier.clickable { selectedTab = idx }
                    ) {
                        Text(
                            title,
                            color = if (active) AccentDeep else Color(0xFFC0C7D4),
                            fontSize = 14.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ─── Пустой контент ───
            Text("No posts yet…", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(
                "Publish photos and videos to display on\nyour profile page",
                color = TextSub,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ─── Add a post ───
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Blue,
                modifier = Modifier.clickable(onClick = onAddPost),
                shadowElevation = 6.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PhotoCamera, "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Add a post", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ─── Top bar: QR слева, More справа ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(androidx.compose.ui.Alignment.TopCenter)
                .zIndex(10f)
                .statusBarsPadding()
                .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F1F))
                    .clickable { /* QR */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.QrCode, "QR", tint = TextMain, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F1F))
                    .clickable { /* More */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MoreVert, "More", tint = TextMain, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(
            Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, label, tint = TextMain, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileInfoRow(value: String, label: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(value, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSub, fontSize = 13.sp)
    }
}
