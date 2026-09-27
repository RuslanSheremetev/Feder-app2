package com.feder.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.feder.compose.data.entity.LinkPreviewEntity
import com.feder.compose.repository.LinkPreviewRepository

private val CardBg     = Color(0xFF1F1F1F)
private val CardBgMine = Color(0x33000000)
private val TextMain   = Color(0xFFE5E2E1)
private val TextSub    = Color(0xFFC0C7D4)
private val TextSite   = Color(0xFF8A919E)
private val Accent     = Color(0xFFA1C9FF)

@Composable
fun LinkPreviewCard(
    url: String,
    token: String,
    isMine: Boolean,
    repository: LinkPreviewRepository?,
    onYouTubeClick: (videoId: String) -> Unit = {}
) {
    if (repository == null) return

    val context = LocalContext.current
    val openUrl: (String) -> Unit = remember(context) {
        { u ->
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(u)
                ).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    var preview by remember(url) { mutableStateOf<LinkPreviewEntity?>(null) }
    var loading by remember(url) { mutableStateOf(true) }

    LaunchedEffect(url) {
        loading = true
        preview = repository.getPreview(url, token)
        loading = false
    }

    if (loading) return
    val p = preview ?: return

    when (p.type) {
        "youtube" -> YouTubeCard(p, isMine) { vid -> onYouTubeClick(vid) }
        "article" -> ArticleCard(p, isMine) { openUrl(url) }
        "image"   -> ImageCard(p, isMine) { openUrl(url) }
        else      -> GenericCard(p, isMine) { openUrl(url) }
    }
}

@Composable
private fun YouTubeCard(
    p: LinkPreviewEntity,
    isMine: Boolean,
    onPlay: (String) -> Unit
) {
    val bg = if (isMine) CardBgMine else CardBg
    Column(
        Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { p.videoId?.let(onPlay) }
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            if (!p.image.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(p.image).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
        Column(Modifier.padding(10.dp)) {
            if (!p.title.isNullOrEmpty()) {
                Text(
                    p.title,
                    color = TextMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!p.description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    p.description,
                    color = TextSite,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(
    p: LinkPreviewEntity,
    isMine: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isMine) CardBgMine else CardBg
    Row(
        Modifier
            .width(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        if (!p.image.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(p.image).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            if (!p.title.isNullOrEmpty()) {
                Text(
                    p.title,
                    color = TextMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!p.description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    p.description,
                    color = TextSub,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!p.siteName.isNullOrEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    p.siteName,
                    color = TextSite,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ImageCard(
    p: LinkPreviewEntity,
    isMine: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isMine) CardBgMine else CardBg
    Box(
        Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
    ) {
        if (!p.image.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(p.image).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun GenericCard(
    p: LinkPreviewEntity,
    isMine: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isMine) CardBgMine else CardBg
    Row(
        Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔗", fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            p.url,
            color = Accent,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun extractFirstUrl(text: String): String? {
    if (text.isBlank()) return null
    val regex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    val match = regex.find(text) ?: return null
    return match.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}')
}
