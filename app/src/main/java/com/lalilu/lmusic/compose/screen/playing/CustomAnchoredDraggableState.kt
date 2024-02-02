package com.lalilu.lmusic.compose.screen.playing

import android.content.Context
import android.widget.OverScroller
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import kotlin.math.abs

enum class DragAnchor {
    Min, MinXMiddle, Middle, MiddleXMax, Max;

    companion object {
        val Saver = Saver<MutableState<DragAnchor>, Int>(
            save = { it.value.ordinal },
            restore = { mutableStateOf(getByOrdinal(it)) }
        )

        fun getByOrdinal(ordinal: Int): DragAnchor {
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
    context: Context,
    private val initAnchor: () -> DragAnchor,
    private val onStateChange: (DragAnchor, DragAnchor) -> Unit = { _, _ -> }
) {
    private val animator: SpringAnimation by lazy {
        springAnimationOf(
            setter = { updatePosition(it) },
            getter = { position.floatValue },
            finalPosition = 0f
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }.apply {
            addEndListener { animation, canceled, value, velocity ->

            }
        }
    }
    private val dragThreshold = 200
    private val overScroller by lazy { OverScroller(context) }

    private var minPosition = Int.MIN_VALUE
    private var middlePosition = Int.MIN_VALUE
    private var maxPosition = Int.MIN_VALUE

    val position = mutableFloatStateOf(Float.MIN_VALUE)
    val state = mutableStateOf(initAnchor())

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

    fun scrollBy(dy: Float): Float {
        val dyResult = dampDy(dy)
        val oldPosition = position.floatValue
        updatePosition(position.floatValue + dyResult)
        return position.floatValue - oldPosition
    }

    fun fling(velocityY: Float): Float {
        if (velocityY == 0f) {
            animateToState()
            return velocityY
        }

        overScroller.fling(
            0, position.floatValue.toInt(),
            0, velocityY.toInt(),
            0, 0,
            minPosition,
            maxPosition
        )

        snapBy(overScroller.finalY)
        return velocityY
    }

    fun snapBy(targetPosition: Int) {
        when (stateValue) {
            DragAnchor.Middle -> {
                val position =
                    calcSnapToPosition(targetPosition, minPosition, middlePosition, maxPosition)
                animator.animateToFinalPosition(position.toFloat())
            }

            DragAnchor.Min -> {
                val position =
                    calcSnapToPosition(targetPosition, minPosition, middlePosition)
                animator.animateToFinalPosition(position.toFloat())
            }

            DragAnchor.Max -> {
                val position =
                    calcSnapToPosition(targetPosition, middlePosition, maxPosition)
                animator.animateToFinalPosition(position.toFloat())
            }

            else -> animateToState()
        }
    }

    fun animateToState(newState: DragAnchor = stateValue) {
        val targetPosition = getSnapPositionByState(newState)
        animator.animateToFinalPosition(targetPosition.toFloat())
    }

    fun tryCancel() {
        if (animator.isRunning) {
            animator.cancel()
        }
    }
}


@Composable
fun rememberCustomAnchoredDraggableState(
    context: Context = LocalContext.current,
    onStateChange: (DragAnchor, DragAnchor) -> Unit = { _, _ -> }
): CustomAnchoredDraggableState {
    var initAnchor by rememberSaveable(saver = DragAnchor.Saver) { mutableStateOf(DragAnchor.Middle) }

    return remember {
        CustomAnchoredDraggableState(
            context = context,
            initAnchor = { initAnchor },
            onStateChange = { oldState, newState ->
                initAnchor = newState
                onStateChange(oldState, newState)
            }
        )
    }
}