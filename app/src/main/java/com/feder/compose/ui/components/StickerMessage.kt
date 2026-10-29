package com.feder.compose.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feder.compose.ui.screen.DrawCheck

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerMessage(
    stickerId: String?,
    stickerText: String?,
    time: String,
    isMine: Boolean,
    status: String = "sent",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    if (stickerId.isNullOrEmpty()) return
    val sticker = ALL_PACKS.flatMap { it.stickers }.firstOrNull { it.id == stickerId }
        ?: StickerItem(stickerId, 1, stickerText ?: "")
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(
                start = if (isMine) 0.dp else 8.dp,
                end = if (isMine) 8.dp else 0.dp
            )
        ) {
            Box(
                Modifier
                    .size(140.dp)
                    .combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = { onLongClick?.invoke() }
                    )
            ) {
                coil.compose.AsyncImage(
                    model = "file:///android_asset/stickers/${sticker.id}",
                    contentDescription = sticker.id,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (!stickerText.isNullOrEmpty()) {
                    Text(
                        stickerText,
                        color = Color(0xFFE8493E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                }
                if (time.isNotEmpty()) {
                    Text(time, color = Color(0xFF8A919E), fontSize = 11.sp)
                    if (isMine) {
                        Spacer(Modifier.width(3.dp))
                        // Галочка как у текстовых (нарисованная)
                        val checkColor = when (status) {
                            "read" -> Color(0xFF4CAF50)
                            else -> Color.White.copy(alpha = 0.7f)
                        }
                        DrawCheck(
                            double = (status == "read" || status == "received"),
                            tint = checkColor,
                            size = 14.dp
                        )
                    }
                }
            }
        }
    }
}
