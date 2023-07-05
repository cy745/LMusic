package com.lalilu.lmusic.ui

import android.widget.OverScroller
import androidx.compose.ui.util.fastAny
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import kotlin.math.abs
import kotlin.math.roundToInt

open class AppbarOffsetHelper(protected val appbar: CoverAppbar) {
    private var layoutTop = 0
    private var layoutBottom = 0
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
        updateOffset()
    }

    open fun onViewLayout() {
        layoutTop = appbar.top
        layoutBottom = appbar.bottom

        if (layoutTop != position) {
            updateOffset()
            snapIfNeeded()
        }
    }

    open fun updateOffset() {
        ViewCompat.offsetTopAndBottom(
            appbar,
            position.coerceAtMost(0) - (appbar.top - layoutTop)
        )
        appbar.bottom = layoutBottom + position
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
        if (!anchors.fastAny { it() == position }) {
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
}