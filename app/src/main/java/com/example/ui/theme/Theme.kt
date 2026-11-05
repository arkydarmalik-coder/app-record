package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ScreenRecorderDarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = ElectricPurpleVariant,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E4A5C),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = EmeraldSuccess,
    onTertiary = Color.Black,
    error = CoralRecordingRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun AppScreenRecordingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ScreenRecorderDarkColorScheme,
        typography = Typography,
        content = content
    )
}
