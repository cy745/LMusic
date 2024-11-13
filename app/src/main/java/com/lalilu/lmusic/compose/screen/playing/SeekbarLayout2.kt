package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.lmusic.utils.extension.durationToTime
import kotlinx.coroutines.launch

sealed class SeekbarVerticalState {
    data object ProgressBar : SeekbarVerticalState()
    data object Cancel : SeekbarVerticalState()
    data object Dispatcher : SeekbarVerticalState()
}

sealed class SeekbarHorizontalState {
    data object Idle : SeekbarHorizontalState()
    data object Follow : SeekbarHorizontalState()
}

sealed interface ClickPart {
    data object Start : ClickPart
    data object Middle : ClickPart
    data object End : ClickPart
}

@Preview
@Composable
fun SeekbarLayout2(
    modifier: Modifier = Modifier,
    minValue: () -> Float = { 0f },
    maxValue: () -> Float = { 0f },
    dataValue: () -> Float = { 0f },
    animateColor: () -> Color = { Color.DarkGray },
    onDragStart: suspend (Offset) -> Unit = {},
    onDragStop: suspend (Int) -> Unit = {},
    onDispatchDragOffset: (Float) -> Unit = {},
    onValueChange: (Float) -> Unit = {},
    onSeekTo: (Float) -> Unit = {},
    onClick: (ClickPart) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val scrollSensitivity = remember { 1.3f }
    val scrollThreadHold = remember { 200f }
    val seekbarPaddingBottom = remember { density.run { 156.dp.toPx() } }

    val currentValue = remember { mutableFloatStateOf(0f) }
    val seekbarOffsetY = remember { mutableFloatStateOf(0f) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val seekbarVerticalState =
        remember { mutableStateOf<SeekbarVerticalState>(SeekbarVerticalState.ProgressBar) }
    val seekbarHorizontalState =
        remember { mutableStateOf<SeekbarHorizontalState>(SeekbarHorizontalState.Idle) }

    val moved = remember { mutableStateOf(false) }
    val isTouching = remember { mutableStateOf(false) }
    val isCanceled = remember {
        derivedStateOf { seekbarVerticalState.value != SeekbarVerticalState.ProgressBar }
    }

    val resultValue = remember {
        derivedStateOf {
            val value = if (isTouching.value && !isCanceled.value) currentValue.floatValue
            else dataValue()

            value.coerceIn(minValue(), maxValue())
        }
    }

    // 使值的变化平滑
    val animateValue = animateFloatAsState(
        targetValue = resultValue.value,
        visibilityThreshold = 0.005f,
        animationSpec = if (isTouching.value && !isCanceled.value) snap() else spring(stiffness = Spring.StiffnessLow),
        label = ""
    )

    val draggableState = rememberDraggable2DState { offset ->
        val oldState = seekbarVerticalState.value
        val deltaY = offset.y
        val deltaX = offset.x

        // 直接记录Y轴上的滚动距离
        seekbarOffsetY.floatValue += deltaY

        // 根据当前状态控制进度变量
        currentValue.floatValue = if (oldState == SeekbarVerticalState.ProgressBar) {
            (currentValue.floatValue + deltaX / boxSize.width * (maxValue() - minValue()) * scrollSensitivity)
                .coerceIn(minValue(), maxValue())
        } else {
            animateValue.value
        }

        // 根据Y轴滚动距离决定新的状态
        seekbarVerticalState.value = when {
            seekbarOffsetY.floatValue < -200f -> SeekbarVerticalState.Dispatcher
            seekbarOffsetY.floatValue < -100f -> SeekbarVerticalState.Cancel
            else -> SeekbarVerticalState.ProgressBar
        }

        // 当状态发生变化的时候，进行震动
        if (oldState != seekbarVerticalState.value) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        when (oldState) {
            seekbarVerticalState.value -> {}
            SeekbarVerticalState.Dispatcher -> scope.launch { onDragStop(-1) }

            SeekbarVerticalState.Cancel -> when (seekbarVerticalState.value) {
                SeekbarVerticalState.Dispatcher -> {
                    val animationState = AnimationState(
                        initialValue = 0f,
                        initialVelocity = 100f,
                    )
                    scope.launch {
                        var lastValue = 0f
                        animationState.animateTo(scrollThreadHold + seekbarPaddingBottom) {
                            val dt = value - lastValue
                            lastValue = value
                            onDispatchDragOffset(-dt)
                        }
                    }
                }

                else -> {}
            }

            else -> {}
        }

        // 若当前状态为Dispatcher，则将滚动的位移量向外分发
        if (seekbarVerticalState.value == SeekbarVerticalState.Dispatcher) {
            onDispatchDragOffset(deltaY)
        }
    }

    Box(
        modifier = modifier
            .padding(bottom = 100.dp)
            .fillMaxWidth(0.7f)
            .wrapContentHeight()
            .onPlaced { boxSize = it.size }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)

                        when (event.type) {
                            PointerEventType.Press -> {
                                // 开始触摸时，将当前可见的进度值记录下来
                                currentValue.floatValue = animateValue.value
                                isTouching.value = true
                                moved.value = false
                            }

                            PointerEventType.Release -> {
                                if (moved.value && !isCanceled.value) {
                                    onSeekTo(currentValue.floatValue)
                                }
                                isTouching.value = false
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    val clickPart = when (position.x) {
                        in 0f..(boxSize.width / 3f) -> ClickPart.Start
                        in (boxSize.width * 2 / 3f)..boxSize.width.toFloat() -> ClickPart.End
                        else -> ClickPart.Middle
                    }
                    onClick(clickPart)
                }
            }
            .combineDetectDrag(
                onLongClickStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDragStart = {
                    moved.value = true
                    seekbarVerticalState.value = SeekbarVerticalState.ProgressBar
                    seekbarHorizontalState.value = SeekbarHorizontalState.Follow

                    seekbarOffsetY.floatValue = it.y
                    scope.launch { onDragStart(it) }
                },
                onDragEnd = {
                    seekbarVerticalState.value = SeekbarVerticalState.ProgressBar
                    seekbarHorizontalState.value = SeekbarHorizontalState.Idle

                    scope.launch { onDragStop(0) }
                },
                onDrag = { change, dragAmount ->
                    draggableState.dispatchRawDelta(dragAmount)
                }
            )
    ) {
        val textMeasurer = rememberTextMeasurer()
        val bgColor = MaterialTheme.colors.background
        val alpha = animateFloatAsState(
            targetValue = if (isTouching.value) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = ""
        )
        val textStyle = remember {
            TextStyle.Default.copy(
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                baselineShift = BaselineShift.None
            )
        }

        Box(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .drawWithCache {
                    val innerPath = Path()
                    val currentValueText = animateValue.value
                        .toLong()
                        .durationToTime()
                    val maxValueText = maxValue()
                        .toLong()
                        .durationToTime()
                    val currentValueTextResult = textMeasurer.measure(
                        text = currentValueText,
                        style = textStyle
                    )
                    val maxValueTextResult = textMeasurer.measure(
                        text = maxValueText,
                        style = textStyle
                    )

                    val maxPadding = 4.dp.toPx()
                    val paddingAnimate = maxPadding * alpha.value

                    val innerRadius = 16.dp.toPx() - paddingAnimate
                    val innerHeight = size.height - (paddingAnimate * 2f)
                    val innerWidth = size.width - (paddingAnimate * 2f)

                    val actualValue = animateValue.value
                    val actualProgress = actualValue.normalize(minValue(), maxValue())

                    val thumbWidth = innerWidth * actualProgress
                    val thumbHeight = innerHeight

                    innerPath.reset()
                    innerPath.addRoundRect(
                        RoundRect(
                            rect = Rect(
                                offset = Offset(x = paddingAnimate, y = paddingAnimate),
                                size = Size(width = innerWidth, height = innerHeight)
                            ),
                            cornerRadius = CornerRadius(innerRadius, innerRadius)
                        )
                    )

                    onDrawBehind {
                        // 纯色背景
                        drawRect(color = bgColor, alpha = alpha.value)

                        // 圆角裁切
                        clipPath(innerPath) {
                            drawRect(color = Color(100, 100, 100, 50))
                            onValueChange(actualValue)

                            // 绘制总时长文本（固定右侧）
                            drawText(
                                textLayoutResult = maxValueTextResult,
                                topLeft = Offset(
                                    x = size.width - maxValueTextResult.size.width - 16.dp.toPx(),
                                    y = (size.height - maxValueTextResult.size.height) / 2f
                                ),
                                color = Color.White,
                            )

                            // 绘制滑块
                            drawRoundRect(
                                color = animateColor(),
                                cornerRadius = CornerRadius(innerRadius, innerRadius),
                                topLeft = Offset(x = paddingAnimate, y = paddingAnimate),
                                size = Size(width = thumbWidth, height = thumbHeight)
                            )

                            // 绘制实时进度文本（移动）
                            drawText(
                                textLayoutResult = currentValueTextResult,
                                topLeft = Offset(
                                    x = (thumbWidth - 16.dp.toPx() - currentValueTextResult.size.width)
                                        .coerceAtLeast(16.dp.toPx()),
                                    y = (size.height - currentValueTextResult.size.height) / 2f
                                ),
                                color = Color.White,
                            )

//                            // 绘制把手元素
//                            drawRoundRect(
//                                color = Color.White,
//                                alpha = alpha.value,
//                                cornerRadius = CornerRadius(50f),
//                                topLeft = Offset(
//                                    x = innerWidth * actualProgress + paddingAnimate - 8.dp.toPx(),
//                                    y = (size.height - (innerHeight * 0.5f)) / 2f
//                                ),
//                                size = Size(
//                                    width = 4.dp.toPx(),
//                                    height = innerHeight * 0.5f
//                                )
//                            )
                        }
                    }
                }
        ) {
//            Row {
//                Icon(
//                    imageVector = RemixIcon.Media.repeatFill,
//                    contentDescription = null
//                )
//
//                Icon(
//                    imageVector = RemixIcon.Media.repeatFill,
//                    contentDescription = null
//                )
//
//                Icon(
//                    imageVector = RemixIcon.Media.repeatFill,
//                    contentDescription = null
//                )
//            }
        }
    }
}

@Composable
fun Modifier.combineDetectDrag(
    key: Any = Unit,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onLongClickStart: () -> Unit = {}
) = this.then(
    Modifier
        .pointerInput(key) {
            detectDragGestures(onDragStart, onDragEnd, onDragEnd, onDrag)
        }
        .pointerInput(key) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    onLongClickStart()
                    onDragStart(it)
                },
                onDragEnd = onDragEnd,
                onDragCancel = onDragEnd,
                onDrag = onDrag
            )
        }
)


private fun Float.normalize(minValue: Float, maxValue: Float): Float {
    val min = minOf(minValue, maxValue)
    val max = maxOf(minValue, maxValue)

    if (min == max) return 0f
    if (this <= min) return 0f
    if (this >= max) return 1f

    return ((this - min) / (max - min))
        .coerceIn(0f, 1f)
}