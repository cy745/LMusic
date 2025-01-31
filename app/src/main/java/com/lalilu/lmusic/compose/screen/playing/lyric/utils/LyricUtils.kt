package com.lalilu.lmusic.compose.screen.playing.lyric.utils

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.io.File

/**
 * 读取字体文件，并将其转换成Compose可用的FontFamily
 *
 * @param path 字体所在路径
 * @return 字体文件对应的FontFamily
 */
@Composable
fun rememberFontFamilyFromPath(path: () -> String?): State<FontFamily?> {
    val fontFamily = remember { mutableStateOf<FontFamily?>(null) }

    LaunchedEffect(path()) {
        val fontFile = path()?.takeIf { it.isNotBlank() }
            ?.let { File(it) }
            ?.takeIf { it.exists() && it.canRead() }
            ?: return@LaunchedEffect

        fontFamily.value = runCatching { FontFamily(Typeface.createFromFile(fontFile)) }
            .getOrNull()
    }

    return fontFamily
}

/**
 * 将存储的Gravity的Int值转换成Compose可用的TextAlign
 */
@Composable
fun rememberTextAlignFromGravity(gravity: () -> Int?): TextAlign {
    return remember(gravity()) {
        when (gravity()) {
            0 -> TextAlign.Start
            1 -> TextAlign.Center
            2 -> TextAlign.End
            else -> TextAlign.Start
        }
    }
}

/**
 *  将存储的Int值转换成Compose可用的TextUnit
 */
@Composable
fun rememberTextSizeFromInt(textSize: () -> Int?): TextUnit {
    return remember(textSize()) { textSize()?.takeIf { it > 0 }?.sp ?: 26.sp }
}

fun normalized(start: Long, end: Long, current: Long): Float {
    if (start >= end) return 0f
    val result = (current - start).toFloat() / (end - start).toFloat()
    return result.coerceIn(0f, 1f)
}