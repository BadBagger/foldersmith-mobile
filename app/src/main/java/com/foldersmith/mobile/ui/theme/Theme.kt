package com.foldersmith.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F8A5B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5FA86B),
    tertiary = Color(0xFFF2B84B),
    surface = Color(0xFFFAFFF9),
    surfaceVariant = Color(0xFFE3F4E7),
    background = Color(0xFFFAFFF9),
    onSurface = Color(0xFF172019)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DDC9A),
    onPrimary = Color(0xFF06391F),
    secondary = Color(0xFFA7D8A9),
    tertiary = Color(0xFFFFD36A),
    surface = Color(0xFF111811),
    surfaceVariant = Color(0xFF263528),
    background = Color(0xFF0C120D),
    onSurface = Color(0xFFE9F4EA)
)

@Composable
fun FolderSmithTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
