package com.feder.compose.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// Dark Theme Colors
val DarkBackground = Color(0xFF131313)
val DarkSurface = Color(0xFF131313)
val DarkSurfaceContainerLow = Color(0xFF1C1B1B)
val DarkSurfaceContainerHigh = Color(0xFF2A2A2A)
val DarkSurfaceContainerHighest = Color(0xFF353534)
val DarkSurfaceVariant = Color(0xFF353534)
val DarkPrimary = Color(0xFFA1C9FF)
val DarkPrimaryContainer = Color(0xFF339DFF)
val DarkOnPrimary = Color(0xFF00325A)
val DarkOnPrimaryContainer = Color(0xFF00335C)
val DarkSecondary = Color(0xFFC8C6C5)
val DarkSecondaryContainer = Color(0xFF474746)
val DarkOnSecondary = Color(0xFF303030)
val DarkTertiary = Color(0xFFFFB877)
val DarkError = Color(0xFFFFB4AB)
val DarkOnSurface = Color(0xFFE5E2E1)
val DarkOnSurfaceVariant = Color(0xFFC0C7D4)
val DarkOutline = Color(0xFF8A919E)
val DarkOutlineVariant = Color(0xFF404752)
val DarkOnlineGreen = Color(0xFF41B35D)
val DarkMuted = Color(0xFFFFB4AB)

// Light Theme Colors
val LightBackground = Color(0xFFF8F9FF)
val LightSurface = Color(0xFFF8F9FF)
val LightSurfaceContainerLow = Color(0xFFF0F1F8)
val LightSurfaceContainerHigh = Color(0xFFE0E2EB)
val LightSurfaceContainerHighest = Color(0xFFD6D9E3)
val LightSurfaceVariant = Color(0xFFE0E2EB)
val LightPrimary = Color(0xFF1A5489)
val LightPrimaryContainer = Color(0xFF339DFF)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnPrimaryContainer = Color(0xFFFFFFFF)
val LightSecondary = Color(0xFF5A5A5A)
val LightSecondaryContainer = Color(0xFFE0E0E0)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightTertiary = Color(0xFFCC6600)
val LightError = Color(0xFFBA1A1A)
val LightOnSurface = Color(0xFF1A1B20)
val LightOnSurfaceVariant = Color(0xFF43474F)
val LightOutline = Color(0xFF74777F)
val LightOutlineVariant = Color(0xFFC4C6D0)
val LightOnlineGreen = Color(0xFF2E7D32)
val LightMuted = Color(0xFFBA1A1A)

// Reactive Theme Colors object
object ThemeColors {
    var Background by mutableStateOf(DarkBackground)
    var Surface by mutableStateOf(DarkSurface)
    var SurfaceContainerLow by mutableStateOf(DarkSurfaceContainerLow)
    var SurfaceContainerHigh by mutableStateOf(DarkSurfaceContainerHigh)
    var SurfaceContainerHighest by mutableStateOf(DarkSurfaceContainerHighest)
    var SurfaceVariant by mutableStateOf(DarkSurfaceVariant)
    var Primary by mutableStateOf(DarkPrimary)
    var PrimaryContainer by mutableStateOf(DarkPrimaryContainer)
    var OnPrimary by mutableStateOf(DarkOnPrimary)
    var OnPrimaryContainer by mutableStateOf(DarkOnPrimaryContainer)
    var Secondary by mutableStateOf(DarkSecondary)
    var SecondaryContainer by mutableStateOf(DarkSecondaryContainer)
    var OnSecondary by mutableStateOf(DarkOnSecondary)
    var Tertiary by mutableStateOf(DarkTertiary)
    var Error by mutableStateOf(DarkError)
    var OnSurface by mutableStateOf(DarkOnSurface)
    var OnSurfaceVariant by mutableStateOf(DarkOnSurfaceVariant)
    var Outline by mutableStateOf(DarkOutline)
    var OutlineVariant by mutableStateOf(DarkOutlineVariant)
    var OnlineGreen by mutableStateOf(DarkOnlineGreen)
    var Muted by mutableStateOf(DarkMuted)
}

// Экспорт для совместимости (все экраны используют эти имена)
val Background get() = ThemeColors.Background
val Surface get() = ThemeColors.Surface
val SurfaceContainerLow get() = ThemeColors.SurfaceContainerLow
val SurfaceContainerHigh get() = ThemeColors.SurfaceContainerHigh
val SurfaceContainerHighest get() = ThemeColors.SurfaceContainerHighest
val SurfaceVariant get() = ThemeColors.SurfaceVariant
val Primary get() = ThemeColors.Primary
val PrimaryContainer get() = ThemeColors.PrimaryContainer
val OnPrimary get() = ThemeColors.OnPrimary
val OnPrimaryContainer get() = ThemeColors.OnPrimaryContainer
val Secondary get() = ThemeColors.Secondary
val SecondaryContainer get() = ThemeColors.SecondaryContainer
val OnSecondary get() = ThemeColors.OnSecondary
val Tertiary get() = ThemeColors.Tertiary
val Error get() = ThemeColors.Error
val OnSurface get() = ThemeColors.OnSurface
val OnSurfaceVariant get() = ThemeColors.OnSurfaceVariant
val Outline get() = ThemeColors.Outline
val OutlineVariant get() = ThemeColors.OutlineVariant
val OnlineGreen get() = ThemeColors.OnlineGreen
val Muted get() = ThemeColors.Muted

fun updateThemeColors(isDark: Boolean) {
    if (isDark) {
        ThemeColors.Background = DarkBackground
        ThemeColors.Surface = DarkSurface
        ThemeColors.SurfaceContainerLow = DarkSurfaceContainerLow
        ThemeColors.SurfaceContainerHigh = DarkSurfaceContainerHigh
        ThemeColors.SurfaceContainerHighest = DarkSurfaceContainerHighest
        ThemeColors.SurfaceVariant = DarkSurfaceVariant
        ThemeColors.Primary = DarkPrimary
        ThemeColors.PrimaryContainer = DarkPrimaryContainer
        ThemeColors.OnPrimary = DarkOnPrimary
        ThemeColors.OnPrimaryContainer = DarkOnPrimaryContainer
        ThemeColors.Secondary = DarkSecondary
        ThemeColors.SecondaryContainer = DarkSecondaryContainer
        ThemeColors.OnSecondary = DarkOnSecondary
        ThemeColors.Tertiary = DarkTertiary
        ThemeColors.Error = DarkError
        ThemeColors.OnSurface = DarkOnSurface
        ThemeColors.OnSurfaceVariant = DarkOnSurfaceVariant
        ThemeColors.Outline = DarkOutline
        ThemeColors.OutlineVariant = DarkOutlineVariant
        ThemeColors.OnlineGreen = DarkOnlineGreen
        ThemeColors.Muted = DarkMuted
    } else {
        ThemeColors.Background = LightBackground
        ThemeColors.Surface = LightSurface
        ThemeColors.SurfaceContainerLow = LightSurfaceContainerLow
        ThemeColors.SurfaceContainerHigh = LightSurfaceContainerHigh
        ThemeColors.SurfaceContainerHighest = LightSurfaceContainerHighest
        ThemeColors.SurfaceVariant = LightSurfaceVariant
        ThemeColors.Primary = LightPrimary
        ThemeColors.PrimaryContainer = LightPrimaryContainer
        ThemeColors.OnPrimary = LightOnPrimary
        ThemeColors.OnPrimaryContainer = LightOnPrimaryContainer
        ThemeColors.Secondary = LightSecondary
        ThemeColors.SecondaryContainer = LightSecondaryContainer
        ThemeColors.OnSecondary = LightOnSecondary
        ThemeColors.Tertiary = LightTertiary
        ThemeColors.Error = LightError
        ThemeColors.OnSurface = LightOnSurface
        ThemeColors.OnSurfaceVariant = LightOnSurfaceVariant
        ThemeColors.Outline = LightOutline
        ThemeColors.OutlineVariant = LightOutlineVariant
        ThemeColors.OnlineGreen = LightOnlineGreen
        ThemeColors.Muted = LightMuted
    }
}

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary, onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    tertiary = DarkTertiary, error = DarkError,
    background = DarkBackground, onBackground = DarkOnSurface,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline, outlineVariant = DarkOutlineVariant,
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary, onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer, onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary, onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    tertiary = LightTertiary, error = LightError,
    background = LightBackground, onBackground = LightOnSurface,
    surface = LightSurface, onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant, onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline, outlineVariant = LightOutlineVariant,
)

data class ThemeController(val isDark: Boolean, val onToggle: () -> Unit)
val LocalDarkTheme = staticCompositionLocalOf { ThemeController(true, {}) }

@Composable
fun FederTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
