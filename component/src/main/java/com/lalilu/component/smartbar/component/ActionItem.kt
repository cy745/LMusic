package com.lalilu.component.smartbar.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import kotlin.random.Random
import kotlin.random.nextInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ActionItem(
    modifier: Modifier = Modifier,
    actionContext: ActionContext,
    action: ScreenAction
) {
    when (action) {
        is ScreenAction.Dynamic -> {
            action.content(actionContext)
        }

        is ScreenAction.Static -> {
            val color = action.color()
            val title = action.title()
            val subTitle = action.subTitle()
            val icon = action.icon()
            val dotColor = action.dotColor()

            Surface(
                modifier = modifier,
                color = color.copy(0.2f),
                onClick = { action.onAction() }
            ) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        icon?.let {
                            Image(
                                modifier = Modifier.size(20.dp),
                                imageVector = icon,
                                contentDescription = title,
                                colorFilter = ColorFilter.tint(color = color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Column(
                            modifier = Modifier,
                            verticalArrangement = Arrangement
                                .spacedBy(2.dp, Alignment.CenterVertically)
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                color = color,
                                fontWeight = FontWeight.Medium
                            )

                            if (actionContext.isFullyExpanded && subTitle != null) {
                                Text(
                                    text = subTitle,
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp,
                                    color = color.copy(0.5f),
                                )
                            }
                        }
                    }

                    if (dotColor != null) {
                        val animation = rememberInfiniteTransition(label = "")
                        val scaleValue = animation.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1000),
                                repeatMode = RepeatMode.Reverse,
                                initialStartOffset = StartOffset(
                                    offsetMillis = remember { Random.nextInt(0..1000) }
                                )
                            ),
                            label = ""
                        )

                        Spacer(
                            modifier = Modifier
                                .graphicsLayer { alpha = scaleValue.value }
                                .padding(8.dp)
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(color = dotColor)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }
}