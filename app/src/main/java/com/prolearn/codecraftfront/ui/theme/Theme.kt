package com.prolearn.codecraftfront.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color(0xFF1A0640),
    primaryContainer = Color(0xFF3A1A78),
    onPrimaryContainer = Color(0xFFE9DAFF),

    secondary = NeonCyan,
    onSecondary = Color(0xFF002B36),
    secondaryContainer = Color(0xFF003D52),
    onSecondaryContainer = Color(0xFFCFF7FF),

    tertiary = NeonOrange,
    onTertiary = Color(0xFF3A1500),
    tertiaryContainer = Color(0xFF6A3100),
    onTertiaryContainer = Color(0xFFFFDCC8),

    background = NightBg,
    onBackground = TextOnDark,
    surface = NightSurface,
    onSurface = TextOnDark,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = MutedOnDark,
    outline = NightOutline,
    outlineVariant = Color(0xFF2A1B58),

    error = ErrorRed,
    onError = Color(0xFF3F0012),
    errorContainer = Color(0xFF5A0024),
    onErrorContainer = Color(0xFFFFD9E0),

    inverseSurface = Color(0xFFEAE0FF),
    inverseOnSurface = Color(0xFF1A0F40),
    inversePrimary = Color(0xFF6A3EE6),
    scrim = Color(0xFF000000),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6A3EE6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF20005F),

    secondary = Color(0xFF0A8AB3),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFF7FF),
    onSecondaryContainer = Color(0xFF002B36),

    tertiary = Color(0xFFB45A1F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC8),
    onTertiaryContainer = Color(0xFF3A1500),

    background = DayBg,
    onBackground = TextOnLight,
    surface = DaySurface,
    onSurface = TextOnLight,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = MutedOnLight,
    outline = DayOutline,
    outlineVariant = Color(0xFFE7DBFF),

    error = ErrorRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFD9E0),
    onErrorContainer = Color(0xFF5A0024),

    inverseSurface = Color(0xFF1F1340),
    inverseOnSurface = Color(0xFFEAE0FF),
    inversePrimary = NeonPurple,
    scrim = Color(0xFF000000),
)

@Composable
fun CodeCraftFrontTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
