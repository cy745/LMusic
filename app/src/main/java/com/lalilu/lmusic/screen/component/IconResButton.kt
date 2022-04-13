package com.lalilu.lmusic.screen.component

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

@Composable
fun IconResButton(
    @DrawableRes iconRes: Int,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1f,
    color: Color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        .copy(alpha = alpha),
    onClick: () -> Unit = {}
) = IconResButton(
    iconPainter = painterResource(id = iconRes),
    color = color,
    alpha = alpha,
    onClick = onClick
)

@Composable
fun IconResButton(
    iconPainter: Painter,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1f,
    color: Color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        .copy(alpha = alpha),
    onClick: () -> Unit = {}
) {
    IconButton(onClick = onClick) {
        Image(
            painter = iconPainter,
            contentDescription = "",
            colorFilter = ColorFilter.tint(color = color)
        )
    }
}