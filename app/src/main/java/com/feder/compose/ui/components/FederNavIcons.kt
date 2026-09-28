package com.feder.compose.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Chats — Feather "message-circle" (точь-в-точь как в HTML-образце) */
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

/** Contacts — Feather "user" */
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
            moveTo(20f, 21f)
            lineTo(20f, 19f)
            curveTo(20f, 17.94f, 19.58f, 16.93f, 18.83f, 16.17f)
            curveTo(18.07f, 15.42f, 17.06f, 15f, 16f, 15f)
            lineTo(8f, 15f)
            curveTo(6.94f, 15f, 5.93f, 15.42f, 5.17f, 16.17f)
            curveTo(4.42f, 16.93f, 4f, 17.94f, 4f, 19f)
            lineTo(4f, 21f)
        }
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 11f)
            curveTo(14.21f, 11f, 16f, 9.21f, 16f, 7f)
            curveTo(16f, 4.79f, 14.21f, 3f, 12f, 3f)
            curveTo(9.79f, 3f, 8f, 4.79f, 8f, 7f)
            curveTo(8f, 9.21f, 9.79f, 11f, 12f, 11f)
            close()
        }
    }.build()
}

/**
 * Settings — Feather "settings" с ТОЧНЫМ pathData.
 * Это тот же SVG, что в HTML-образце.
 * Круг в центре + зубчатый контур (12 зубьев).
 */
val FederSettingsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FederSettings",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        // circle cx=12 cy=12 r=3
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
        // path M19.4 15a1.65 1.65 0 0 0 .33 1.82l... (Feather settings)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(19.4f, 15f)
            curveTo(19.59f, 15.44f, 19.69f, 15.93f, 19.69f, 16.42f)
            curveTo(19.69f, 16.91f, 19.59f, 17.4f, 19.4f, 17.84f)
            lineTo(19.46f, 17.9f)
            curveTo(19.85f, 18.29f, 20.16f, 18.75f, 20.36f, 19.27f)
            curveTo(20.56f, 19.79f, 20.64f, 20.35f, 20.6f, 20.9f)
            curveTo(20.56f, 21.45f, 20.4f, 21.99f, 20.13f, 22.48f)
            curveTo(19.86f, 22.96f, 19.49f, 23.39f, 19.04f, 23.72f)
            curveTo(18.59f, 24.05f, 18.08f, 24.28f, 17.54f, 24.39f)
            curveTo(17f, 24.5f, 16.44f, 24.49f, 15.9f, 24.36f)
            curveTo(15.36f, 24.24f, 14.86f, 24f, 14.42f, 23.67f)
            lineTo(14.36f, 23.73f)
            curveTo(14.12f, 24.11f, 13.79f, 24.42f, 13.4f, 24.64f)
            curveTo(13.01f, 24.86f, 12.58f, 24.98f, 12.14f, 24.98f)
            curveTo(11.7f, 24.98f, 11.27f, 24.86f, 10.88f, 24.64f)
            curveTo(10.49f, 24.42f, 10.16f, 24.11f, 9.92f, 23.73f)
            lineTo(9.86f, 23.67f)
            curveTo(9.42f, 24f, 8.92f, 24.24f, 8.38f, 24.36f)
            curveTo(7.84f, 24.49f, 7.28f, 24.5f, 6.74f, 24.39f)
            curveTo(6.2f, 24.28f, 5.69f, 24.05f, 5.24f, 23.72f)
            curveTo(4.79f, 23.39f, 4.42f, 22.96f, 4.15f, 22.48f)
            curveTo(3.88f, 21.99f, 3.72f, 21.45f, 3.68f, 20.9f)
            curveTo(3.64f, 20.35f, 3.72f, 19.79f, 3.92f, 19.27f)
            curveTo(4.12f, 18.75f, 4.43f, 18.29f, 4.82f, 17.9f)
            lineTo(4.88f, 17.84f)
            curveTo(4.69f, 17.4f, 4.59f, 16.91f, 4.59f, 16.42f)
            curveTo(4.59f, 15.93f, 4.69f, 15.44f, 4.88f, 15f)
            lineTo(4.82f, 14.94f)
            curveTo(4.43f, 14.55f, 4.12f, 14.09f, 3.92f, 13.57f)
            curveTo(3.72f, 13.05f, 3.64f, 12.49f, 3.68f, 11.94f)
            curveTo(3.72f, 11.39f, 3.88f, 10.85f, 4.15f, 10.36f)
            curveTo(4.42f, 9.88f, 4.79f, 9.45f, 5.24f, 9.12f)
            curveTo(5.69f, 8.79f, 6.2f, 8.56f, 6.74f, 8.45f)
            curveTo(7.28f, 8.34f, 7.84f, 8.35f, 8.38f, 8.48f)
            curveTo(8.92f, 8.6f, 9.42f, 8.84f, 9.86f, 9.17f)
            lineTo(9.92f, 9.11f)
            curveTo(10.16f, 8.73f, 10.49f, 8.42f, 10.88f, 8.2f)
            curveTo(11.27f, 7.98f, 11.7f, 7.86f, 12.14f, 7.86f)
            curveTo(12.58f, 7.86f, 13.01f, 7.98f, 13.4f, 8.2f)
            curveTo(13.79f, 8.42f, 14.12f, 8.73f, 14.36f, 9.11f)
            lineTo(14.42f, 9.17f)
            curveTo(14.86f, 8.84f, 15.36f, 8.6f, 15.9f, 8.48f)
            curveTo(16.44f, 8.35f, 17f, 8.34f, 17.54f, 8.45f)
            curveTo(18.08f, 8.56f, 18.59f, 8.79f, 19.04f, 9.12f)
            curveTo(19.49f, 9.45f, 19.86f, 9.88f, 20.13f, 10.36f)
            curveTo(20.4f, 10.85f, 20.56f, 11.39f, 20.6f, 11.94f)
            curveTo(20.64f, 12.49f, 20.56f, 13.05f, 20.36f, 13.57f)
            curveTo(20.16f, 14.09f, 19.85f, 14.55f, 19.46f, 14.94f)
            close()
        }
    }.build()
}
