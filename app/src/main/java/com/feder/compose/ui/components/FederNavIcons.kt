package com.feder.compose.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Chats — точь-в-точь как в HTML-варианте */
val FederChatsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FederChats",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 11.5f)
            curveTo(21f, 12.84f, 20.68f, 14.13f, 20.1f, 15.3f)
            curveTo(19.41f, 16.71f, 18.33f, 17.9f, 16.99f, 18.72f)
            curveTo(15.66f, 19.53f, 14.14f, 19.96f, 12.59f, 19.97f)
            curveTo(11.24f, 19.97f, 9.93f, 19.66f, 8.75f, 19.07f)
            lineTo(3f, 21f)
            lineTo(4.9f, 15.3f)
            curveTo(4.31f, 14.13f, 4f, 12.84f, 4f, 11.5f)
            curveTo(4f, 9.95f, 4.42f, 8.45f, 5.23f, 7.11f)
            curveTo(6.05f, 5.78f, 7.24f, 4.7f, 8.65f, 4.01f)
            curveTo(10.07f, 3.32f, 11.65f, 3.05f, 13.2f, 3.25f)
            curveTo(15.3f, 3.5f, 17.2f, 4.6f, 18.52f, 6.27f)
            curveTo(19.83f, 7.93f, 20.55f, 10.02f, 20.52f, 12.17f)
            close()
        }
    }.build()
}

/** Contacts — точь-в-точь как в HTML-варианте */
val FederContactsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FederContacts",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(16f, 8f)
            curveTo(16f, 10.21f, 14.21f, 12f, 12f, 12f)
            curveTo(9.79f, 12f, 8f, 10.21f, 8f, 8f)
            curveTo(8f, 5.79f, 9.79f, 4f, 12f, 4f)
            curveTo(14.21f, 4f, 16f, 5.79f, 16f, 8f)
            close()
        }
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 21f)
            lineTo(4f, 19f)
            curveTo(4f, 15.69f, 6.69f, 13f, 10f, 13f)
            lineTo(14f, 13f)
            curveTo(17.31f, 13f, 20f, 15.69f, 20f, 19f)
            lineTo(20f, 21f)
        }
    }.build()
}

/**
 * Settings — точь-в-точь как в HTML-образце.
 * Копия из SVG: <circle cx=12 cy=12 r=3 /> + path "M19.4 15a1.65..."
 * (feather-icons "settings", но с правильным viewBox 24×24 — координаты
 *  из SVG отмасштабированы)
 */
val FederSettingsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FederSettings",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        // Inner circle (r=3)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(15f, 12f)
            curveTo(15f, 13.66f, 13.66f, 15f, 12f, 15f)
            curveTo(10.34f, 15f, 9f, 13.66f, 9f, 12f)
            curveTo(9f, 10.34f, 10.34f, 9f, 12f, 9f)
            curveTo(13.66f, 9f, 15f, 10.34f, 15f, 12f)
            close()
        }
        // Outer gear — упрощённый путь из feather icons "settings" (12 зубьев + центр)
        // Весь путь умещается в viewBox 24×24
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // M19.4 15 a1.65 1.65 0 0 0 .33 1.82 l.06 .06 a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0 l-.06-.06 a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51 V21 a2 2 0 0 1-2 2 2 2 0 0 1-2-2 v-.09 A1.65 1.65 0 0 0 9 19.4 a1.65 1.65 0 0 0-1.82.33 l-.06 .06 a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83 l.06-.06 a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1 H3 a2 2 0 0 1-2-2 2 2 0 0 1 2-2 h.09 A1.65 1.65 0 0 0 4.6 9 a1.65 1.65 0 0 0-.33-1.82 l-.06-.06 a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0 l.06.06 a1.65 1.65 0 0 0 1.82.33 H9 a1.65 1.65 0 0 0 1-1.51 V3 a2 2 0 0 1 2-2 2 2 0 0 1 2 2 v.09 a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33 l.06-.06 a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83 l-.06.06 a1.65 1.65 0 0 0-.33 1.82 V9 a1.65 1.65 0 0 0 1.51 1 H21 a2 2 0 0 1 2 2 2 2 0 0 1-2 2 h-.09 a1.65 1.65 0 0 0-1.51 1 z

            moveTo(19.4f, 15f)
            curveTo(19.6f, 15.6f, 19.7f, 16.25f, 19.7f, 16.82f)
            lineTo(19.76f, 16.88f)
            curveTo(20.6f, 17.71f, 20.6f, 19.04f, 19.76f, 19.88f)
            curveTo(19.35f, 20.29f, 18.79f, 20.48f, 18.24f, 20.48f)
            curveTo(17.69f, 20.48f, 17.13f, 20.29f, 16.72f, 19.88f)
            lineTo(16.66f, 19.82f)
            curveTo(16.24f, 20.05f, 15.78f, 20.19f, 15.31f, 20.22f)
            lineTo(15.22f, 20.23f)
            curveTo(15.02f, 20.79f, 14.72f, 21.31f, 14.31f, 21.71f)
            curveTo(13.9f, 22.13f, 13.34f, 22.32f, 12.79f, 22.32f)
            curveTo(12.24f, 22.32f, 11.68f, 22.13f, 11.27f, 21.71f)
            curveTo(10.86f, 21.31f, 10.56f, 20.79f, 10.36f, 20.23f)
            lineTo(10.27f, 20.22f)
            curveTo(9.8f, 20.19f, 9.34f, 20.05f, 8.92f, 19.82f)
            lineTo(8.86f, 19.88f)
            curveTo(8.45f, 20.29f, 7.89f, 20.48f, 7.34f, 20.48f)
            curveTo(6.79f, 20.48f, 6.23f, 20.29f, 5.82f, 19.88f)
            curveTo(4.98f, 19.04f, 4.98f, 17.71f, 5.82f, 16.88f)
            lineTo(5.88f, 16.82f)
            curveTo(5.88f, 16.25f, 5.98f, 15.6f, 6.18f, 15f)
            lineTo(6.17f, 14.9f)
            curveTo(5.61f, 14.7f, 5.09f, 14.4f, 4.69f, 13.99f)
            curveTo(3.85f, 13.15f, 3.85f, 11.82f, 4.69f, 10.99f)
            curveTo(5.09f, 10.58f, 5.61f, 10.28f, 6.17f, 10.08f)
            lineTo(6.18f, 9.98f)
            curveTo(5.98f, 9.38f, 5.88f, 8.73f, 5.88f, 8.16f)
            lineTo(5.82f, 8.1f)
            curveTo(4.98f, 7.27f, 4.98f, 5.94f, 5.82f, 5.1f)
            curveTo(6.23f, 4.69f, 6.79f, 4.5f, 7.34f, 4.5f)
            curveTo(7.89f, 4.5f, 8.45f, 4.69f, 8.86f, 5.1f)
            lineTo(8.92f, 5.16f)
            curveTo(9.34f, 4.93f, 9.8f, 4.79f, 10.27f, 4.76f)
            lineTo(10.36f, 4.75f)
            curveTo(10.56f, 4.19f, 10.86f, 3.67f, 11.27f, 3.27f)
            curveTo(11.68f, 2.85f, 12.24f, 2.66f, 12.79f, 2.66f)
            curveTo(13.34f, 2.66f, 13.9f, 2.85f, 14.31f, 3.27f)
            curveTo(14.72f, 3.67f, 15.02f, 4.19f, 15.22f, 4.75f)
            lineTo(15.31f, 4.76f)
            curveTo(15.78f, 4.79f, 16.24f, 4.93f, 16.66f, 5.16f)
            lineTo(16.72f, 5.1f)
            curveTo(17.13f, 4.69f, 17.69f, 4.5f, 18.24f, 4.5f)
            curveTo(18.79f, 4.5f, 19.35f, 4.69f, 19.76f, 5.1f)
            curveTo(20.6f, 5.94f, 20.6f, 7.27f, 19.76f, 8.1f)
            lineTo(19.7f, 8.16f)
            curveTo(19.7f, 8.73f, 19.6f, 9.38f, 19.4f, 9.98f)
            lineTo(19.41f, 10.08f)
            curveTo(19.97f, 10.28f, 20.49f, 10.58f, 20.89f, 10.99f)
            curveTo(21.73f, 11.82f, 21.73f, 13.15f, 20.89f, 13.99f)
            curveTo(20.49f, 14.4f, 19.97f, 14.7f, 19.41f, 14.9f)
            close()
        }
    }.build()
}
