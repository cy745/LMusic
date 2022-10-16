package com.lalilu.lmusic.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int?,
    text: () -> String,
    showIcon: () -> Boolean = { false },
    shape: Shape = RoundedCornerShape(10.dp),
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit = {}
) {
    val textR = remember { text() }

    TextButton(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            backgroundColor = color.copy(0.15f)
        )
    ) {
        AnimatedVisibility(visible = icon != null && showIcon()) {
            Icon(
                modifier = Modifier
                    .padding(end = 5.dp)
                    .size(20.dp),
                painter = painterResource(id = icon!!),
                contentDescription = textR,
            )
        }
        Text(text = textR)
    }
}