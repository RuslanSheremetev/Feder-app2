package com.feder.compose.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF131313)
val Surface = Color(0xFF131313)
val SurfaceContainerLow = Color(0xFF1C1B1B)
val SurfaceContainerHigh = Color(0xFF2A2A2A)
val SurfaceContainerHighest = Color(0xFF353534)
val SurfaceVariant = Color(0xFF353534)

val Primary = Color(0xFFA1C9FF)
val PrimaryContainer = Color(0xFF339DFF)
val OnPrimary = Color(0xFF00325A)
val OnPrimaryContainer = Color(0xFF00335C)

val Secondary = Color(0xFFC8C6C5)
val SecondaryContainer = Color(0xFF474746)
val OnSecondary = Color(0xFF303030)

val Tertiary = Color(0xFFFFB877)
val Error = Color(0xFFFFB4AB)

val OnSurface = Color(0xFFE5E2E1)
val OnSurfaceVariant = Color(0xFFC0C7D4)
val Outline = Color(0xFF8A919E)
val OutlineVariant = Color(0xFF404752)

val OnlineGreen = Color(0xFF41B35D)
val Muted = Color(0xFFFFB4AB)

private val FederColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    tertiary = Tertiary,
    error = Error,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

@Composable
fun FederTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FederColorScheme,
        content = content
    )
}
