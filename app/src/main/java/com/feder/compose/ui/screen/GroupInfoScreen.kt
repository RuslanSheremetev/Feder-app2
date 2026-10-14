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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
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

    // ═══ Анимация скролла ═══
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val rawProgress by remember {
        androidx.compose.runtime.derivedStateOf {
            val idx = listState.firstVisibleItemIndex
            val off = listState.firstVisibleItemScrollOffset
            // total = суммарный скролл в пикселях (реальный, независимо от idx)
            val total = idx * 1000f + off
            (total / 1800f).coerceIn(0f, 1f)
        }
    }
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "group-header-progress"
    )

    // ═══ GROUPINFO_BANNER_SIMPLE ═══
    // Аватарка — уменьшается и гаснет НА МЕСТЕ
    val pAvatar = (progress / 0.30f).coerceIn(0f, 1f)
    val avatarSz = (96f - 60f * pAvatar).dp       // 96 → 36
    val avatarA  = (1f - pAvatar).coerceIn(0f, 1f)

    // Имя — гаснет на месте
    val pName = (progress / 0.40f).coerceIn(0f, 1f)
    val nameA = (1f - pName).coerceIn(0f, 1f)

    // Members — гаснет
    val pMembers = (progress / 0.50f).coerceIn(0f, 1f)
    val membersA = (1f - pMembers).coerceIn(0f, 1f)

    // Кнопки — гаснут
    val pActions = (progress / 0.60f).coerceIn(0f, 1f)
    val actionsA = (1f - pActions).coerceIn(0f, 1f)

    val infoAlpha = membersA
 

    // ═══ ПЛАВНЫЙ КАСКАД БЕЗ ПЕРЕСЕЧЕНИЙ ═══
    // Фаза 1: Аватар 96→0dp  [0.00..0.20]
    val avatarSize = (96f - progress * 96f / 0.20f).coerceIn(0f, 96f).dp
    val avatarAlpha = (1f - progress / 0.20f).coerceIn(0f, 1f)
    // Пауза [0.20..0.25]

    // Фаза 2: Имя 24→20sp, alpha 1→0  [0.25..0.40]
    val nameProgress = ((progress - 0.25f) / 0.15f).coerceIn(0f, 1f)
    val nameFontSize = (24f - nameProgress * 4f).sp
    val nameAlpha = (1f - nameProgress).coerceIn(0f, 1f)
    // Пауза [0.40..0.45]

    // Фаза 3: Кнопки 56→0dp  [0.45..0.65]
    val actionsProgress = ((progress - 0.45f) / 0.20f).coerceIn(0f, 1f)
    val actionsHeight = (56f - actionsProgress * 56f).coerceIn(0f, 56f).dp
    val actionsAlpha = (1f - actionsProgress).coerceIn(0f, 1f)
    // Пауза [0.65..0.70]

    // Фаза 4: Add Members 56→0dp  [0.70..0.85]
    val addMembersProgress = ((progress - 0.30f) / 0.20f).coerceIn(0f, 1f)
    val addMembersHeight = (72f - addMembersProgress * 72f).coerceIn(0f, 72f).dp
    val addMembersAlpha = (1f - addMembersProgress).coerceIn(0f, 1f)
    val addMembersOffsetY = (-addMembersProgress * 100f).dp

    // Фон шапки — появляется [0.60..0.75]
    val headerBgAlpha = ((progress - 0.20f) / 0.35f).coerceIn(0f, 1f)
    // Маленький аватар в хедере — появляется [0.30..0.45] (после аватара)
    val headerAvatarAlpha = ((progress - 0.15f) / 0.30f).coerceIn(0f, 1f)
    // Имя в шапке — появляется [0.65..0.85]
    val headerNameAlpha = ((progress - 0.15f) / 0.30f).coerceIn(0f, 1f)
    // Общая (для старого кода)
    // Общая (для старого кода, если остался)
    val contentAlpha = (1f - progress / 0.9f).coerceIn(0f, 1f)

    LaunchedEffect(groupName, reloadKey) {
        // === GROUPINFO_NO_FETCH_IF_CACHED_V1 ===
        // Как Contacts: если данные уже в кэше (префетч в loadChats), НЕ делаем HTTP.
        if (!groupMembersCache?.get(groupName).isNullOrEmpty()) {
            loading = false
            groupInfoCache?.get(groupName)?.let { cached ->
                val createdBy = cached.optString("created_by", "")
                isOwner = createdBy == myUsername
                val gname = cached.optString("name", groupName)
                val avatarUrl = cached.optString("avatar_url", "").ifEmpty { null }
                groupInfo = GroupInfo(gname, createdBy, members, avatarUrl)
            }
            return@LaunchedEffect
        }
        // === /GROUPINFO_NO_FETCH_IF_CACHED_V1 ===
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
            groupMembersCache?.put(groupName, list)
            groupInfoCache?.put(groupName, json)
        } catch (e: Exception) {
            android.util.Log.e("GroupInfo", "load: ${e.message}")
        }
        loading = false
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // TopAppBar
// === HEADER_PILL_V2 (анимированный) ===
        // Фон-полоса хедера (появляется при скролле)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((progress * 70).dp)  // ← 0 → 70dp (покрывает Row полностью)
                .background(
                    androidx.compose.ui.graphics.Color(0xFF131313)
                )
                .align(androidx.compose.ui.Alignment.TopCenter)
                .zIndex(9f)
        )
        // ═══ HEADER — точная копия Contacts ═══
        val outerHPad by androidx.compose.animation.core.animateDpAsState(
            targetValue = (16f * (1f - (progress * 1.5f).coerceIn(0f, 1f))).dp, label = "outerHPad"
        )
        val outerVPad by androidx.compose.animation.core.animateDpAsState(
            targetValue = (12f - 4f * progress).dp, label = "outerVPad"
        )
        val gap by androidx.compose.animation.core.animateDpAsState(
            targetValue = (12f * (1f - (progress * 2.5f).coerceIn(0f, 1f))).dp, label = "gap"
        )
        val radius by androidx.compose.animation.core.animateDpAsState(
            targetValue = (24f - 22f * (progress * 1.5f).coerceIn(0f, 1f)).dp, label = "radius"
        )
        val searchRadius by androidx.compose.animation.core.animateDpAsState(
            targetValue = (22f - 20f * (progress * 1.5f).coerceIn(0f, 1f)).dp, label = "searchRadius"
        )
        val searchWidth by androidx.compose.animation.core.animateDpAsState(
            targetValue = (44f + 60f * (progress * 1.5f).coerceIn(0f, 1f)).dp, label = "searchWidth"
        )
        val ovalInnerH by androidx.compose.animation.core.animateDpAsState(
            targetValue = (14f + 8f * progress).dp, label = "ovalInnerH"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(androidx.compose.ui.Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = outerHPad, vertical = outerVPad)
                .zIndex(10f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ─── ЛЕВАЯ ПЛАШКА: растёт от 55% до 100% ───
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.55f + 0.45f * progress)
                    .height(44.dp)
                    .clip(RoundedCornerShape(radius))
                    .background(
                        androidx.compose.ui.graphics.Color(0xFF131313)
                    )
                    .clickable { onBack() }
                    .padding(horizontal = ovalInnerH, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ArrowBack, "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width((8f * progress).dp))
                Box(
                    modifier = Modifier
                        .size((28f * progress).dp)
                        .clip(CircleShape)
                        .alpha(headerAvatarAlpha)
                ) {
                    val hUrl = groupInfo?.avatarUrl
                    if (!hUrl.isNullOrEmpty()) {
                        val hFull = if (hUrl.startsWith("http")) hUrl
                                    else "http://2.26.71.102:8010$hUrl"
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(hFull).crossfade(false).build(),
                            contentDescription = groupName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        com.feder.compose.ui.components.GroupAvatar(size = (28f * progress).dp)
                    }
                }
                Spacer(Modifier.width((8f * progress).dp))
                Text(
                    groupName,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = headerNameAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            // ─── Толкаем правую плашку вправо ───
            Spacer(Modifier.weight(1f))
            // ─── Зазор: 12 → 0 ───
            Spacer(Modifier.width(gap))
            // ─── ПРАВАЯ ПЛАШКА: 44 → 104dp ───
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .width(searchWidth)
                        .height(44.dp)
                        .clip(RoundedCornerShape(searchRadius))
                        .background(
                            androidx.compose.ui.graphics.Color(0xFF131313)
                        )
                        .clickable { showRenameDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit, "Rename",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Spacer(Modifier.size(44.dp))
            }
        }
        // === /HEADER_PILL_V2 (анимированный) ===

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 0.dp, bottom = 24.dp)
            ) {
                // ═══ GROUPINFO_BANNER_SIMPLE ═══
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(330.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // 1) АВАТАРКА — уменьшается на месте
                        Box(
                            Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 60.dp)
                                .size(avatarSz)
                                .clip(CircleShape)
                                .alpha(avatarA)
                        ) {
                            val avatarUrl = groupInfo?.avatarUrl
                            if (!avatarUrl.isNullOrEmpty()) {
                                val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
                                              else "http://2.26.71.102:8010$avatarUrl"
                                AsyncImage(
                                    model = ImageRequest.Builder(ctx).data(fullUrl).crossfade(false).build(),
                                    contentDescription = groupName,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                com.feder.compose.ui.components.GroupAvatar(size = avatarSz)
                            }
                        }
                        // 2) ИМЯ — на месте
                        Text(
                            groupName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 172.dp)
                                .alpha(nameA)
                        )
                        // 3) MEMBERS
                        Text(
                            if (loading) "Loading members…" else "${members.size} members",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 212.dp)
                                .alpha(membersA)
                        )
                        // 4) КНОПКИ
                        Row(
                            Modifier
                                .align(Alignment.TopStart)
                                .offset(y = 256.dp)
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .alpha(actionsA),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ActionTile(icon = Icons.Filled.Chat, label = "Message",
                                modifier = Modifier.weight(1f), onClick = onMessage)
                            ActionTile(
                                icon = if (isMuted) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                                label = if (isMuted) "Unmute" else "Mute",
                                modifier = Modifier.weight(1f), onClick = onMuteToggle)
                            ActionTile(icon = Icons.Filled.Videocam, label = "Video",
                                modifier = Modifier.weight(1f), onClick = onVideoCall)
                            ActionTile(icon = Icons.Filled.ExitToApp, label = "Leave",
                                modifier = Modifier.weight(1f),
                                tint = MaterialTheme.colorScheme.error,
                                onClick = { })
                        }
                    }
                }
                // Add Members — отдельная карточка
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(addMembersHeight)
                            .alpha(addMembersAlpha)
                            .offset(y = addMembersOffsetY)
                            .padding(horizontal = 16.dp)
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
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(3.dp))
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1)
        }
    }
}

