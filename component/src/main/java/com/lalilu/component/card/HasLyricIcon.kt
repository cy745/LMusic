package com.lalilu.component.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lalilu.component.R
import com.lalilu.component.extension.dayNightTextColorFilter

@Composable
fun HasLyricIcon(
    hasLyric: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false }
) {
    if (fixedHeight()) {
        AnimatedVisibility(
            visible = hasLyric(),
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            label = "HasLyricIconVisibility"
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_lrc_fill),
                contentDescription = "Lyric Icon",
                colorFilter = dayNightTextColorFilter(0.9f),
                modifier = Modifier
                    .size(20.dp)
                    .aspectRatio(1f)
            )
        }
    } else {
        val alpha by animateFloatAsState(
            targetValue = if (hasLyric()) 1f else 0f,
            label = "HasLyricIconAlpha"
        )
        Image(
            painter = painterResource(id = R.drawable.ic_lrc_fill),
            contentDescription = "Lyric Icon",
            colorFilter = dayNightTextColorFilter(0.9f),
            modifier = Modifier
                .size(20.dp)
                .aspectRatio(1f)
                .alpha(alpha)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HasLyricIconPreview() {
    Box(modifier = Modifier.padding(20.dp)) {
        HasLyricIcon(hasLyric = { true })
    }
}