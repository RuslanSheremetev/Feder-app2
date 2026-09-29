package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import okhttp3.*
import com.google.gson.JsonParser
import com.feder.compose.ChatItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
fun ContactsScreen(contacts: List<ChatItem>, onBack: () -> Unit, onContactClick: (String) -> Unit = {}, isSearchVisible: Boolean = false, searchQuery: String = "", onSearchChange: (String) -> Unit = {}) {
    var searchText by remember { mutableStateOf("") }


    

    val groupedContacts = contacts.groupBy { it.name.first().uppercase() }

    Box(modifier = Modifier.fillMaxSize().padding(top = 64.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {

                // Поиск — появляется по нажатию на лупу
                item {
                    AnimatedVisibility(
                        visible = isSearchVisible,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.height(40.dp).padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Search, "search", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchChange,
                                    singleLine = true,
                                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (searchQuery.isEmpty()) {
                                                Text("Search contacts...", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                // New Group, New Secret Chat, New Channel
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        ActionButton(Icons.Filled.GroupAdd, "New Group")
                        ActionButton(Icons.Filled.Lock, "New Secret Chat")
                        ActionButton(Icons.Filled.Campaign, "New Channel")
                    }
                }

                // Contacts by letter
                groupedContacts.forEach { (letter, contacts) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
                        ) {
                            Text(
                                letter,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                    items(contacts, key = { it.username }) { contact ->
                        ContactRow(contact, onClick = { onContactClick(contact.username) })
                    }
                }

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
        else -> java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.US).format(java.util.Date(timestamp * 1000))
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
private fun ContactRow(contact: ChatItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box {
            if (contact.avatarUrl != null) {
                AsyncImage(
                    model = when {
                        contact.avatarUrl.isNullOrEmpty() -> null
                        contact.avatarUrl.startsWith("http") -> contact.avatarUrl
                        contact.avatarUrl.startsWith("/") -> "http://2.26.71.102:8010${contact.avatarUrl}"
                        else -> "http://2.26.71.102:8010/avatars/${contact.username}/avatar.jpg"
                    },
                    contentDescription = contact.name,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.name.take(2).uppercase() ?: "", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.W600, fontSize = 20.sp)
                }
            }
            if (contact.online) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd).size(40.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.W500)
            Text(
    if (contact.online) "online" 
    else if (contact.lastSeen != null && contact.lastSeen > 0) formatLastSeen(contact.lastSeen)
    else "offline",
    color = if (contact.online) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
    fontSize = 12.sp
)
        }
    }
}

