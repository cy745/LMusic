package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class DragAnchor {
    Min, MinXMiddle, Middle, MiddleXMax, Max;

    companion object {
        val Saver = Saver<MutableState<DragAnchor>, Int>(
            save = { it.value.ordinal },
            restore = { mutableStateOf(getByOrdinal(it)) }
        )

        private fun getByOrdinal(ordinal: Int): DragAnchor {
            return when (ordinal) {
                0 -> Min
                1 -> MinXMiddle
                2 -> Middle
                3 -> MiddleXMax
                4 -> Max
                else -> Middle
            }
        }
    }
}

class CustomAnchoredDraggableState(
    private val scope: CoroutineScope,
    private val flingBehavior: FlingBehavior,
    private val initAnchor: () -> DragAnchor,
    private val onStateChange: (DragAnchor, DragAnchor) -> Unit = { _, _ -> },
) : ScrollableState {
    private val animation by lazy { Animatable(0f, Float.VectorConverter) }
    private val dragThreshold = 120
    private var minPosition = Int.MIN_VALUE
    private var middlePosition = Int.MIN_VALUE
    private var maxPosition = Int.MIN_VALUE
    val position = mutableFloatStateOf(Float.MIN_VALUE)
    val state = mutableStateOf(initAnchor())

    private val scrollMutex = MutatorMutex()
    private val isScrollingState = mutableStateOf(false)
    private val isLastScrollForwardState = mutableStateOf(false)
    private val isLastScrollBackwardState = mutableStateOf(false)
    override val isScrollInProgress: Boolean
        get() = isScrollingState.value

    private val scrollScope: ScrollScope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float {
            if (pixels.isNaN()) return 0f
            val delta = dispatchRawDelta(pixels)
            isLastScrollForwardState.value = delta > 0
            isLastScrollBackwardState.value = delta < 0
            return delta
        }
    }

    override fun dispatchRawDelta(delta: Float): Float {
        val dyResult = dampDy(delta)
        val oldPosition = position.floatValue
        updatePosition(position.floatValue + dyResult)
        return position.floatValue - oldPosition
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        scrollMutex.mutateWith(scrollScope, scrollPriority) {
            isScrollingState.value = true
            try {
                block()
            } finally {
                isScrollingState.value = false
            }
        }
    }

    private var oldStateValue: DragAnchor = initAnchor()
        set(value) {
            if (field == value) return
            field = value
        }

    private var stateValue: DragAnchor = initAnchor()
        set(value) {
            if (field == value) return
            oldStateValue = field
            state.value = value
            onStateChange(field, value)
            field = value
        }

    fun updateAnchor(min: Int, middle: Int, max: Int) {
        minPosition = min
        middlePosition = middle
        maxPosition = max

        if (position.floatValue == Float.MIN_VALUE) {
            val targetPosition = getPositionByAnchor(initAnchor()) ?: middlePosition
            updatePosition(targetPosition.toFloat())
        }
    }

    private fun updatePosition(newPosition: Float) {
        position.floatValue = newPosition.coerceIn(minPosition.toFloat(), maxPosition.toFloat())
        stateValue = getStateByPosition(position.floatValue.toInt())
    }

    private fun getStateByPosition(value: Int): DragAnchor = when (value) {
        in minPosition until (minPosition + dragThreshold) -> DragAnchor.Min
        in (minPosition + dragThreshold) until (middlePosition - dragThreshold) -> DragAnchor.MinXMiddle
        in (middlePosition - dragThreshold) until (middlePosition + dragThreshold) -> DragAnchor.Middle
        in (middlePosition + dragThreshold) until (maxPosition - dragThreshold) -> DragAnchor.MiddleXMax
        else -> DragAnchor.Max
    }

    private fun getSnapPositionByState(state: DragAnchor): Int = when (state) {
        DragAnchor.Min -> minPosition
        DragAnchor.Middle -> middlePosition
        DragAnchor.Max -> maxPosition

        DragAnchor.MinXMiddle -> when (oldStateValue) {
            DragAnchor.Min -> middlePosition
            DragAnchor.Middle -> minPosition
            else -> calcSnapToPosition(position.floatValue.toInt(), middlePosition, minPosition)
        }

        DragAnchor.MiddleXMax -> when (oldStateValue) {
            DragAnchor.Middle -> maxPosition
            DragAnchor.Max -> middlePosition
            else -> calcSnapToPosition(position.floatValue.toInt(), middlePosition, maxPosition)
        }
    }

    private fun calcSnapToPosition(value: Int, vararg anchors: Int): Int {
        return calcSnapToPosition(value, anchors.asIterable())
    }

    private fun calcSnapToPosition(value: Int, anchors: Iterable<Int>): Int {
        var min = Int.MAX_VALUE
        var target = value
        for (anchor in anchors) {
            val temp = abs(value - anchor)
            if (temp < min) {
                min = temp
                target = anchor
            }
        }
        return target
    }

    private fun dampDy(dy: Float): Float {
        var result = dy
        if (dy > 0) {
            val percent = 1f - (position.floatValue - middlePosition) / dragThreshold * 0.5f
            if (percent in 0F..1F) result = dy * percent
        }
        return result
    }

    fun getPositionByAnchor(anchor: DragAnchor) = when (anchor) {
        DragAnchor.Min -> minPosition
        DragAnchor.Middle -> middlePosition
        DragAnchor.Max -> maxPosition
        else -> null
    }

    fun progressBetween(
        from: DragAnchor,
        to: DragAnchor,
        offset: Float = position.floatValue,
        defaultValue: Float = 0f,
    ): Float {
        val a = getPositionByAnchor(from) ?: return defaultValue
        val b = getPositionByAnchor(to) ?: return defaultValue
        val distance = abs(b - a)

        if (distance <= 1e-6f) return defaultValue

        val progress = (offset - a) / (b - a)
        return if (progress < 1e-6f) 0f else if (progress > 1 - 1e-6f) 1f else progress
    }

    suspend fun fling(velocityY: Float): Float {
        if (velocityY == 0f) {
            animateToState()
            return velocityY
        }

        var velocityLeft = velocityY
        scroll { velocityLeft = with(flingBehavior) { performFling(velocityY) } }

        if (velocityLeft == 0f) {
            snapBy(position.floatValue.toInt())
        }

        return velocityLeft
    }

    fun snapBy(targetPosition: Int) {
        when (stateValue) {
            DragAnchor.Middle -> {
                val position =
                    calcSnapToPosition(targetPosition, minPosition, middlePosition, maxPosition)
                scope.launch { doAnimateTo(position.toFloat()) }
            }

            DragAnchor.Min -> {
                val position = calcSnapToPosition(targetPosition, minPosition, middlePosition)
                scope.launch { doAnimateTo(position.toFloat()) }
            }

            DragAnchor.Max -> {
                val position =
                    calcSnapToPosition(targetPosition, middlePosition, maxPosition)
                scope.launch { doAnimateTo(position.toFloat()) }
            }

            else -> animateToState()
        }
    }

    fun animateToState(newState: DragAnchor = stateValue) {
        val targetPosition = getSnapPositionByState(newState)
        scope.launch { doAnimateTo(targetPosition.toFloat()) }
    }

    fun tryCancel() {
        if (animation.isRunning) {
            scope.launch { animation.stop() }
        }
    }

    suspend fun doAnimateTo(offset: Float) {
        animation.apply {
            snapTo(position.floatValue)
            animateTo(
                targetValue = offset,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) {
                updatePosition(value)
            }
        }
    }
}


@Composable
fun rememberCustomAnchoredDraggableState(
    onStateChange: (DragAnchor, DragAnchor) -> Unit = { _, _ -> }
): CustomAnchoredDraggableState {
    var initAnchor by rememberSaveable(saver = DragAnchor.Saver) { mutableStateOf(DragAnchor.Middle) }
    val flingBehavior = ScrollableDefaults.flingBehavior()
    val scope = rememberCoroutineScope()

    return remember {
        CustomAnchoredDraggableState(
            scope = scope,
            flingBehavior = flingBehavior,
            initAnchor = { initAnchor },
            onStateChange = { oldState, newState ->
                initAnchor = newState
                onStateChange(oldState, newState)
            }
        )
    }
}