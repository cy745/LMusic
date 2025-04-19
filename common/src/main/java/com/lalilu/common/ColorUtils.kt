package com.lalilu.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.palette.graphics.Palette

fun Palette?.getColorPair(): Pair<Color, Color> {
    if (this == null) return Color.DarkGray to Color.White

    val bgColor = dominantSwatch?.rgb
        ?.let { Color(it) }
        ?: Color.DarkGray

    val contentColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White

    return bgColor to contentColor
}
