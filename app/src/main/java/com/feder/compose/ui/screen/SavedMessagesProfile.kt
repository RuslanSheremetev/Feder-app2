package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.feder.compose.ui.components.PhotoViewer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi

// ─── Хардкод цветов (не тянем из Theme.kt, чтобы файл компилировался сам) ───
private val Bg               = Color(0xFF131313)
private val Surface          = Color(0xFF1F1F1F)
private val Primary          = Color(0xFFA1C9FF)
private val PrimaryContainer = Color(0xFF339DFF)
private val OnPrimary        = Color(0xFF00325A)
private val OnSurfaceVar     = Color(0xFFC0C7D4)
private val Outline          = Color(0xFF8A919E)
private val OutlineVariant   = Color(0xFF404752)

@kotlin.OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SavedMessagesProfile(
    onBack: () -> Unit = {},
    mediaUrls: List<String> = emptyList(),
    messages: List<com.feder.compose.ui.screen.MsgItem> = emptyList()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var photoViewerIndex by remember { mutableStateOf<Int?>(null) }
    val tabs = listOf("Медиа", "Файлы", "Ссылки", "Музыка", "Голосовые", "Заметки")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = OnSurfaceVar,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = { /* меню */ }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Меню",
                    tint = OnSurfaceVar,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── Скроллируемый контент ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header: аватар + имя + подзаголовок ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryContainer, Color(0xFF1A5489))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Saved Messages",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Ваши сохранённые сообщения",
                    color = OnSurfaceVar,
                    fontSize = 13.sp
                )
            }

            // ── Табы (горизонтальный скролл) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val active = index == selectedTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) Primary else Surface)
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            tab,
                            color = if (active) OnPrimary else OnSurfaceVar,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Music tab (3) ───
            if (selectedTab == 3) {
                val audioMsgs = messages.filter { msg ->
                    val u = msg.imageUrls.firstOrNull() ?: msg.imageUrl
                    u != null && com.feder.compose.audio.IsAudio.isAudioFile(u)
                }.reversed()
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (audioMsgs.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            Text("Нет музыки", color = OnSurfaceVar, fontSize = 14.sp)
                        }
                    } else {
                        audioMsgs.forEach { msg ->
                            val u = msg.imageUrls.firstOrNull() ?: msg.imageUrl
                            if (u != null) {
                                com.feder.compose.audio.AudioBubble(
                                    audioUrl = u, token = "",
                                    isMine = msg.from == "demo",
                                    time = msg.time, msgStatus = msg.status,
                                    onLongClick = { }
                                )
                            }
                        }
                    }
                }
            } else {
            // ── Empty state ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mediaUrls.isNotEmpty()) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
                                repeat(3 - rowUrls.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = OutlineVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Нет медиа",
                        color = OnSurfaceVar,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Всё, что вы сохраняете — фото, видео, документы — появится здесь",
                        color = Outline,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        }
    }
            }

        // ─── PhotoViewer (fullscreen) ───
        photoViewerIndex?.let { idx ->
            PhotoViewer(
                urls = mediaUrls,
                initialIndex = idx,
                senderName = "Saved Messages",
                timeText = "",
                onClose = { photoViewerIndex = null }
            )
        }
}

@Composable
private fun MediaThumb(url: String, onClick: () -> Unit = {}) {
    val fullUrl = when {
        url.startsWith("http://") || url.startsWith("https://") -> url
        url.startsWith("LOCAL:") -> url.removePrefix("LOCAL:")
        url.endsWith(".mp4") -> "http://2.26.71.102:8018/videos/$url"
        url.startsWith("/uploads/") -> "http://2.26.71.102:8012$url"
        else -> "http://2.26.71.102:8012/uploads/$url"
    }
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF353534))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = fullUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
