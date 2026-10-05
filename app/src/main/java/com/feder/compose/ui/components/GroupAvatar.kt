package com.feder.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Аватарка группы (V2 из макета) — иконка силуэтов людей
 * на синем градиенте, в круглой обрезке.
 *
 * Использование:
 *   if (chat.isGroup) GroupAvatar(size = 56.dp) else <обычная аватарка>
 */
@Composable
fun GroupAvatar(
    size: Dp = 56.dp,
    baseColor: Color = Color(0xFF339DFF)
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        baseColor.copy(alpha = 0.72f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}
