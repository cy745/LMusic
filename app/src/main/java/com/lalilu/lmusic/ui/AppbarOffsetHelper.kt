package com.lalilu.lmusic.ui

import android.widget.OverScroller
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import kotlin.math.abs
import kotlin.math.roundToInt

open class AppbarOffsetHelper(
    protected val appbar: CoverAppbar,
) : AppbarProgressHelper() {
    private var scroller: OverScroller? = null
    private val animator: SpringAnimation by lazy {
        springAnimationOf(
            setter = { setPosition(it.toInt()) },
            getter = { position.toFloat() },
            finalPosition = 0f
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
    }
    private val anchors = listOf(::minPosition::get, ::maxPosition::get, { 0 })

    val minPosition: Int
        get() = appbar.minAnchorHeight - appbar.middleAnchorHeight
    val maxPosition: Int
        get() = appbar.maxAnchorHeight - appbar.middleAnchorHeight

    /**
     * 实际控制元素位置的变量
     * [(minHeight - middleHeight) ~ 0 ~ (maxHeight - middleHeight)]
     */
    var position = 0
        private set

    /**
     * 更新当前位置
     *
     * @return 当前位置是否发生变更
     */
    protected open fun setPosition(value: Number) {
        if (position == value) return
        position = value.toInt().coerceIn(minPosition, maxPosition)
        updateProgress(minPosition, maxPosition, position)
        updateOffset(position)
    }

    open fun onViewLayout() {
        updateOffsetFromProgress()
        snapIfNeeded()
    }

    open fun updateOffsetFromProgress() {
        if (fullProgress == -1f) {
            updateProgress(minPosition, maxPosition, position)
        }
        if (fullProgress != -1f) {
            position = lerp(minPosition, maxPosition, fullProgress)
        }
        updateOffset(position)
    }

    open fun updateOffset(target: Int) {
//        ViewCompat.offsetTopAndBottom(appbar, target.coerceAtMost(0) - appbar.top)
        appbar.bottom = appbar.middleAnchorHeight + target
    }

    open fun scrollBy(dy: Int): Int {
        val nowPosition = position
        setPosition(position - dy)
        return nowPosition - position
    }

    open fun animateTo(position: Number) {
        animator.animateToFinalPosition(position.toFloat())
    }

    open fun cancelAnimation() {
        if (animator.isRunning) {
            animator.cancel()
        }
    }

    open fun snapIfNeeded() {
        if (!anchors.any { it() == position }) {
            snapBy(position)
        }
    }

    open fun snapBy(position: Int) {
        val target = calcSnapToOffset(position, anchors.map { it() })
        animateTo(target)
    }

    open fun calcSnapToOffset(value: Int, vararg anchors: Int): Int {
        return calcSnapToOffset(value, anchors.asIterable())
    }

    open fun calcSnapToOffset(value: Int, anchors: Iterable<Int>): Int {
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

    open fun fling(velocityY: Float) {
        scroller = scroller ?: OverScroller(appbar.context)
        scroller!!.fling(
            0, position,            // startX / Y
            0, velocityY.roundToInt(),  // velocityX / Y
            0, 0,                    // minX / maxX
            minPosition, maxPosition              // minY / maxY
        )

        snapBy(scroller!!.finalY)
    }

    private fun lerp(start: Int, stop: Int, fraction: Float): Int {
        return start + ((stop - start) * fraction.toDouble()).roundToInt()
    }
}