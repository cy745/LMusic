package com.lalilu.lmusic.screen.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.math.MathUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

private val SliderToTickAnimation = TweenSpec<Float>(durationMillis = 100)
private val SliderHeight = 36.dp
private val SliderMinWidth = 48.dp
private val DefaultSliderConstraints = Modifier
    .widthIn(min = SliderMinWidth)
    .heightIn(max = SliderHeight)

@Composable
fun StateSeekBar(
    value: Float,
    selections: List<String>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinished: (() -> Unit)? = null,
) {
    require(steps >= 0) { "steps should be >= 0" }
    val onValueChangeState = rememberUpdatedState(onValueChange)

    // 根据 steps 划分出锚点值
    val tickFractions = remember(steps) {
        stepsToTickFractions(steps)
    }

    val selectionOffset = selections.size * 2f

    BoxWithConstraints(
        modifier
            .requiredSizeIn(minWidth = SliderHeight, minHeight = SliderHeight)
            .sliderSemantics(value, tickFractions, enabled, onValueChange, valueRange, steps)
            .focusable(enabled, interactionSource)
    ) {
        // 布局方向是否右至左（右向排版的手机用的）
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val widthPx = constraints.maxWidth.toFloat()

        // 获取起点和终点值
        val minPx = widthPx / selectionOffset
        val maxPx = widthPx - minPx

        // 将 [minPx, maxPx] 缩放至 valueRange, 并获取offset在range中的百分比值
        fun scaleToUserValue(offset: Float) =
            scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

        // 将 valueRange 缩放至 [minPx, maxPx], 并获取offset在range中的百分比值
        fun scaleToOffset(userValue: Float) =
            scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

        val scope = rememberCoroutineScope()
        // rawOffset 根据外部传入的 value 进行变化，
        // 实际就是将外部的 value 缩放至 [minPx, maxPx] 域中对应的值
        val rawOffset = remember { mutableStateOf(scaleToOffset(value)) }

        // draggableState 根据 minPx, maxPx, valueRange 三个值进行重构
        val draggableState = remember(minPx, maxPx, valueRange) {

            // SliderDraggableState 负责接收处理 drag 事件并产生 delta 值供使用
            SliderDraggableState { delta ->
                rawOffset.value = (rawOffset.value + delta)
                val offsetInTrack = rawOffset.value.coerceIn(minPx, maxPx)

                // 将 offset 值变换成 valueRange 中的值后传出外部
                onValueChangeState.value.invoke(scaleToUserValue(offsetInTrack))
            }
        }

        CorrectValueSideEffect(::scaleToOffset, valueRange, minPx..maxPx, rawOffset, value)

        // 监听手势结束的事件，并执行动画使值平滑过渡至锚点值
        val gestureEndAction = rememberUpdatedState<(Float) -> Unit> { velocity: Float ->
            val current = rawOffset.value
            // 计算出应该贴向的目标值
            val target = snapValueToTick(current, tickFractions, minPx, maxPx)

            if (current != target) {
                scope.launch {
                    animateToTarget(draggableState, current, target, velocity)
                    onValueChangeFinished?.invoke()
                }
            } else if (!draggableState.isDragging) {
                // check ifDragging in case the change is still in progress (touch -> drag case)
                onValueChangeFinished?.invoke()
            }
        }

        val press = Modifier.sliderPressModifier(
            draggableState = draggableState,
            interactionSource = interactionSource,
            tickFractions = tickFractions,
            minPx = minPx,
            maxPx = maxPx,
            isRtl = isRtl,
            rawOffset = rawOffset,
            gestureEndAction = gestureEndAction,
            enabled = enabled,
            scope = scope,
            onValueChangeFinished = onValueChangeFinished
        )

        val drag = Modifier.draggable(
            orientation = Orientation.Horizontal,
            reverseDirection = isRtl,
            enabled = enabled,
            interactionSource = interactionSource,
            onDragStopped = { velocity -> gestureEndAction.value.invoke(velocity) },
            startDragImmediately = draggableState.isDragging,
            state = draggableState
        )

        val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
        val fraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)

        SliderImpl(
            enabled = enabled,
            selections = selections,
            positionFraction = fraction,
            tickFractions = tickFractions,
            draggableState = draggableState,
            colors = SliderDefaults.colors(),
            width = maxPx - minPx,
            interactionSource = interactionSource,
            modifier = press.then(drag)
        )
    }
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    selections: List<String>,
    positionFraction: Float,
    tickFractions: List<Float>,
    draggableState: SliderDraggableState,
    colors: SliderColors,
    width: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier
) {
    val interactions = remember { mutableStateListOf<Interaction>() }
    val paddingAnim = animateFloatAsState(
        targetValue = if (interactions.isNotEmpty()) 3f else 0f
    )
    val radiusAnim = animateFloatAsState(
        targetValue = if (interactions.isNotEmpty()) 8f else 10f
    )
    val backgroundColor = MaterialTheme.colors.background
    val bgColor = contentColorFor(backgroundColor = backgroundColor).copy(0.2f)
    val thumbColor = contentColorFor(backgroundColor = backgroundColor).copy(0.7f)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    Surface(shape = RoundedCornerShape(10.dp)) {
        Box(modifier.then(DefaultSliderConstraints)) {
            val offset = width * positionFraction

            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingAnim.value.dp)
                    .clip(RoundedCornerShape(radiusAnim.value.dp))
                    .background(color = bgColor)
            )
            Spacer(
                modifier = Modifier
                    .graphicsLayer { translationX = offset }
                    .fillMaxHeight()
                    .fillMaxWidth(1f / selections.size)
                    .padding(paddingAnim.value.dp)
                    .clip(RoundedCornerShape(radiusAnim.value.dp))
                    .background(color = thumbColor)
            )
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(selections.size),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.Center,
                userScrollEnabled = false
            ) {
                selections.forEach {
                    item {
                        Text(
                            text = it,
                            color = backgroundColor,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 为Modifier 添加
 */
private fun Modifier.sliderPressModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    tickFractions: List<Float>,
    minPx: Float,
    maxPx: Float,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<(Float) -> Unit>,
    enabled: Boolean,
    scope: CoroutineScope,
    onValueChangeFinished: (() -> Unit)? = null
) = composed(
    factory = {
        if (enabled) {
            val animateTo: MutableState<suspend (Float) -> Unit> = remember { mutableStateOf({}) }
            LaunchedEffect(
                rawOffset, tickFractions,
                draggableState, minPx, maxPx, isRtl,
                onValueChangeFinished
            ) {
                animateTo.value = { offset ->
                    val to = if (isRtl) maxPx - offset else offset
                    val current = rawOffset.value
                    val target = snapValueToTick(to, tickFractions, minPx, maxPx)
                    animateToTarget(draggableState, current, target, 2f)
                    onValueChangeFinished?.invoke()
                }
            }
            Modifier.pointerInput(interactionSource) {
                detectTapGestures { offset ->
                    scope.launch { animateTo.value.invoke(offset.x) }
                }
            }
        } else {
            this
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "sliderPressModifier"
        properties["draggableState"] = draggableState
        properties["interactionSource"] = interactionSource
        properties["maxPx"] = maxPx
        properties["isRtl"] = isRtl
        properties["rawOffset"] = rawOffset
        properties["gestureEndAction"] = gestureEndAction
        properties["enabled"] = enabled
    })

private suspend fun animateToTarget(
    draggableState: DraggableState,
    current: Float,
    target: Float,
    velocity: Float
) {
    draggableState.drag {
        var latestValue = current
        Animatable(initialValue = current)
            .animateTo(target, SliderToTickAnimation, velocity) {
                dragBy(this.value - latestValue)
                latestValue = this.value
            }
    }
}


private fun snapValueToTick(
    current: Float,
    tickFractions: List<Float>,
    minPx: Float,
    maxPx: Float
): Float {
    // target is a closest anchor to the `current`, if exists
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

@Composable
private fun CorrectValueSideEffect(
    scaleToOffset: (Float) -> Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackRange: ClosedFloatingPointRange<Float>,
    valueState: MutableState<Float>,
    value: Float
) {
    SideEffect {
        val error = (valueRange.endInclusive - valueRange.start) / 1000
        val newOffset = scaleToOffset(value)
        if (abs(newOffset - valueState.value) > error) {
            if (valueState.value in trackRange) {
                valueState.value = newOffset
            }
        }
    }
}

// 计算 pos 在 [a, b] 中的位置 0f..1f
// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)


// 将 x1 从 [a1, b1] 定义域变换至 [a2, b2] 定义域
// Scale x1 from a1..b1 range to a2..b2 range
private fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))


private fun stepsToTickFractions(steps: Int): List<Float> {
    return if (steps == 0) emptyList() else List(steps + 2) { it.toFloat() / (steps + 1) }
}

private class SliderDraggableState(
    val onDelta: (Float) -> Unit
) : DraggableState {

    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float): Unit = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit
    ): Unit = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    override fun dispatchRawDelta(delta: Float) {
        return onDelta(delta)
    }
}


/**
 * 为滑块添加语义说明，用于适配无障碍服务
 */
private fun Modifier.sliderSemantics(
    value: Float,
    tickFractions: List<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                val newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                val resolvedValue = if (steps > 0) {
                    tickFractions.map { lerp(valueRange.start, valueRange.endInclusive, it) }
                        .minByOrNull { abs(it - newValue) } ?: newValue
                } else {
                    newValue
                }
                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (resolvedValue == coerced) {
                    false
                } else {
                    onValueChange(resolvedValue)
                    true
                }
            }
        )
    }.progressSemantics(value, valueRange, steps)
}