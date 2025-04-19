package com.lalilu.lmusic.compose.component.playing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.component.extension.enableFor
import com.lalilu.lplayer.MPlayer


@Composable
fun PlayingToolbar(
    modifier: Modifier = Modifier,
    isUserTouchEnable: () -> Boolean = { false },
    isExtraVisible: () -> Boolean = { true },
    contentColor: () -> Color,
    onClick: () -> Unit = {},
    fixContent: @Composable RowScope.() -> Unit = {},
    extraContent: @Composable AnimatedVisibilityScope.() -> Unit = {}
) {
    val metadata = MPlayer.currentMediaMetadata
    val defaultSloganStr = stringResource(id = R.string.default_slogan)

    val enter = remember {
        fadeIn(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + expandHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            clip = false
        ) + slideInHorizontally(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { it / 2 }
    }
    val exit = remember {
        fadeOut(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + shrinkHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            clip = false
        ) + slideOutHorizontally(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { it / 2 }
    }

    Row(
        modifier = modifier
            .enableFor(isUserTouchEnable) {
                clickable(
                    onClick = { if (isUserTouchEnable()) onClick() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
            }
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 25.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayingHeader(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            title = { metadata?.title?.toString()?.takeIf(String::isNotBlank) ?: defaultSloganStr },
            subTitle = {
                metadata?.subtitle?.toString()?.takeIf(String::isNotBlank) ?: defaultSloganStr
            },
            contentColor = contentColor,
            isPlaying = { MPlayer.isPlaying }
        )

        fixContent()

        AnimatedVisibility(
            visible = isExtraVisible(),
            enter = enter,
            exit = exit,
            content = extraContent
        )
    }
}