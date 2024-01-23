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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lplayer.LPlayer


@Composable
fun PlayingToolbar(
    modifier: Modifier = Modifier,
    isUserTouchEnable: () -> Boolean = { false },
    isItemPlaying: (mediaId: String) -> Boolean = { false },
    isExtraVisible: () -> Boolean = { true },
    onClick: () -> Unit = {},
    extraContent: @Composable AnimatedVisibilityScope.() -> Unit = {}
) {
    val song by LPlayer.runtime.info.playingFlow.collectAsState(null)
    val defaultSloganStr = stringResource(id = R.string.default_slogan)

    val enter = remember {
        fadeIn(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + expandHorizontally(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
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
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + slideOutHorizontally(
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { it / 2 }
    }

    Row(
        modifier = modifier
            .clickable(
                onClick = { if (isUserTouchEnable()) onClick() },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 25.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlayingHeader(
            modifier = Modifier.weight(1f),
            title = { song?.title?.takeIf(String::isNotBlank) ?: defaultSloganStr },
            subTitle = { song?.subTitle ?: defaultSloganStr },
            isPlaying = { song?.let { isItemPlaying(it.mediaId) } ?: false }
        )

        AnimatedVisibility(
            visible = isExtraVisible(),
            enter = enter,
            exit = exit,
            content = extraContent
        )
    }
}