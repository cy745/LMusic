package com.lalilu.lmusic.compose.component.playing

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lalilu.component.card.PlayingTipIcon
import com.lalilu.lmusic.utils.extension.slideTransition

@Composable
fun PlayingHeader(
    modifier: Modifier = Modifier,
    title: () -> String,
    subTitle: () -> String,
    isPlaying: () -> Boolean,
) {
    val density = LocalDensity.current
    val slideMovement = remember { density.run { 50.dp.toPx().toInt() } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = title(),
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
            transitionSpec = {
                slideTransition(
                    duration = 400,
                    movement = slideMovement
                )
            },
            label = "TitleTextAnimation"
        ) { text ->
            Text(
                modifier = Modifier
                    .wrapContentWidth(Alignment.Start)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(30.dp)
                    ),
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                style = MaterialTheme.typography.subtitle1
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayingTipIcon(isPlaying = isPlaying)
            AnimatedContent(
                targetState = subTitle(),
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
                transitionSpec = {
                    slideTransition(
                        duration = 450,
                        movement = slideMovement
                    )
                },
                label = "SubTitleTextAnimation"
            ) { text ->
                Text(
                    modifier = Modifier
                        .wrapContentWidth(Alignment.Start)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            spacing = MarqueeSpacing(30.dp)
                        ),
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.caption,
                )
            }
        }
    }
}