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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Contact(
    val name: String,
    val username: String,
    val status: String,
    val avatarUrl: String? = null,
    val initials: String? = null,
    val online: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    var searchText by remember { mutableStateOf("") }

    var contacts by remember { mutableStateOf(listOf<Contact>()) }
    
        LaunchedEffect(Unit) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url("http://2.26.71.102:8002/api/chat_settings/all?me=demo").build()
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            val body = response.body?.string()
            if (body != null) {
                val jsonArray = JsonParser.parseString(body).asJsonArray
                val loaded = jsonArray.map { el ->
                    val obj = el.asJsonObject
                    val uname = obj.get("username")?.asString ?: ""
                    val name = obj.get("name")?.asString ?: uname
                    val avatar = obj.get("avatar_url")?.asString
                    val online = obj.get("online")?.asBoolean ?: false
                    val lastMsg = obj.get("last_message")?.asString ?: ""
                    val status = if (online) "online" else if (lastMsg.isNotEmpty()) lastMsg else "offline"
                    Contact(name = name, username = uname, status = status, avatarUrl = avatar, initials = name.take(2).uppercase(), online = online)
                }
                contacts = loaded.filter { it.username != "demo" && it.username != "123" }
            }
        } catch (_: Exception) { }
    }

    val groupedContacts = contacts.groupBy { it.name.first().uppercase() }

    Box(modifier = Modifier.fillMaxSize().padding(top = 64.dp, bottom = 80.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {
                // Search
                item {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search contacts...", color = MaterialTheme.colorScheme.outline) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.outline) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
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
                    items(contacts) { contact ->
                        ContactRow(contact)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

            // FAB
            FloatingActionButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.875f)
                    .padding(start = 0.dp, bottom = 84.dp)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.PersonAdd, "Add contact", modifier = Modifier.size(24.dp))
        }
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
private fun ContactRow(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box {
            if (contact.avatarUrl != null) {
                AsyncImage(
                    model = contact.avatarUrl,
                    contentDescription = contact.name,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.initials ?: "", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.W600, fontSize = 20.sp)
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
            Text(contact.status, color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
        }
    }
}
 
