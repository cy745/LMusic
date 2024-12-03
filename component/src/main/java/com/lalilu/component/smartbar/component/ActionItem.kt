package com.lalilu.component.smartbar.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.RemixIcon
import com.lalilu.component.LongClickableTextButton
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.deleteBinFill
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
            if (action.longClick()) {
                LongClickActionItemContent(
                    modifier = modifier,
                    color = action.color(),
                    title = action.title(),
                    subTitle = action.subTitle(),
                    icon = action.icon(),
                    dotColor = action.dotColor(),
                    onAction = action.onAction
                )
            } else {
                ActionItemContent(
                    modifier = modifier,
                    color = action.color(),
                    title = action.title(),
                    subTitle = action.subTitle(),
                    icon = action.icon(),
                    dotColor = action.dotColor(),
                    onAction = action.onAction
                )
            }
        }
    }
}

@Composable
fun LongClickActionItemContent(
    modifier: Modifier = Modifier,
    color: Color,
    title: String,
    subTitle: String? = null,
    icon: ImageVector? = null,
    dotColor: Color? = null,
    fullyExpended: Boolean = false,
    onAction: () -> Unit = {}
) {
    val tipsShow = remember { mutableLongStateOf(0L) }

    LaunchedEffect(tipsShow.longValue) {
        delay(3000)

        if (!isActive) return@LaunchedEffect
        tipsShow.longValue = 0L
    }

    LongClickableTextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        horizontalArrangement = Arrangement.Start,
        contentPadding = PaddingValues(0.dp),
        onClick = { tipsShow.longValue = System.currentTimeMillis() },
        onLongClick = {
            tipsShow.longValue = 0
            onAction()
        }
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
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )

                    if (fullyExpended && subTitle != null) {
                        AnimatedContent(
                            targetState = tipsShow.longValue > 0,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = ""
                        ) { show ->
                            if (show) {
                                Text(
                                    modifier = Modifier.alpha(0.6f),
                                    text = "长按以执行",
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            } else {
                                Text(
                                    text = subTitle,
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp,
                                    color = color.copy(0.5f),
                                )
                            }
                        }
                    } else {
                        AnimatedVisibility(visible = tipsShow.longValue > 0) {
                            Text(
                                modifier = Modifier.alpha(0.6f),
                                text = subTitle ?: "长按以执行",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActionItemContent(
    modifier: Modifier = Modifier,
    color: Color,
    title: String,
    subTitle: String? = null,
    icon: ImageVector? = null,
    dotColor: Color? = null,
    fullyExpended: Boolean = false,
    onAction: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable(onClick = onAction),
        color = color.copy(0.2f),
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

                    if (fullyExpended && subTitle != null) {
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

@Preview(showBackground = true)
@Composable
private fun ActionItemContentPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActionItemContent(
            modifier = Modifier
                .height(78.dp),
            title = "删除歌单",
            icon = RemixIcon.System.deleteBinFill,
            color = Color.Red,
            fullyExpended = false
        )

        ActionItemContent(
            modifier = Modifier
                .height(78.dp)
                .fillMaxWidth(),
            title = "删除歌单",
            icon = RemixIcon.System.deleteBinFill,
            color = Color.Red,
            fullyExpended = false
        )
    }
}