package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.lalilu.common.AccumulatedValue
import com.lalilu.lmusic.utils.extension.durationToTime
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

sealed class SeekbarState {
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

    val bgAlpha = animateFloatAsState(
        targetValue = if (isTouching && !isCanceled) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = ""
    )
    val textAlpha = animateFloatAsState(
        targetValue = if (isSwitching) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
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
                switchModeX.floatValue = offset.x
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
                                if (isMoved && !isCanceled) {
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
                    switchMode.value = false
                    seekbarState.value = SeekbarState.Idle

                    switchModeX.floatValue = 0f
                    seekbarOffsetY.floatValue = 0f
                    scope.launch { onDragStop(0) }
                },
                onDrag = { _, dragAmount ->
                    draggableState.dispatchRawDelta(dragAmount)
                }
            )
    ) {
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

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    translationY = -yTranslationAnimateValue.value * (scrollThreadHold / 2f)
                    scaleX = 1f - (yTranslationAnimateValue.value * 0.1f)
                    scaleY = scaleX
                }
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
            val paddingAnimate = maxPadding * bgAlpha.value

            val innerRadius = 16.dp.toPx() - paddingAnimate
            val innerHeight = size.height - (paddingAnimate * 2f)
            val innerWidth = size.width - (paddingAnimate * 2f)

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

            val actualValue = animateValue.value
            val actualProgress = actualValue.normalize(minValue(), maxValue())
            onValueChange(actualValue)

//            // 通过Value计算Progress，从而获取滑块应有的宽度
//            thumbWidth = normalize(nowValue, minValue, maxValue) * actualWidth
//            thumbWidth = lerp(thumbWidth, actualWidth / thumbCount, switchModeProgress)

            val thumbWidth = innerWidth * actualProgress
            val thumbHeight = innerHeight

            // 纯色背景
            drawRect(color = bgColor, alpha = bgAlpha.value)

            // 圆角裁切
            clipPath(innerPath) {
                drawRect(color = Color(100, 100, 100, 50))

                // 绘制总时长文本（固定右侧）
                drawText(
                    textLayoutResult = maxTextResult,
                    color = Color.White,
                    alpha = textAlpha.value,
                    topLeft = Offset(
                        x = size.width - textSize.value.first - 16.dp.toPx(),
                        y = (size.height - textSize.value.second) / 2f
                    )
                )

                // 绘制滑块
                drawRoundRect(
                    color = animateColor(),
                    cornerRadius = CornerRadius(innerRadius, innerRadius),
                    topLeft = Offset(x = paddingAnimate, y = paddingAnimate),
                    size = Size(width = thumbWidth, height = thumbHeight)
                )

                val textX =
                    (paddingAnimate + thumbWidth - 16.dp.toPx() - textSize.value.first)
                        .let { accumulator.accumulate(it) }
                        .coerceAtLeast(16.dp.roundToPx())

                // 绘制实时进度文本（移动）
                drawText(
                    textLayoutResult = currentTextResult,
                    color = Color.White,
                    alpha = textAlpha.value,
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

//        Box(
//            modifier = Modifier
//                .fillMaxHeight()
//                .fillMaxWidth()
//        ) {
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
//        }
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


class SeekbarProgressKeeper(
    private val minValue: () -> Float,
    private val maxValue: () -> Float,
    private val sizeWidth: () -> Float,
    private val scrollSensitivity: Float,
) {
    var nowValue: Float by mutableFloatStateOf(0f)
        private set

    fun updateValue(value: Float) {
        nowValue = value.coerceIn(minValue(), maxValue())
    }

    fun updateValueByDelta(delta: Float) {
        val value = nowValue + delta / sizeWidth() * (maxValue() - minValue()) * scrollSensitivity
        updateValue(value)
    }
}

@Composable
fun rememberSeekbarProgressKeeper(
    minValue: () -> Float,
    maxValue: () -> Float,
    sizeWidth: () -> Float,
    scrollSensitivity: Float = 1f
): SeekbarProgressKeeper {
    return remember {
        SeekbarProgressKeeper(
            minValue = minValue,
            maxValue = maxValue,
            sizeWidth = sizeWidth,
            scrollSensitivity = scrollSensitivity
        )
    }
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