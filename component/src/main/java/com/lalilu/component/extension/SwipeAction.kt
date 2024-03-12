package com.lalilu.component.extension

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed class SwipeAction {
    data class BySwipe(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
        val onAction: () -> Unit
    ) : SwipeAction()
}

@Composable
fun SwipeActionRow(
    swipeThreshold: Dp = 100.dp,
    maxSwipeThreshold: Dp = swipeThreshold * 2f,
    actionAtLeft: SwipeAction.BySwipe? = null,
    actionAtRight: SwipeAction.BySwipe? = null,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()
    val offset = remember { mutableFloatStateOf(0f) }
    val visibleAtLeft = remember { derivedStateOf { offset.floatValue > 0f } }

    val swipeThresholdPx = remember { with(density) { swipeThreshold.toPx() } }
    val maxSwipeDistance = remember { with(density) { maxSwipeThreshold.toPx() } }
    val arrivedThreshold = remember { derivedStateOf { abs(offset.floatValue) > swipeThresholdPx } }

    val draggableState = rememberDraggableState { dx ->
        var result = dx

        val percent = 1f - (abs(offset.floatValue) - 0f) / maxSwipeDistance * 0.5f
        if (percent in 0F..1F) result = dx * percent

        val target = offset.floatValue + result

        if ((actionAtLeft != null && target >= 0f) || (actionAtRight != null && target <= 0f)) {
            offset.floatValue = target
        }
    }

    val visibleActions = remember {
        derivedStateOf {
            when {
                offset.floatValue > 0f -> actionAtLeft
                offset.floatValue < 0f -> actionAtRight
                else -> null
            }
        }
    }

    if (arrivedThreshold.value) {
        LaunchedEffect(Unit) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .draggable(
                state = draggableState,
                interactionSource = interactionSource,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    coroutineScope.launch {
                        launch {
                            if (arrivedThreshold.value && visibleActions.value != null) {
                                visibleActions.value?.onAction?.invoke()
                            }
                        }

                        launch {
                            draggableState.drag(MutatePriority.PreventUserInput) {
                                Animatable(offset.floatValue)
                                    .animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 200),
                                        block = { dragBy(value - offset.floatValue) }
                                    )
                            }
                        }
                    }
                }
            )
            .offset { IntOffset.Zero.copy(x = offset.floatValue.toInt()) }
    ) {
        content()

        if (visibleActions.value != null) {
            val bgColor = animateColorAsState(
                targetValue = if (arrivedThreshold.value) Color(0xFF1B7E00) else Color(0xFFB35004),
                label = ""
            )

            val alignment = if (visibleAtLeft.value) Alignment.End else Alignment.Start

            Row(
                modifier = Modifier
                    .matchParentSize()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)

                        val multiply = if (visibleAtLeft.value) -1 else 1

                        layout(placeable.width, placeable.height) {
                            placeable.place(x = placeable.width * multiply, y = 0)
                        }
                    }
                    .background(color = bgColor.value.copy(0.15f))
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment)
            ) {
                Text(
                    color = bgColor.value,
                    text = stringResource(id = visibleActions.value!!.titleRes),
                    fontSize = 14.sp
                )

                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = visibleActions.value!!.iconRes),
                    contentDescription = stringResource(id = visibleActions.value!!.titleRes),
                    colorFilter = ColorFilter.tint(color = bgColor.value)
                )
            }
        }
    }
}
