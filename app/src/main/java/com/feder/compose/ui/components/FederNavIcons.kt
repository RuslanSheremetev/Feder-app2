package com.feder.compose.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

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
