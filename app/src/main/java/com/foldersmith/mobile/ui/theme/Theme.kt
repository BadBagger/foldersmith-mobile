package com.foldersmith.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2D6A6A),
    secondary = Color(0xFF7B5E2E),
    tertiary = Color(0xFF8B4D57),
    surface = Color(0xFFFFFBF4),
    surfaceVariant = Color(0xFFE7E1D7),
    background = Color(0xFFFFFBF4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF84D2CE),
    secondary = Color(0xFFE5C178),
    tertiary = Color(0xFFFFB2C1),
    surface = Color(0xFF151917),
    surfaceVariant = Color(0xFF404943),
    background = Color(0xFF101412)
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
