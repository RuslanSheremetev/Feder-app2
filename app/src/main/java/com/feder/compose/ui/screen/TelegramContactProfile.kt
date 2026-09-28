package com.feder.compose.ui.screen

import androidx.compose.foundation.horizontalScroll
import com.feder.compose.ui.components.PhotoViewer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.OpenInNew
import com.feder.compose.repository.LinkPreviewRepository
import com.feder.compose.data.entity.LinkPreviewEntity
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
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider

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
    messages: List<com.feder.compose.ui.screen.MsgItem> = emptyList(),
    token: String = "",
    linkPreviewRepo: LinkPreviewRepository? = null,
    isMuted: Boolean = false,
    onMessage: () -> Unit = {},
    onCall: () -> Unit = {},
    onVideo: () -> Unit = {},
    onMuteToggle: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var photoViewerIndex by remember { mutableStateOf<Int?>(null) }
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
            var showMoreMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(Icons.Filled.MoreVert, "more", tint = Color(0xFFE5E2E1), modifier = Modifier.size(24.dp))
                }
                if (showMoreMenu) {
                    androidx.compose.ui.window.Popup(
                        alignment = Alignment.TopEnd,
                        onDismissRequest = { showMoreMenu = false },
                        properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF2A2A2A),
                            shadowElevation = 8.dp,
                            modifier = Modifier.padding(top = 8.dp, end = 8.dp).width(230.dp)
                        ) {
                            Column(Modifier.padding(vertical = 6.dp)) {
                                MenuRow(Icons.Filled.ZoomIn, "Zoom In") { showMoreMenu = false }
                                MenuRow(Icons.Filled.ZoomOut, "Zoom Out") { showMoreMenu = false }
                                MenuRow(Icons.Filled.CalendarMonth, "Calendar") { showMoreMenu = false }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF404752))
                                MenuRow(Icons.Filled.Check, "Show Photos") { showMoreMenu = false }
                                MenuRow(Icons.Filled.Check, "Show Videos") { showMoreMenu = false }
                            }
                        }
                    }
                }
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
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        mediaUrls.chunked(3).forEachIndexed { rowIdx, rowUrls ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                rowUrls.forEachIndexed { colIdx, url ->
                                    val globalIndex = rowIdx * 3 + colIdx
                                    Box(Modifier.weight(1f)) {
                                        MediaThumb(
                                            url = url,
                                            onClick = { photoViewerIndex = globalIndex }
                                        )
                                    }
                                }
                                // Добить пустыми, если ряд неполный
                                repeat(3 - rowUrls.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                LinksList(messages = messages, linkPreviewRepo = linkPreviewRepo, token = token)
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

        // ─── PhotoViewer (fullscreen) ───
        photoViewerIndex?.let { idx ->
            PhotoViewer(
                urls = mediaUrls,
                initialIndex = idx,
                onClose = { photoViewerIndex = null }
            )
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
private fun MediaThumb(url: String, onClick: () -> Unit = {}) {
    val fullUrl = when {
        url.startsWith("http") -> url
        url.startsWith("/") -> "http://2.26.71.102:8010$url"
        url.startsWith("LOCAL:") -> url.removePrefix("LOCAL:")
        else -> "http://2.26.71.102:8012/uploads/$url"
    }
    val context = LocalContext.current
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF353534))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(fullUrl).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
private fun MenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFE5E2E1),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(Modifier.width(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            color = Color(0xFFE5E2E1),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// LINKS — список ссылок из сообщений, сгруппированный по дням
// ═══════════════════════════════════════════════════════════════════

private data class LinkEntry(
    val url: String,
    val msg: MsgItem,
    val host: String,
    val title: String?,
    val image: String?,
    val siteName: String?
)

private fun extractFirstUrlLocal(text: String): String? {
    if (text.isBlank()) return null
    val regex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    val m = regex.find(text) ?: return null
    return m.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}')
}

private fun hostOf(url: String): String {
    return try {
        val u = java.net.URL(url)
        u.host.removePrefix("www.")
    } catch (_: Exception) { url }
}

private fun dayLabel(timeVal: Long): String {
    if (timeVal <= 0L) return "Earlier"
    val now = System.currentTimeMillis()
    val dayMs = 24L * 60 * 60 * 1000
    val todayStart = (now / dayMs) * dayMs
    val entryStart = (timeVal / dayMs) * dayMs
    val diff = ((todayStart - entryStart) / dayMs).toInt()
    return when (diff) {
        0 -> "Today"
        1 -> "Yesterday"
        in 2..6 -> "${diff} days ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
            sdf.format(java.util.Date(timeVal))
        }
    }
}

private fun timeLabel(timeVal: Long): String {
    if (timeVal <= 0L) return ""
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
    return sdf.format(java.util.Date(timeVal))
}

@Composable
private fun LinksList(
    messages: List<MsgItem>,
    linkPreviewRepo: LinkPreviewRepository?,
    token: String
) {
    val context = LocalContext.current

    // Собираем все уникальные ссылки, сортируем по времени убыв.
    val entries = remember(messages) {
        val seen = HashSet<String>()
        messages
            .sortedByDescending { it.timeVal }
            .mapNotNull { m ->
                val u = extractFirstUrlLocal(m.text) ?: return@mapNotNull null
                if (!seen.add(u)) null else u to m
            }
    }

    if (entries.isEmpty()) {
        Box(
            Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No links yet", color = Color(0xFFC0C7D4), fontSize = 14.sp)
        }
        return
    }

    // Группируем по дню
    val grouped = entries.groupBy { dayLabel(it.second.timeVal) }

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        grouped.forEach { (day, dayEntries) ->
            item(key = "header_$day") {
                Text(
                    day,
                    color = Color(0xFFC0C7D4),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                )
            }
            items(dayEntries, key = { it.first }) { (url, msg) ->
                LinkRow(
                    url = url,
                    timeText = timeLabel(msg.timeVal),
                    linkPreviewRepo = linkPreviewRepo,
                    token = token,
                    onClick = {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(url)
                            ).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LinkRow(
    url: String,
    timeText: String,
    linkPreviewRepo: LinkPreviewRepository?,
    token: String,
    onClick: () -> Unit
) {
    var preview by remember(url) { mutableStateOf<LinkPreviewEntity?>(null) }
    LaunchedEffect(url) {
        if (linkPreviewRepo != null && token.isNotEmpty()) {
            try { preview = linkPreviewRepo.getPreview(url, token) } catch (_: Exception) {}
        }
    }
    val p = preview
    val displayTitle = p?.title?.takeIf { it.isNotBlank() } ?: hostOf(url)
    val displaySub = p?.siteName?.takeIf { it.isNotBlank() } ?: hostOf(url)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Превью 56x56
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            if (!p?.image.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(p!!.image).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Color(0xFFA1C9FF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                displayTitle,
                color = Color(0xFFE5E2E1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                displaySub,
                color = Color(0xFF8A919E),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (timeText.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(
                timeText,
                color = Color(0xFF8A919E),
                fontSize = 11.sp
            )
        }
    }
}
