package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.feder.compose.ChatItem

// ─── Цвета в стиле Telegram-дизайна ───
private val ContactsBg       = Color(0xFF0D0D0D)
private val ContactsSurface  = Color(0xFF1A1A1A)
private val ContactsTextMain = Color(0xFFE6EDF3)
private val ContactsTextSub  = Color(0xFF6B7280)
private val ContactsOnline   = Color(0xFF41B35D)
private val ContactsCircle   = Color(0xFF2B5278)

data class Contact(
    val name: String,
    val username: String,
    val status: String,
    val avatarUrl: String? = null,
    val initials: String? = null,
    val online: Boolean = false,
    val lastSeen: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<ChatItem>,
    onBack: () -> Unit,
    onContactClick: (String) -> Unit = {},
    onNewGroup: () -> Unit = {},
    isSearchVisible: Boolean = false,
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {}
) {
    val groupedContacts = contacts.groupBy { it.name.firstOrNull()?.uppercase() ?: "#" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ContactsBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp)
        ) {

            // ═══ ПОИСК (пилюля) ═══
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ContactsSurface)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Search, "search",
                            tint = ContactsTextSub,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            singleLine = true,
                            textStyle = TextStyle(color = ContactsTextMain, fontSize = 16.sp),
                            cursorBrush = SolidColor(Color(0xFF339DFF)),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search", color = ContactsTextSub, fontSize = 16.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // ═══ БОЛЬШИЕ ДЕЙСТВИЯ ═══
            item {
                Column {
                    ContactsActionRow(Icons.Filled.GroupAdd, "New Group", onClick = onNewGroup)
                    ContactsActionRow(Icons.Filled.Lock, "New Secret Chat")
                    ContactsActionRow(Icons.Filled.Campaign, "New Channel")
                }
            }

            // ═══ КОНТАКТЫ ПО БУКВАМ ═══
            groupedContacts.forEach { (letter, items) ->
                item {
                    Text(
                        letter,
                        color = ContactsTextSub,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                }
                items(items, key = { it.username }) { contact ->
                    ContactsRow(contact, onClick = { onContactClick(contact.username) })
                }
            }
        }

        // ═══ FLOATING ХЕДЕР ═══
        Text(
            "Contacts",
            color = ContactsTextMain,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp)
                .zIndex(10f)
        )
    }
}

@Composable
private fun ContactsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ContactsCircle),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            label,
            color = ContactsTextMain,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ContactsRow(contact: ChatItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар + online-dot
        Box(modifier = Modifier.size(54.dp)) {
            val model = when {
                contact.avatarUrl.isNullOrEmpty() -> null
                contact.avatarUrl.startsWith("http") -> contact.avatarUrl
                contact.avatarUrl.startsWith("/") -> "http://2.26.71.102:8010${contact.avatarUrl}"
                else -> "http://2.26.71.102:8010/avatars/${contact.username}/avatar.jpg"
            }
            if (model != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(false).build(),
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(54.dp).clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(ContactsCircle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.name.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (contact.online) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(ContactsOnline)
                        .border(2.dp, ContactsBg, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                contact.name,
                color = ContactsTextMain,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (contact.online) "online"
                else if (contact.lastSeen != null && contact.lastSeen > 0) formatLastSeen(contact.lastSeen)
                else "offline",
                color = if (contact.online) ContactsOnline else ContactsTextSub,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

fun formatLastSeen(timestamp: Long): String {
    val now = System.currentTimeMillis() / 1000
    val diff = now - timestamp
    return when {
        diff < 60 -> "just now"
        diff < 3600 -> "${diff / 60} min ago"
        diff < 86400 -> "${diff / 3600} h ago"
        diff < 604800 -> "${diff / 86400} d ago"
        else -> java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.US)
            .format(java.util.Date(timestamp * 1000))
    }
}
