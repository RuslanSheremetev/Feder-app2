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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.feder.compose.ChatItem

/**
 * FIX_STAGE5: Info-экран группы.
 * Показывает: имя, участников, кнопки Leave/Add/Rename (для админа).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    groupName: String,
    myUsername: String,
    token: String,
    onBack: () -> Unit,
    onMembersChanged: () -> Unit = {}
) {
    var groupInfo by remember { mutableStateOf<GroupInfo?>(null) }
    var members by remember { mutableStateOf<List<String>>(emptyList()) }
    var isOwner by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(groupName) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    LaunchedEffect(groupName) {
        try {
            val encoded = java.net.URLEncoder.encode(groupName, "UTF-8")
            val url = "http://2.26.71.102:8004/api/group/info?name=$encoded"
            val req = okhttp3.Request.Builder().url(url)
                .addHeader("Authorization", "Bearer $token").build()
            val resp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                okhttp3.OkHttpClient().newCall(req).execute()
            }
            val body = resp.body?.string() ?: "{}"
            val json = org.json.JSONObject(body)
            val gname = json.optString("name", groupName)
            val createdBy = json.optString("created_by", "")
            val arr = json.optJSONArray("members") ?: org.json.JSONArray()
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            groupInfo = GroupInfo(gname, createdBy, list)
            members = list
            isOwner = createdBy == myUsername
        } catch (e: Exception) {
            android.util.Log.e("GroupInfo", "load: ${e.message}")
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // TopAppBar
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 40.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Group Info", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (isOwner) {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(Icons.Filled.Edit, "Rename", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Аватарка + имя
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                groupName.firstOrNull()?.uppercase() ?: "G",
                                fontSize = 40.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(groupName, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("${members.size} members", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Add Members
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { /* TODO: add members screen */ }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Add Members", fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Участники
                item {
                    Text("MEMBERS", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                }
                items(members) { username ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { /* TODO: profile */ }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(username.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(username, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                        if (username == groupInfo?.createdBy) {
                            Text("owner", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Leave group
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    try {
                                        val json = org.json.JSONObject().apply {
                                            put("name", groupName)
                                            put("user", myUsername)
                                        }
                                        val req = okhttp3.Request.Builder()
                                            .url("http://2.26.71.102:8004/api/group/leave")
                                            .addHeader("Authorization", "Bearer $token")
                                            .post(okhttp3.RequestBody.create(
                                                "application/json".toMediaType(),
                                                json.toString()))
                                            .build()
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            okhttp3.OkHttpClient().newCall(req).execute()
                                        }
                                        onMembersChanged()
                                        onBack()
                                    } catch (e: Exception) {
                                        android.util.Log.e("GroupInfo", "leave: ${e.message}")
                                    }
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(16.dp))
                        Text("Leave Group", fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Rename dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename group") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                val json = org.json.JSONObject().apply {
                                    put("name", groupName)
                                    put("new_name", newName)
                                    put("user", myUsername)
                                }
                                val req = okhttp3.Request.Builder()
                                    .url("http://2.26.71.102:8004/api/group/rename")
                                    .addHeader("Authorization", "Bearer $token")
                                    .post(okhttp3.RequestBody.create(
                                        "application/json".toMediaType(),
                                        json.toString()))
                                    .build()
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    okhttp3.OkHttpClient().newCall(req).execute()
                                }
                                showRenameDialog = false
                                onMembersChanged()
                            } catch (e: Exception) {
                                android.util.Log.e("GroupInfo", "rename: ${e.message}")
                            }
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

data class GroupInfo(
    val name: String,
    val createdBy: String,
    val members: List<String>
)
