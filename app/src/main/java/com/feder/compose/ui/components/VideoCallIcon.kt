package com.feder.compose.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Иконка видеокамеры — точная копия Material Symbols "videocam" outlined.
 *
 * Форма: прямоугольник со скруглёнными углами + трапеция справа (объектив).
 * Обводка толстая (2dp), углы скруглённые.
 */
@Composable
fun VideoCallIcon(
    tint: Color = Color.White,
    size: Dp = 26.dp,
    strokeWidth: Dp = 2.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val s = strokeWidth.toPx()

        val path = Path().apply {
            // Прямоугольник: 3..18 по X, 5..23 по Y (из viewBox 28x28)
            val scale = w / 28f
            fun x(v: Float) = v * scale
            fun y(v: Float) = v * scale

            moveTo(x(5.5f), y(5f))
            lineTo(x(15.5f), y(5f))
            quadraticBezierTo(x(18f), y(5f), x(18f), y(7.5f))
            lineTo(x(18f), y(10.5f))
            lineTo(x(23.4f), y(7.5f))
            quadraticBezierTo(x(24.5f), y(7.22f), x(24.5f), y(8.15f))
            lineTo(x(24.5f), y(19.85f))
            quadraticBezierTo(x(24.5f), y(20.78f), x(23.4f), y(20.5f))
            lineTo(x(18f), y(17.5f))
            lineTo(x(18f), y(20.5f))
            quadraticBezierTo(x(18f), y(23f), x(15.5f), y(23f))
            lineTo(x(5.5f), y(23f))
            quadraticBezierTo(x(3f), y(23f), x(3f), y(20.5f))
            lineTo(x(3f), y(7.5f))
            quadraticBezierTo(x(3f), y(5f), x(5.5f), y(5f))
            close()
        }

        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = s,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Иконка телефона (трубка) — Material Symbols "call" outlined.
 */
@Composable
fun PhoneCallIcon(
    tint: Color = Color.White,
    size: Dp = 24.dp,
    strokeWidth: Dp = 2.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val s = strokeWidth.toPx()
        val scale = w / 28f
        fun v(x: Float) = x * scale

        val path = Path().apply {
            moveTo(v(22f), v(18.92f))
            lineTo(v(22f), v(21.42f))
            quadraticBezierTo(v(22f), v(22.5f), v(20.82f), v(22.5f))
            cubicTo(
                v(10.1f), v(22.5f), v(3.5f), v(15.9f), v(3.5f), v(5.18f)
            )
            quadraticBezierTo(v(3.5f), v(4f), v(4.58f), v(4f))
            lineTo(v(7.08f), v(4f))
            quadraticBezierTo(v(8.16f), v(4f), v(8.5f), v(5.72f))
            cubicTo(
                v(8.82f), v(7.05f), v(9.06f), v(8.31f), v(9.2f), v(9.53f)
            )
            quadraticBezierTo(v(9.26f), v(10.03f), v(8.75f), v(10.55f))
            lineTo(v(7.91f), v(11.09f))
            cubicTo(
                v(9.05f), v(13.05f), v(10.95f), v(14.95f), v(12.91f), v(16.09f)
            )
            lineTo(v(13.45f), v(15.25f))
            quadraticBezierTo(v(13.97f), v(14.74f), v(14.47f), v(14.8f))
            cubicTo(
                v(15.69f), v(14.94f), v(16.95f), v(15.18f), v(18.28f), v(15.5f)
            )
            quadraticBezierTo(v(20f), v(15.84f), v(20f), v(16.92f))
            lineTo(v(20f), v(18.92f))
            close()
        }

        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = s,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Иконка "три точки" (⋮) — вертикальное меню.
 */
@Composable
fun DotsVerticalIcon(
    tint: Color = Color.White,
    size: Dp = 24.dp,
    dotRadius: Dp = 1.5.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val scale = w / 28f
        fun v(x: Float) = x * scale
        val r = dotRadius.toPx()

        drawCircle(tint, radius = r, center = androidx.compose.ui.geometry.Offset(v(14f), v(7f)))
        drawCircle(tint, radius = r, center = androidx.compose.ui.geometry.Offset(v(14f), v(14f)))
        drawCircle(tint, radius = r, center = androidx.compose.ui.geometry.Offset(v(14f), v(21f)))
    }
}
