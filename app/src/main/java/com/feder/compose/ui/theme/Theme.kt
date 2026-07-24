package com.feder.compose.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// Dark Theme Colors (оригинальные)
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

// Совместимость со старым кодом — используют MaterialTheme.colorScheme
// (экраны должны использовать эти переменные вместо прямых DarkColors)
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

fun updateThemeColors(isDark: Boolean) {
    if (isDark) {
        Background = DarkBackground; Surface = DarkSurface
        SurfaceContainerLow = DarkSurfaceContainerLow; SurfaceContainerHigh = DarkSurfaceContainerHigh
        SurfaceContainerHighest = DarkSurfaceContainerHighest; SurfaceVariant = DarkSurfaceVariant
        Primary = DarkPrimary; PrimaryContainer = DarkPrimaryContainer
        OnPrimary = DarkOnPrimary; OnPrimaryContainer = DarkOnPrimaryContainer
        Secondary = DarkSecondary; SecondaryContainer = DarkSecondaryContainer
        OnSecondary = DarkOnSecondary; Tertiary = DarkTertiary
        Error = DarkError; OnSurface = DarkOnSurface
        OnSurfaceVariant = DarkOnSurfaceVariant; Outline = DarkOutline
        OutlineVariant = DarkOutlineVariant; OnlineGreen = DarkOnlineGreen; Muted = DarkMuted
    } else {
        Background = LightBackground; Surface = LightSurface
        SurfaceContainerLow = LightSurfaceContainerLow; SurfaceContainerHigh = LightSurfaceContainerHigh
        SurfaceContainerHighest = LightSurfaceContainerHighest; SurfaceVariant = LightSurfaceVariant
        Primary = LightPrimary; PrimaryContainer = LightPrimaryContainer
        OnPrimary = LightOnPrimary; OnPrimaryContainer = LightOnPrimaryContainer
        Secondary = LightSecondary; SecondaryContainer = LightSecondaryContainer
        OnSecondary = LightOnSecondary; Tertiary = LightTertiary
        Error = LightError; OnSurface = LightOnSurface
        OnSurfaceVariant = LightOnSurfaceVariant; Outline = LightOutline
        OutlineVariant = LightOutlineVariant; OnlineGreen = LightOnlineGreen; Muted = LightMuted
    }
}

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

// CompositionLocal для доступа к теме из любого экрана
data class ThemeController(val isDark: Boolean, val onToggle: () -> Unit)
val LocalDarkTheme = staticCompositionLocalOf { ThemeController(true, {}) }

@Composable
fun FederTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
