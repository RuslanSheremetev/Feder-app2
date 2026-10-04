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

/**
 * FIX_STAGE6: экран добавления участников в существующую группу.
 * Как NewGroupScreen, но без поля имени.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembersScreen(
    groupName: String,
    existingMembers: List<String>,
    contacts: List<ChatItem>,
    myUsername: String = "demo",
    token: String,
    onBack: () -> Unit,
    onAdded: () -> Unit = {}
) {
    val selected = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    // Исключаем уже участников
    val available = contacts.filter { it.username !in existingMembers }
    val canAdd = selected.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Add Members", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            IconButton(
                enabled = canAdd,
                onClick = {
                    scope.launch {
                        try {
                            for (username in selected) {
                                val json = org.json.JSONObject().apply {
                                    put("name", groupName)
                                    put("user", username)
                                }
                                val req = okhttp3.Request.Builder()
                                    .url("http://2.26.71.102:8004/api/group/add_member")
                                    .addHeader("Authorization", "Bearer $token")
                                    .post(okhttp3.RequestBody.create(
                                        "application/json".toMediaType(),
                                        json.toString()))
                                    .build()
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    okhttp3.OkHttpClient().newCall(req).execute()
                                }
                            }
                            onAdded()
                        } catch (e: Exception) {
                            android.util.Log.e("AddMembers", "add: ${e.message}")
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.Check, "Add",
                    tint = if (canAdd) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.outline)
            }
        }

        Text("${selected.size} selected",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        if (available.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("All contacts are already in the group",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(available, key = { it.username }) { c ->
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
                            Text("@" + c.username, fontSize = 13.sp,
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
}
