package com.lalilu.lmusic.compose.screen.playing.seekbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.lalilu.RemixIcon
import com.lalilu.common.AccumulatedValue
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.orderPlayFill
import com.lalilu.remixicon.media.repeatOneFill
import com.lalilu.remixicon.media.shuffleFill
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private sealed class SeekbarState {
    data object Idle : SeekbarState()
    data object ProgressBar : SeekbarState()
    data object Switcher : SeekbarState()
    data object Cancel : SeekbarState()
    data object Dispatcher : SeekbarState()
}

sealed interface ClickPart {
    data object Start : ClickPart
    data object Middle : ClickPart
    data object End : ClickPart
}

@Preview
@Composable
fun SeekbarLayout(
    modifier: Modifier = Modifier,
    minValue: () -> Float = { 0f },
    maxValue: () -> Float = { 0f },
    dataValue: () -> Float = { 0f },
    switchIndex: () -> Int = { 0 },
    animateColor: () -> Color = { Color.DarkGray },
    onDragStart: suspend (Offset) -> Unit = {},
    onDragStop: suspend (Int) -> Unit = {},
    onDispatchDragOffset: (Float) -> Unit = {},
    onValueChange: (Float) -> Unit = {},
    onSeekTo: (Float) -> Unit = {},
    onSwitchTo: (Int) -> Unit = {},
    onClick: (ClickPart) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val bgColor = MaterialTheme.colors.background
    val accumulator = remember { AccumulatedValue() }

    val scrollSensitivity = remember { 1.3f }
    val scrollThreadHold = remember { 200f }
    val seekbarPaddingBottom = remember { density.run { 156.dp.toPx() } }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val progressKeeper = rememberSeekbarProgressKeeper(
        minValue = minValue,
        maxValue = maxValue,
        sizeWidth = { boxSize.width.toFloat() },
        scrollSensitivity = scrollSensitivity
    )

    val seekbarOffsetY = remember { mutableFloatStateOf(0f) }
    val switchMode = remember { mutableStateOf(false) }
    val switchModeX = remember { mutableFloatStateOf(0f) }
    val seekbarState = remember { mutableStateOf<SeekbarState>(SeekbarState.Idle) }

    var isMoved by remember { mutableStateOf(false) }
    var isTouching by remember { mutableStateOf(false) }
    val isSwitching by remember { derivedStateOf { seekbarState.value is SeekbarState.Switcher } }
    val isCanceled by remember {
        derivedStateOf { seekbarState.value is SeekbarState.Cancel || seekbarState.value is SeekbarState.Dispatcher }
    }

    val resultValue = remember {
        derivedStateOf {
            when {
                isSwitching -> {
                    progressKeeper.updateValue(dataValue())
                    false to dataValue()
                }

                isTouching && !isCanceled -> true to progressKeeper.nowValue
                else -> {
                    progressKeeper.updateValue(dataValue())
                    false to dataValue()
                }
            }
        }
    }

    // 使值的变化平滑
    val animateValue = animateFloatAsState(
        targetValue = resultValue.value.second,
        animationSpec = if (resultValue.value.first) snap() else spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.005f,
        label = ""
    )
    val touchingProgress = animateFloatAsState(
        targetValue = if (isTouching && !isCanceled) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = ""
    )
    val switchingProgress = animateFloatAsState(
        targetValue = if (isSwitching) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = ""
    )
    val yProgressValue = remember {
        derivedStateOf {
            val value = seekbarOffsetY.floatValue.coerceAtMost(0f)
                .absoluteValue
                .takeIf { it < (scrollThreadHold / 2f) }
                ?: 0f

            (value / (scrollThreadHold / 2f)).coerceIn(0f, 1f)
        }
    }
    val yTranslationAnimateValue = animateFloatAsState(
        targetValue = yProgressValue.value,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = ""
    )
    val textStyle = remember {
        TextStyle.Default.copy(
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            baselineShift = BaselineShift.None,
            textAlign = TextAlign.End,
            color = Color.White
        )
    }
    val textSize = remember {
        derivedStateOf {
            // 获取最大时长，并使用0替换其内部的数字，计算其最大宽度
            val text = maxValue().toLong()
                .durationToTime()
                .replace(Regex("[0-9]"), "0")
            val result = textMeasurer.measure(
                text = text,
                style = textStyle
            )
            result.size.width to result.size.height
        }
    }

    val draggableState = rememberDraggable2DState { offset ->
        val oldState = seekbarState.value

        val deltaY = offset.y
        val deltaX = offset.x

        // 直接记录Y轴上的滚动距离
        seekbarOffsetY.floatValue += deltaY

        // 根据当前状态控制进度变量
        when {
            isSwitching -> {
                switchModeX.floatValue += deltaX
            }

            oldState == SeekbarState.ProgressBar -> {
                progressKeeper.updateValueByDelta(delta = deltaX)
            }
        }

        // 根据Y轴滚动距离决定新的状态
        seekbarState.value = when {
            seekbarOffsetY.floatValue < -scrollThreadHold -> SeekbarState.Dispatcher
            seekbarOffsetY.floatValue < -(scrollThreadHold / 2f) -> SeekbarState.Cancel
            else -> if (switchMode.value) SeekbarState.Switcher else SeekbarState.ProgressBar
        }

        // 当状态发生变化的时候，进行震动
        if (oldState != seekbarState.value) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        when (oldState) {
            seekbarState.value -> {}
            SeekbarState.Dispatcher -> scope.launch { onDragStop(-1) }

            SeekbarState.Cancel -> when (seekbarState.value) {
                SeekbarState.Dispatcher -> {
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
        if (seekbarState.value is SeekbarState.Dispatcher) {
            onDispatchDragOffset(deltaY)
        }
    }

    Box(
        modifier = modifier
            .padding(bottom = 100.dp)
            .fillMaxWidth(0.7f)
            .height(IntrinsicSize.Max)
            .onPlaced { boxSize = it.size }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)

                        when (event.type) {
                            PointerEventType.Press -> {
                                // 开始触摸时，将当前可见的进度值记录下来
                                progressKeeper.updateValue(animateValue.value)
                                isTouching = true
                                isMoved = false
                            }

                            PointerEventType.Release -> {
                                if (isMoved && !isCanceled && !isSwitching) {
                                    onSeekTo(progressKeeper.nowValue)
                                }
                                isTouching = false
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
                    switchModeX.floatValue = it.x
                    switchMode.value = true
                    seekbarState.value = SeekbarState.Switcher
                },
                onDragStart = {
                    isMoved = true
                    seekbarState.value = if (switchMode.value) SeekbarState.Switcher
                    else SeekbarState.ProgressBar

                    seekbarOffsetY.floatValue = it.y
                    scope.launch { onDragStart(it) }
                },
                onDragEnd = {
                    if (isSwitching) {
                        val actualWidth = boxSize.width - density.run { 4.dp.roundToPx() }
                        val singleWidth = actualWidth / 3f

                        when (switchModeX.floatValue) {
                            in 0f..singleWidth -> onSwitchTo(0)
                            in singleWidth..(singleWidth * 2) -> onSwitchTo(1)
                            else -> onSwitchTo(2)
                        }
                    }

                    switchMode.value = false
                    seekbarState.value = SeekbarState.Idle

                    seekbarOffsetY.floatValue = 0f
                    scope.launch { onDragStop(0) }
                },
                onDrag = { _, dragAmount ->
                    draggableState.dispatchRawDelta(dragAmount)
                }
            )
            .graphicsLayer {
                translationY = -yTranslationAnimateValue.value * (scrollThreadHold / 2f)
                scaleX = 1f - (yTranslationAnimateValue.value * 0.1f)
                scaleY = scaleX
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            val innerPath = Path()
            val currentValueText = animateValue.value
                .toLong()
                .durationToTime()
            val maxValueText = maxValue()
                .toLong()
                .durationToTime()

            val currentTextResult = textMeasurer
                .measure(text = currentValueText, style = textStyle)
            val maxTextResult = textMeasurer
                .measure(text = maxValueText, style = textStyle)

            val maxPadding = 4.dp.toPx()
            val paddingValue = maxPadding * touchingProgress.value

            val innerRadius = 16.dp.toPx() - paddingValue
            val innerHeight = size.height - (paddingValue * 2f)
            val innerWidth = size.width - (paddingValue * 2f)

            innerPath.reset()
            innerPath.addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(x = paddingValue, y = paddingValue),
                        size = Size(width = innerWidth, height = innerHeight)
                    ),
                    cornerRadius = CornerRadius(innerRadius, innerRadius)
                )
            )

            val actualValue = animateValue.value
            val actualProgress = actualValue.normalize(minValue(), maxValue())
            onValueChange(actualValue)

            val thumbWidth = lerp(
                start = innerWidth * actualProgress,    // 根据进度计算的宽度
                stop = innerWidth / 3f,                 // 进度条均分宽度
                fraction = switchingProgress.value      // 根据切换进度进行插值
            ).coerceIn(0f, innerWidth)

            val thumbLeft = lerp(
                start = paddingValue,
                stop = switchModeX.floatValue - (innerWidth / 3f) / 2f,
                fraction = switchingProgress.value
            ).coerceIn(
                paddingValue,
                paddingValue + innerWidth - (innerWidth / 3f)
            )    // 限制滑块位置，确保其始终处于可见范围内

            val thumbTop = paddingValue
            val thumbHeight = innerHeight

            val textX =
                (paddingValue + (innerWidth * actualProgress) - 16.dp.toPx() - textSize.value.first)
                    .let { accumulator.accumulate(it) }
                    .coerceAtLeast(16.dp.roundToPx())

            // 纯色背景
            drawRect(
                color = bgColor,
                alpha = touchingProgress.value
            )

            // 圆角裁切
            clipPath(innerPath) {
                drawRect(color = Color(100, 100, 100, 50))

                // 绘制总时长文本（固定右侧）
                drawText(
                    textLayoutResult = maxTextResult,
                    color = Color.White,
                    alpha = 1f - switchingProgress.value,
                    topLeft = Offset(
                        x = size.width - textSize.value.first - 16.dp.toPx(),
                        y = (size.height - textSize.value.second) / 2f
                    )
                )

                // 绘制滑块
                drawRoundRect(
                    color = animateColor(),
                    cornerRadius = CornerRadius(innerRadius, innerRadius),
                    topLeft = Offset(x = thumbLeft, y = thumbTop),
                    size = Size(width = thumbWidth, height = thumbHeight)
                )

                // 绘制实时进度文本（移动）
                drawText(
                    textLayoutResult = currentTextResult,
                    color = Color.White,
                    alpha = 1f - switchingProgress.value,
                    topLeft = Offset(
                        x = textX.toFloat(),
                        y = (size.height - textSize.value.second) / 2f
                    )
                )

                // 绘制把手元素
//                drawRoundRect(
//                    color = Color.White,
//                    alpha = alpha.value,
//                    cornerRadius = CornerRadius(50f),
//                    topLeft = Offset(
//                        x = innerWidth * actualProgress + paddingAnimate - 8.dp.toPx(),
//                        y = (size.height - (innerHeight * 0.5f)) / 2f
//                    ),
//                    size = Size(
//                        width = 4.dp.toPx(),
//                        height = innerHeight * 0.5f
//                    )
//                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = isSwitching,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Icon(
                    imageVector = RemixIcon.Media.orderPlayFill,
                    contentDescription = null,
                    tint = Color.White
                )
                Icon(
                    imageVector = RemixIcon.Media.repeatOneFill,
                    contentDescription = null,
                    tint = Color.White
                )
                Icon(
                    imageVector = RemixIcon.Media.shuffleFill,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

private fun Modifier.combineDetectDrag(
    key: Any = Unit,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onLongClickStart: (Offset) -> Unit = {}
): Modifier = this
    .pointerInput(key) {
        detectDragGestures(
            onDragStart = onDragStart,
            onDragEnd = onDragEnd,
            onDragCancel = onDragEnd,
            onDrag = onDrag
        )
    }
    .pointerInput(key) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                onLongClickStart(it)
                onDragStart(it)
            },
            onDragEnd = onDragEnd,
            onDragCancel = onDragEnd,
            onDrag = onDrag
        )
    }

private fun Float.normalize(minValue: Float, maxValue: Float): Float {
    val min = minOf(minValue, maxValue)
    val max = maxOf(minValue, maxValue)

    if (min == max) return 0f
    if (this <= min) return 0f
    if (this >= max) return 1f

    return ((this - min) / (max - min))
        .coerceIn(0f, 1f)
}