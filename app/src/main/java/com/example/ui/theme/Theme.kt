package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = MyraBlack,
    primaryContainer = Color(0xFF00384D),
    onPrimaryContainer = NeonCyan,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4D0026),
    onSecondaryContainer = NeonMagenta,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    background = MyraBlack,
    onBackground = TextPrimary,
    surface = MyraDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = MyraCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = MyraCardBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyraAssistantTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyraAssistantTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

