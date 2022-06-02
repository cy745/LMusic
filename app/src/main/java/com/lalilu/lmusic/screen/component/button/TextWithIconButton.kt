package com.lalilu.lmusic.screen.component.button

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun TextWithIconButton(
    @StringRes textRes: Int,
    @DrawableRes iconRes: Int? = null,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit = {},
) = TextWithIconButton(
    text = stringResource(id = textRes),
    iconPainter = iconRes?.let { painterResource(id = it) },
    shape = shape,
    color = color,
    onClick = onClick
)

@Composable
fun TextWithIconButton(
    text: String,
    showIcon: Boolean = true,
    iconPainter: Painter? = null,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            backgroundColor = color.copy(0.15f)
        )
    ) {
        AnimatedVisibility(visible = iconPainter != null && showIcon) {
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