package com.feder.compose.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StickerMessage(
    stickerId: String?,
    stickerText: String?,
    time: String,
    isMine: Boolean,
    modifier: Modifier = Modifier
) {
    if (stickerId.isNullOrEmpty()) return

    val sticker = MATRYOSHKA_PACK.firstOrNull { it.id == stickerId }
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
            Box(Modifier.size(140.dp)) {
                MatryoshkaSticker(sticker.copy(text = ""))
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
                        Text("✓✓", color = Color(0xFF41B35D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
