package com.feder.compose.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Chats — точь-в-точь как в HTML-варианте 1 */
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

/** Contacts — точь-в-точь как в HTML-варианте 1 */
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

/** Settings — точь-в-точь как в HTML-варианте 1 */
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
        // Внешний шестиугольник
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12.22f, 2f)
            lineTo(13.78f, 2f)
            curveTo(14.02f, 2f, 14.24f, 2.13f, 14.35f, 2.34f)
            lineTo(15.2f, 3.97f)
            curveTo(15.31f, 4.18f, 15.55f, 4.31f, 15.79f, 4.31f)
            lineTo(17.61f, 4.31f)
            curveTo(17.85f, 4.31f, 18.08f, 4.44f, 18.19f, 4.65f)
            lineTo(19.75f, 7.35f)
            curveTo(19.86f, 7.56f, 19.86f, 7.82f, 19.75f, 8.03f)
            lineTo(18.84f, 9.66f)
            curveTo(18.73f, 9.87f, 18.73f, 10.13f, 18.84f, 10.34f)
            lineTo(19.75f, 11.97f)
            curveTo(19.86f, 12.18f, 19.86f, 12.44f, 19.75f, 12.65f)
            lineTo(18.19f, 15.35f)
            curveTo(18.08f, 15.56f, 17.85f, 15.69f, 17.61f, 15.69f)
            lineTo(15.79f, 15.69f)
            curveTo(15.55f, 15.69f, 15.31f, 15.82f, 15.2f, 16.03f)
            lineTo(14.35f, 17.66f)
            curveTo(14.24f, 17.87f, 14.02f, 18f, 13.78f, 18f)
            lineTo(10.66f, 18f)
            curveTo(10.42f, 18f, 10.19f, 17.87f, 10.08f, 17.66f)
            lineTo(9.23f, 16.03f)
            curveTo(9.12f, 15.82f, 8.89f, 15.69f, 8.65f, 15.69f)
            lineTo(6.83f, 15.69f)
            curveTo(6.58f, 15.69f, 6.36f, 15.56f, 6.25f, 15.35f)
            lineTo(4.69f, 12.65f)
            curveTo(4.58f, 12.44f, 4.58f, 12.18f, 4.69f, 11.97f)
            lineTo(5.6f, 10.34f)
            curveTo(5.71f, 10.13f, 5.71f, 9.87f, 5.6f, 9.66f)
            lineTo(4.69f, 8.03f)
            curveTo(4.58f, 7.82f, 4.58f, 7.56f, 4.69f, 7.35f)
            lineTo(6.25f, 4.65f)
            curveTo(6.36f, 4.44f, 6.58f, 4.31f, 6.83f, 4.31f)
            lineTo(8.65f, 4.31f)
            curveTo(8.89f, 4.31f, 9.12f, 4.18f, 9.23f, 3.97f)
            lineTo(10.08f, 2.34f)
            curveTo(10.19f, 2.13f, 10.42f, 2f, 10.66f, 2f)
            close()
        }
    }.build()
}
