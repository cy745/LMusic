package com.lalilu.lmusic.compose.component.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    text: String,
    iconPainter: Painter? = null,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colors.primary,
    showIcon: () -> Boolean = { false },
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            backgroundColor = color.copy(0.15f)
        )
    ) {
        AnimatedVisibility(visible = iconPainter != null && showIcon()) {
            Icon(
                modifier = Modifier
                    .padding(end = 5.dp)
                    .size(20.dp),
                painter = iconPainter!!,
                contentDescription = text,
            )
        }
        Text(text = text)
    }
}