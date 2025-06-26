package com.lalilu.lmusic.compose.screen.playing.lyric.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContext
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings

@Composable
fun LyricStartTips(
    index: Int,
    item: LyricItem.StartTips,
    modifier: Modifier = Modifier,
    settings: LyricSettings,
    context: LyricContext,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val currentTime = context.currentTime()

    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = currentTime in item.focusTime..item.endTime,
        enter = fadeIn() + expandVertically(
            clip = false,
            expandFrom = Alignment.CenterVertically
        ),
        exit = fadeOut() + shrinkVertically(
            clip = false,
            shrinkTowards = Alignment.CenterVertically
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(settings.containerPadding)
                .padding(top = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = 8.dp,
                    alignment = when (settings.textAlign) {
                        TextAlign.End -> Alignment.End
                        TextAlign.Center -> Alignment.CenterHorizontally
                        else -> Alignment.Start
                    }
                )
            ) {
                val animate1 = animateFloatAsState(
                    targetValue = if (currentTime >= item.startTime) 0.9f else 0.5f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
                val animate2 = animateFloatAsState(
                    targetValue = if (currentTime >= (item.startTime + 1000L)) 0.9f else 0.5f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
                val animate3 = animateFloatAsState(
                    targetValue = if (currentTime >= (item.endTime - 1000L)) 0.9f else 0.5f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
                Spacer(
                    modifier = Modifier
                        .size(36.dp * animate1.value)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(
                                center = center,
                                color = Color.White,
                                radius = size.width / 2f,
                                alpha = animate1.value
                            )
                        }
                )
                Spacer(
                    modifier = Modifier
                        .size(36.dp * animate2.value)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(
                                color = Color.White,
                                radius = size.width / 2f,
                                alpha = animate2.value
                            )
                        }
                )
                Spacer(
                    modifier = Modifier
                        .size(36.dp * animate3.value)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(
                                color = Color.White,
                                radius = size.width / 2f,
                                alpha = animate3.value
                            )
                        }
                )
            }
        }
    }
}