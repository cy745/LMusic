package com.lalilu.lmusic.screen.component.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.lalilu.R

@Composable
fun TextVisibleToggleButton(
    textVisible: Boolean,
    onClick: () -> Unit = {}
) {
    IconButton(onClick = onClick) {
        val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        val colorFilter = ColorFilter.tint(color = color.copy(0.8f))
        Crossfade(targetState = textVisible) { visible ->
            if (visible) {
                Image(
                    painter = painterResource(id = R.drawable.ic_text),
                    contentDescription = "",
                    colorFilter = colorFilter
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_format_clear),
                    contentDescription = "",
                    colorFilter = colorFilter
                )
            }
        }
    }
}