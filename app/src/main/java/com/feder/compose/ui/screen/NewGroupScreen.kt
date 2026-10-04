package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.feder.compose.ChatItem

@Composable
fun NewGroupScreen(
    contacts: List<ChatItem>,
    myUsername: String = "demo",
    onBack: () -> Unit,
    onCreate: (name: String, members: List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<String>() }
    val canCreate = groupName.isNotBlank() && selected.isNotEmpty()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back",
                    tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("New Group", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f))
            IconButton(
                onClick = { if (canCreate) onCreate(groupName, selected.toList()) },
                enabled = canCreate
            ) {
                Icon(Icons.Filled.Check, "Create",
                    tint = if (canCreate) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.outline)
            }
        }

        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            placeholder = { Text("Group name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text("${selected.size} of ${contacts.size} selected",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(contacts, key = { it.username }) { c ->
                val isChecked = selected.contains(c.username)
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { if (isChecked) selected.remove(c.username)
                                     else selected.add(c.username) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatar = c.avatarUrl
                    if (avatar != null && avatar.isNotEmpty()) {
                        val url = if (avatar.startsWith("http")) avatar
                                  else "http://2.26.71.102:8010$avatar"
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(url).crossfade(false).build(),
                            contentDescription = c.name,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.size(44.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center) {
                            Text(c.name.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.name, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text(if (c.online) "online" else "last seen recently",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Checkbox(checked = isChecked, onCheckedChange = { ch ->
                        if (ch) selected.add(c.username)
                        else selected.remove(c.username)
                    })
                }
            }
        }
    }
}
