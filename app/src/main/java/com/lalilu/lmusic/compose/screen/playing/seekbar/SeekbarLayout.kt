package com.lalilu.lmusic.compose.screen.playing.seekbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.lalilu.RemixIcon
import com.lalilu.common.AccumulatedValue
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.orderPlayFill
import com.lalilu.remixicon.media.repeatOneFill
import com.lalilu.remixicon.media.shuffleFill
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private sealed interface SeekbarState {
    data object Idle : SeekbarState
    data object ProgressBar : SeekbarState
    data object Switcher : SeekbarState
    data object Cancel : SeekbarState
    data object Dispatcher : SeekbarState
}

sealed interface ClickPart {
    data object Start : ClickPart
    data object Middle : ClickPart
    data object End : ClickPart
}

private fun SeekbarState.isCanceled(): Boolean {
    return when (this) {
        is SeekbarState.Cancel, is SeekbarState.Dispatcher -> true
        else -> false
    }
}

@Preview
@Composable
fun SeekbarLayout(
    modifier: Modifier = Modifier,
    minValue: () -> Float = { 0f },
    maxValue: () -> Float = { 0f },
    dataValue: () -> Float = { 0f },
    switchIndex: () -> Int = { 0 },
    scrollThreadHold: Float = 200f,
    animation: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) },
    animateColor: () -> Color = { Color.DarkGray },
    onDragStart: suspend (Offset) -> Unit = {},
    onDragStop: suspend (Int) -> Unit = {},
    onDispatchDragOffset: (Float) -> Unit = {},
    onSeekTo: (Float) -> Unit = {},
    onSwitchTo: (Int) -> Unit = {},
    onClick: (ClickPart) -> Unit = {}
) {
    val textStyle = remember {
        TextStyle.Default.copy(
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            baselineShift = BaselineShift.None,
            textAlign = TextAlign.End,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val boxSize = constraints
        val haptic = LocalHapticFeedback.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val seekbarPaddingBottom = remember { 100.dp }
        val seekbarHeight = remember { 56.dp }

        val progressKeeper = rememberSeekbarProgressKeeper(
            minValue = minValue,
            maxValue = maxValue,
            sizeWidth = { boxSize.maxWidth.toFloat() },
        )

        val switchMode = remember { mutableStateOf(false) }
        val switchModeX = remember { mutableFloatStateOf(0f) }
        val seekbarOffsetY = remember { mutableFloatStateOf(0f) }
        val seekbarState = remember { mutableStateOf<SeekbarState>(SeekbarState.Idle) }

        var isMoved by remember { mutableStateOf(false) }
        var isTouching by remember { mutableStateOf(false) }
        val isSwitching by remember { derivedStateOf { seekbarState.value is SeekbarState.Switcher } }
        val isCanceled by remember { derivedStateOf { seekbarState.value.isCanceled() } }
        val snap = remember { derivedStateOf { !(isSwitching || !isTouching || isCanceled) } }

        val maxDurationText = remember(maxValue()) { maxValue().toLong().durationToTime() }
        val currentTimeText = durationToText(duration = { animation.value.toLong() })

        // 使值的变化平滑
        LaunchedEffect(Unit) {
            snapshotFlow { if (snap.value) progressKeeper.nowValue else dataValue() }
                .distinctUntilChanged()
                .onEach { value ->
                    if (snap.value) {
                        animation.snapTo(value)
                    } else {
                        progressKeeper.updateValue(value)
                        launch {
                            animation.animateTo(
                                targetValue = value,
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                        }
                    }
                }.launchIn(this)
        }

        val offsetY = remember {
            derivedStateOf {
                val offsetY = seekbarOffsetY.floatValue
                    .coerceAtMost(0f)
                    .absoluteValue
                    .takeIf { it < (scrollThreadHold / 2f) }
                    ?: 0f
                (offsetY / (scrollThreadHold / 2f)).coerceIn(0f, 1f)
            }
        }
        val offsetYProgress = animateFloatAsState(
            targetValue = offsetY.value,
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            ),
        )

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
                            val targetOffset = scrollThreadHold +
                                    density.run { (seekbarPaddingBottom + seekbarHeight).toPx() }

                            animationState.animateTo(targetOffset) {
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
            modifier = Modifier
                .fillMaxWidth()
                .height(seekbarHeight)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)

                            when (event.type) {
                                PointerEventType.Press -> {
                                    // 开始触摸时，将当前可见的进度值记录下来
                                    progressKeeper.updateValue(animation.value)
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
                            in 0f..(boxSize.maxWidth / 3f) -> ClickPart.Start
                            in (boxSize.maxWidth * 2 / 3f)..boxSize.maxWidth.toFloat() -> ClickPart.End
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
                            val actualWidth = boxSize.maxWidth - density.run { 4.dp.roundToPx() }
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
                    compositingStrategy = CompositingStrategy.Offscreen

                    translationY = -offsetYProgress.value * (scrollThreadHold / 2f)
                    scaleX = 1f - (offsetYProgress.value * 0.1f)
                    scaleY = scaleX
                }
                .clip(RoundedCornerShape(16.dp))
        ) {
            SeekbarBackground(visible = { isTouching && !isCanceled })
            SeekbarContentMask(clip = { isTouching && !isCanceled })
            SeekbarDuration(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                visible = { !isSwitching },
                text = { maxDurationText },
                textStyle = textStyle
            )
            SeekbarThumb(
                clip = { isTouching && !isCanceled },
                thumbColor = animateColor,
                progress = { animation.value.normalize(minValue(), maxValue()) },
                switching = { isSwitching },
                switchModeX = { switchModeX.floatValue }
            )
            SeekbarDuration(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                visible = { !isSwitching },
                text = { currentTimeText.value },
                textStyle = textStyle,
                offsetProgress = { animation.value.normalize(minValue(), maxValue()) }
            )
            SeekbarSwitcher(switching = { isSwitching })
        }
    }
}

@Composable
private fun SeekbarBackground(
    modifier: Modifier = Modifier,
    bgColor: Color = MaterialTheme.colors.background,
    visible: () -> Boolean = { false }
) {
    val bgAlpha = animateFloatAsState(
        targetValue = if (visible()) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = "SeekbarBackground_bgAlpha"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            color = bgColor,
            alpha = bgAlpha.value
        )
    }
}

@Composable
private fun SeekbarContentMask(
    modifier: Modifier = Modifier,
    maskColor: Color = Color(0x33646464),
    clip: () -> Boolean = { false }
) {
    val path = remember { Path() }
    val clipProgress = animateFloatAsState(
        targetValue = if (clip()) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = "SeekbarContentMask_clipProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxPadding = 4.dp.toPx()
        val paddingValue = maxPadding * clipProgress.value

        val innerRadius = 16.dp.toPx() - paddingValue
        val innerHeight = size.height - (paddingValue * 2f)
        val innerWidth = size.width - (paddingValue * 2f)

        path.reset()
        path.addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = Offset(x = paddingValue, y = paddingValue),
                    size = Size(width = innerWidth, height = innerHeight)
                ),
                cornerRadius = CornerRadius(innerRadius, innerRadius)
            )
        )

        clipPath(path) {
            drawRect(color = maskColor)
        }
    }
}

@Composable
private fun SeekbarDuration(
    modifier: Modifier = Modifier,
    text: () -> String,
    textStyle: TextStyle,
    visible: () -> Boolean = { true },
    offsetProgress: () -> Float = { 0f }
) {
    val accumulator = remember { AccumulatedValue() }
    val alphaValue = animateFloatAsState(
        targetValue = if (visible()) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = "SeekbarDuration_alphaValue"
    )

    BoxWithConstraints(modifier = modifier.wrapContentSize()) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = alphaValue.value

                    val maxPadding = 4.dp.toPx()
                    val innerWidth = constraints.maxWidth - (maxPadding * 2f)

                    translationX =
                        ((innerWidth * offsetProgress()) - size.width - 32.dp.toPx())
                            .let { accumulator.accumulate(it).toFloat() }
                            .coerceAtLeast(0f)
                },
            text = text(),
            style = textStyle
        )
    }
}

@Composable
private fun SeekbarThumb(
    modifier: Modifier = Modifier,
    thumbColor: () -> Color = { Color(0xFF007AD5) },
    progress: () -> Float = { 1f },
    clip: () -> Boolean = { false },
    switching: () -> Boolean = { false },
    switchModeX: () -> Float = { 0f }
) {
    val path = remember { Path() }
    val clipProgress = animateFloatAsState(
        targetValue = if (clip()) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = "SeekbarThumb_clipProgress"
    )
    val switchProgress = animateFloatAsState(
        targetValue = if (switching()) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        visibilityThreshold = 0.001f,
        label = "SeekbarThumb_switchProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxPadding = 4.dp.toPx()
        val paddingValue = maxPadding * clipProgress.value

        val innerRadius = 16.dp.toPx() - paddingValue
        val innerHeight = size.height - (paddingValue * 2f)
        val innerWidth = size.width - (paddingValue * 2f)

        val thumbWidth = lerp(
            start = innerWidth * progress(),    // 根据进度计算的宽度
            stop = innerWidth / 3f,                 // 进度条均分宽度
            fraction = switchProgress.value      // 根据切换进度进行插值
        ).coerceIn(0f, innerWidth)

        val thumbLeft = lerp(
            start = paddingValue,
            stop = switchModeX() - (innerWidth / 3f) / 2f,
            fraction = switchProgress.value
        ).coerceIn(
            paddingValue,
            paddingValue + innerWidth - (innerWidth / 3f)
        )    // 限制滑块位置，确保其始终处于可见范围内

        path.reset()
        path.addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = Offset(x = paddingValue, y = paddingValue),
                    size = Size(width = innerWidth, height = innerHeight)
                ),
                cornerRadius = CornerRadius(innerRadius, innerRadius)
            )
        )

        clipPath(path) {
            // 绘制滑块
            drawRoundRect(
                color = thumbColor(),
                cornerRadius = CornerRadius(innerRadius, innerRadius),
                topLeft = Offset(x = thumbLeft, y = paddingValue),
                size = Size(width = thumbWidth, height = innerHeight)
            )
        }
    }
}

@Composable
private fun SeekbarSwitcher(
    modifier: Modifier = Modifier,
    switching: () -> Boolean = { false },
) {
    AnimatedVisibility(
        modifier = modifier.fillMaxSize(),
        visible = switching(),
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

private fun Long.durationToTime(): String {
    val hour = this / 3600000
    val minute = this / 60000 % 60
    val second = this / 1000 % 60
    return if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
    else "%02d:%02d".format(minute, second)
}

@Composable
private fun durationToText(
    duration: () -> Long = { 0L }
): MutableState<String> {
    val durationText = remember { mutableStateOf(duration().durationToTime()) }

    LaunchedEffect(Unit) {
        var lastTime = -1L
        var hour = 0
        var minute = 0
        var second = 0

        snapshotFlow { duration() }
            .onEach { timeValue ->
                if (timeValue / 1000L != lastTime) {
                    val hourTemp = (timeValue / 3600000).toInt()
                    val minuteTemp = (timeValue / 60000 % 60).toInt()
                    val secondTemp = (timeValue / 1000 % 60).toInt()

                    if (hourTemp != hour || minuteTemp != minute || secondTemp != second) {
                        hour = hourTemp
                        minute = minuteTemp
                        second = secondTemp

                        durationText.value =
                            if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
                            else "%02d:%02d".format(minute, second)
                    }
                }
                lastTime = timeValue / 1000L
            }
            .launchIn(this)
    }

    return durationText
}