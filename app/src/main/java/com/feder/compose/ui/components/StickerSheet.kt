package com.feder.compose.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Цвета в стиле Telegram ───
private val SheetBg       = Color(0xFF1C1C1C)
private val SurfaceDark   = Color(0xFF2A2A2A)
private val HandleColor   = Color(0xFF4A4A4A)
private val TextOn        = Color(0xFFE5E2E1)
private val TextMuted     = Color(0xFF8A919E)
private val ChipActive    = Color(0xFF404752)
private val Accent        = Color(0xFF339DFF)
private val MatryoshkaBlue = Color(0xFF4A88DE)
private val MatryoshkaRed  = Color(0xFFE8493E)
private val MatryoshkaSkin = Color(0xFFFBE0C4)

/**
 * Нижняя панель стикеров — точная копия feder_bot_sheet.html.
 *
 * - Drag handle сверху
 * - Поиск
 * - Заголовок пака + счётчик
 * - Сетка 3 в ряд с матрёшками (Canvas)
 * - Нижнее меню: ⚙️ | Стикеры | Эмодзи | +
 */
@Composable
fun StickerSheet(
    onDismiss: () -> Unit,
    onStickerClick: (StickerItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(StickerTab.STICKERS) }
    var expanded by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (expanded) Modifier.fillMaxHeight(0.92f) else Modifier.fillMaxHeight(0.62f))
                .align(Alignment.BottomCenter)
                .background(
                    SheetBg,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clickable(enabled = false) {} // перехватываем клик
        ) {
            // ─── Drag handle ───
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, drag ->
                                dragOffset += drag
                                if (dragOffset < -80f && !expanded) {
                                    expanded = true
                                    dragOffset = 0f
                                } else if (dragOffset > 80f && expanded) {
                                    expanded = false
                                    dragOffset = 0f
                                }
                            },
                            onDragEnd = { dragOffset = 0f }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HandleColor)
                )
            }

            // ─── Поиск ───
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = TextOn, fontSize = 14.sp),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("Найти стикер", color = TextMuted, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ─── Заголовок пака ───
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Матрёшка",
                    color = TextOn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${MATRYOSHKA_PACK.size} стикеров",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // ─── Сетка стикеров ───
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(MATRYOSHKA_PACK, key = { it.id }) { sticker ->
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStickerClick(sticker) },
                        contentAlignment = Alignment.Center
                    ) {
                        MatryoshkaSticker(sticker)
                    }
                }
            }

            // ─── Нижнее меню ───
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(SheetBg)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ⚙️ Настройки
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Табы
                StickerTabChip(
                    "Стикеры",
                    active = activeTab == StickerTab.STICKERS,
                    onClick = { activeTab = StickerTab.STICKERS }
                )
                Spacer(Modifier.width(4.dp))
                StickerTabChip(
                    "Эмодзи",
                    active = activeTab == StickerTab.EMOJI,
                    onClick = { activeTab = StickerTab.EMOJI }
                )

                Spacer(Modifier.weight(1f))

                // +
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Добавить",
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StickerTabChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) ChipActive else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (active) Color.White else TextMuted,
            fontSize = 14.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Рисуем матрёшку-стикер через Canvas.
 * drawType 1..18 — разные позы/выражения.
 */
@Composable
fun MatryoshkaSticker(sticker: StickerItem) {
    Canvas(Modifier.fillMaxSize().padding(6.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val scale = minOf(w, h) / 100f

        fun s(v: Float) = v * scale

        // ─── Тело (синее платье) ───
        val bodyPath = Path().apply {
            moveTo(cx - s(28), h - s(8))
            cubicTo(
                cx - s(32), h - s(30),
                cx - s(24), h - s(50),
                cx, h - s(50)
            )
            cubicTo(
                cx + s(24), h - s(50),
                cx + s(32), h - s(30),
                cx + s(28), h - s(8)
            )
            close()
        }
        drawPath(bodyPath, color = MatryoshkaBlue)

        // ─── Лицо (круг) ───
        drawCircle(
            color = MatryoshkaSkin,
            radius = s(20),
            center = Offset(cx, h - s(58))
        )

        // ─── Красный платок ───
        val scarfPath = Path().apply {
            moveTo(cx - s(24), h - s(62))
            quadraticBezierTo(cx, h - s(90), cx + s(24), h - s(62))
            quadraticBezierTo(cx + s(16), h - s(60), cx, h - s(68))
            quadraticBezierTo(cx - s(16), h - s(60), cx - s(24), h - s(62))
            close()
        }
        drawPath(scarfPath, color = MatryoshkaRed)

        // ─── Глаза (в зависимости от типа) ───
        val eyeY = h - s(58)
        val eyeOffset = s(8)

        when (sticker.drawType % 4) {
            0 -> {
                // Открытые глаза с зрачками
                drawCircle(Color.White, radius = s(4), center = Offset(cx - eyeOffset, eyeY))
                drawCircle(Color.White, radius = s(4), center = Offset(cx + eyeOffset, eyeY))
                drawCircle(Color(0xFF222222), radius = s(2.2f), center = Offset(cx - eyeOffset, eyeY + s(0.5f)))
                drawCircle(Color(0xFF222222), radius = s(2.2f), center = Offset(cx + eyeOffset, eyeY + s(0.5f)))
            }
            1 -> {
                // Закрытые (радостные)
                drawArc(
                    color = Color(0xFF222222),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - eyeOffset - s(4), eyeY - s(3)),
                    size = Size(s(8), s(6)),
                    style = Stroke(width = s(1.5f))
                )
                drawArc(
                    color = Color(0xFF222222),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx + eyeOffset - s(4), eyeY - s(3)),
                    size = Size(s(8), s(6)),
                    style = Stroke(width = s(1.5f))
                )
            }
            2 -> {
                // Спящие (закрытые горизонтально)
                drawLine(
                    Color(0xFF222222),
                    Offset(cx - eyeOffset - s(3), eyeY),
                    Offset(cx - eyeOffset + s(3), eyeY),
                    strokeWidth = s(1.5f)
                )
                drawLine(
                    Color(0xFF222222),
                    Offset(cx + eyeOffset - s(3), eyeY),
                    Offset(cx + eyeOffset + s(3), eyeY),
                    strokeWidth = s(1.5f)
                )
            }
            3 -> {
                // Сердечки
                drawHeart(cx - eyeOffset, eyeY, s(5), MatryoshkaRed)
                drawHeart(cx + eyeOffset, eyeY, s(5), MatryoshkaRed)
            }
        }

        // ─── Рот (улыбка) ───
        drawArc(
            color = Color(0xFF222222),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - s(6), h - s(54)),
            size = Size(s(12), s(6)),
            style = Stroke(width = s(1.5f))
        )

        // ─── Текст (если есть) ───
        // Текст рендерится через Text() поверх Canvas — сложно в Canvas
        // Поэтому текст не рисуем внутри Canvas — он в отдельном слое ниже
    }

    // Текст под Canvas
    if (sticker.text.isNotEmpty()) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                sticker.text,
                color = MatryoshkaRed,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

/**
 * Сердечко для глаз.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeart(
    cx: Float, cy: Float, size: Float, color: Color
) {
    val path = Path().apply {
        moveTo(cx, cy + size * 0.6f)
        cubicTo(
            cx - size * 1.2f, cy - size * 0.4f,
            cx - size * 0.4f, cy - size * 1.2f,
            cx, cy - size * 0.3f
        )
        cubicTo(
            cx + size * 0.4f, cy - size * 1.2f,
            cx + size * 1.2f, cy - size * 0.4f,
            cx, cy + size * 0.6f
        )
        close()
    }
    drawPath(path, color)
}
