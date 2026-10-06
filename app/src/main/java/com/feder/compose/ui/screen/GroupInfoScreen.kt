package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.layout.Arrangement
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
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import com.feder.compose.ChatItem

/**
 * FIX_STAGE5: Info-экран группы.
 * Показывает: имя, участников, кнопки Leave/Add/Rename (для админа).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupInfoScreen(
    groupName: String,
    myUsername: String,
    token: String,
    onBack: () -> Unit,
    onMembersChanged: () -> Unit = {},
    onAddMembers: () -> Unit = {},
    onMemberClick: (String) -> Unit = {},
    onMemberLongClick: (String) -> Unit = {},
    reloadKey: Int = 0,
    onMessage: () -> Unit = {},
    isMuted: Boolean = false,
    onMuteToggle: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    groupMembersCache: MutableMap<String, List<String>>? = null,
    groupInfoCache: MutableMap<String, org.json.JSONObject>? = null
) {
    // === GROUPINFO_CACHE_V1 ===
    // Сначала берём из кэша (мгновенно), потом обновляем с сервера
    val cachedMembers = groupMembersCache?.get(groupName)
    var groupInfo by remember(groupName) { mutableStateOf<GroupInfo?>(null) }
    var members by remember(groupName) { mutableStateOf<List<String>>(cachedMembers ?: emptyList()) }
    var isOwner by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(groupName) }
    var loading by remember(groupName) { mutableStateOf(cachedMembers.isNullOrEmpty()) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    LaunchedEffect(groupName, reloadKey) {
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
            val avatarUrl = json.optString("avatar_url", "").ifEmpty { null }
            val arr = json.optJSONArray("members") ?: org.json.JSONArray()
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            groupInfo = GroupInfo(gname, createdBy, list, avatarUrl)
            members = list
            isOwner = createdBy == myUsername
            // === GROUPINFO_CACHE_SAVE_V1 ===
            groupMembersCache?.put(groupName, list)
            groupInfoCache?.put(groupName, json)
            // === /GROUPINFO_CACHE_SAVE_V1 ===
        } catch (e: Exception) {
            android.util.Log.e("GroupInfo", "load: ${e.message}")
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // TopAppBar
// === HEADER_PILL_V2 ===
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 40.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглая кнопка «Назад»
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ArrowBack, "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // Овал с названием группы
            Text(
                "Group Info",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            )

            Spacer(Modifier.weight(1f))

            // Круглая кнопка «Редактировать» (только для owner)
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { showRenameDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit, "Rename",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Пустое место, чтобы название оставалось по центру
                Spacer(Modifier.size(40.dp))
            }
        }
// === /HEADER_PILL_V2 ===

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
                        val avatarUrl = groupInfo?.avatarUrl
                        if (!avatarUrl.isNullOrEmpty()) {
                            val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
                                          else "http://2.26.71.102:8010$avatarUrl"
                            AsyncImage(
                                model = ImageRequest.Builder(ctx).data(fullUrl).crossfade(false).build(),
                                contentDescription = groupName,
                                modifier = Modifier.size(96.dp).clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
// === GROUPINFO_AVATAR_ELSE_FIX ===
                            com.feder.compose.ui.components.GroupAvatar(
                                size = 96.dp
                            )
// === /GROUPINFO_AVATAR_ELSE_FIX ===
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(groupName, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("${members.size} members", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Сетка действий — как в Telegram
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Message
                        ActionTile(
                            icon = Icons.Filled.Chat,
                            label = "Message",
                            modifier = Modifier.weight(1f),
                            onClick = onMessage
                        )
                        // Mute / Unmute
                        ActionTile(
                            icon = if (isMuted) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                            label = if (isMuted) "Unmute" else "Mute",
                            modifier = Modifier.weight(1f),
                            onClick = onMuteToggle
                        )
                        // Video Chat
                        ActionTile(
                            icon = Icons.Filled.Videocam,
                            label = "Video Chat",
                            modifier = Modifier.weight(1f),
                            onClick = onVideoCall
                        )
                        // Leave
                        ActionTile(
                            icon = Icons.Filled.ExitToApp,
                            label = "Leave",
                            modifier = Modifier.weight(1f),
                            tint = MaterialTheme.colorScheme.error,
                            onClick = { /* confirmed ниже */ }
                        )
                    }
                }

                // Add Members — отдельная карточка
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onAddMembers() },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.PersonAdd, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Add Members", fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // MEMBERS заголовок
                item {
                    Text("MEMBERS", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                }

                // Участники — карточка со списком
                items(members) { username ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .combinedClickable(
                                onClick = { onMemberClick(username) },
                                onLongClick = {
                                    if (isOwner && username != myUsername) {
                                        memberToRemove = username
                                    }
                                }
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Аватарка — реальная через feder-avatars (8010), fallback на букву
                        var avatarError by remember(username) { mutableStateOf(false) }
                        if (!avatarError) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("http://2.26.71.102:8010/avatars/$username/avatar.jpg")
                                    .crossfade(false)
                                    .build(),
                                contentDescription = username,
                                modifier = Modifier.size(44.dp).clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                onError = { avatarError = true }
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(username.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(username, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(if (username == groupInfo?.createdBy) "online" else "last seen recently",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (username == groupInfo?.createdBy) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text("Owner",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // Leave Group — отдельно внизу
// Leave group
                // === REMOVE_LEAVE_BOTTOM_V3: кнопка Leave Group удалена (item) ===
            }
        }

        // Remove member dialog
        if (memberToRemove != null) {
            AlertDialog(
                onDismissRequest = { memberToRemove = null },
                title = { Text("Remove member") },
                text = { Text("Remove ${memberToRemove} from the group?") },
                confirmButton = {
                    TextButton(onClick = {
                        val uname = memberToRemove ?: return@TextButton
                        memberToRemove = null
                        scope.launch {
                            try {
                                val json = org.json.JSONObject().apply {
                                    put("name", groupName)
                                    put("user_to_remove", uname)
                                    put("admin", myUsername)
                                }
                                val req = okhttp3.Request.Builder()
                                    .url("http://2.26.71.102:8004/api/group/remove_member")
                                    .addHeader("Authorization", "Bearer $token")
                                    .post(okhttp3.RequestBody.create(
                                        "application/json".toMediaType(),
                                        json.toString()))
                                    .build()
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    okhttp3.OkHttpClient().newCall(req).execute()
                                }
                                // перезагружаем участников
                                val encoded = java.net.URLEncoder.encode(groupName, "UTF-8")
                                val r2 = okhttp3.Request.Builder()
                                    .url("http://2.26.71.102:8004/api/group/info?name=$encoded").build()
                                val resp2 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    okhttp3.OkHttpClient().newCall(r2).execute()
                                }
                                val b2 = resp2.body?.string() ?: "{}"
                                val j2 = org.json.JSONObject(b2)
                                val a2 = j2.optJSONArray("members")
                                val l2 = mutableListOf<String>()
                                if (a2 != null) for (ii in 0 until a2.length()) l2.add(a2.getString(ii))
                                members = l2
                            } catch (e: Exception) {
                                android.util.Log.e("GroupInfo", "remove: ${e.message}")
                            }
                        }
                    }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { memberToRemove = null }) { Text("Cancel") }
                }
            )
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
    val members: List<String>,
    val avatarUrl: String? = null
)

@Composable
private fun ActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1)
        }
    }
}

