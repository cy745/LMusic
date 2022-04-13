package com.lalilu.lmusic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColors(
//    primary = Color(0xFFBB86FC),
//    primaryVariant = Color(0xFF3700B3),
//    secondary = Color(0xFF03DAC6),
//    secondaryVariant = Color(0xFF03DAC6),
    background = Color(0xFF1A1A1A),
//    surface = Color(0xFF121212),
//    error = Color(0xFFCF6679),
//    onPrimary = Color.Black,
//    onSecondary = Color.Black,
//    onBackground = Color.White,
//    onSurface = Color.White,
//    onError = Color.Black
)

private val LightColor = lightColors(
//    primary = Color(0xFF6200EE),
//    primaryVariant = Color(0xFF3700B3),
//    secondary = Color(0xFF03DAC6),
//    secondaryVariant = Color(0xFF018786),
    background = Color(0xFFF4F4F4),
//    surface = Color.White,
//    error = Color(0xFFB00020),
//    onPrimary = Color.White,
//    onSecondary = Color.Black,
//    onBackground = Color.Black,
//    onSurface = Color.Black,
//    onError = Color.White
)

@Composable
fun LMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit = {}
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColor,
        content = content
    )
}