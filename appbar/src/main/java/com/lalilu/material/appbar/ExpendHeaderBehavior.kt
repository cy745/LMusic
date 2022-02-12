package com.lalilu.material.appbar

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class ExpendHeaderBehavior<V : AppBarLayout>(
    context: Context?, attrs: AttributeSet?
) : ViewOffsetExpendBehavior<V>(context, attrs) {

    private val INVALID_POINTER = -1

    var scroller: OverScroller? = null
    var mSpringAnimation: SpringAnimation? = null
    private var flingRunnable: Runnable? = null

    private var isBeingDragged = false
    private var activePointerId = INVALID_POINTER
    private var lastMotionY = 0
    private var touchSlop = -1
    private var velocityTracker: VelocityTracker? = null

    open fun getCollapsedOffset(parent: View, child: V): Int {
        return -child.upNestedPreScrollRange
    }

    // todo 需要换一种方式获取
    open fun getFullyExpendOffset(parent: View, child: V): Int {
        val windowManager: WindowManager =
            parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(outMetrics)
        return outMetrics.heightPixels - parent.width
    }

    open fun canDragView(view: V): Boolean {
        return false
    }

    open fun onFlingFinished(parent: CoordinatorLayout, layout: V) {
        // no-op
    }

    open fun setHeaderTopBottomOffset(
        parent: CoordinatorLayout,
        header: V, newOffset: Int
    ): Int {
        return setHeaderTopBottomOffset(
            parent, header, newOffset,
            Int.MIN_VALUE, Int.MAX_VALUE
        )
    }

    open fun setHeaderTopBottomOffset(
        parent: CoordinatorLayout,
        header: V,
        newOffset: Int,
        minOffset: Int,
        maxOffset: Int
    ): Int {
        var consumed = 0
        val curOffset = topAndBottomOffset
        if (minOffset != 0 && curOffset in minOffset..maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            val offset = newOffset.coerceIn(minOffset, maxOffset)
            if (curOffset != offset) {
                topAndBottomOffset = offset
                // Update how much dy we have consumed
                consumed = curOffset - offset
            }
        }
        return consumed
    }


    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        ev: MotionEvent
    ): Boolean {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(parent.context).scaledTouchSlop
        }

        // Shortcut since we're being dragged
        if (ev.actionMasked == MotionEvent.ACTION_MOVE && isBeingDragged) {
            if (activePointerId == INVALID_POINTER) {
                // If we don't have a valid id, the touch down wasn't on content.
                return false
            }
            val pointerIndex = ev.findPointerIndex(activePointerId)
            if (pointerIndex == -1) {
                return false
            }
            val y = ev.getY(pointerIndex).toInt()
            val yDiff = abs(y - lastMotionY)
            if (yDiff > touchSlop) {
                lastMotionY = y
                return true
            }
        }

        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            activePointerId = INVALID_POINTER
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            isBeingDragged = canDragView(child) && parent.isPointInChildBounds(child, x, y)
            if (isBeingDragged) {
                lastMotionY = y
                activePointerId = ev.getPointerId(0)
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()

                // There is an animation in progress. Stop it and catch the view.
                mSpringAnimation?.let {
                    if (it.isRunning) {
                        it.cancel()
                    }
                }
            }
        }
        velocityTracker?.addMovement(ev)
        return false
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout, child: V, ev: MotionEvent
    ): Boolean {
        var consumeUp = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = ev.findPointerIndex(activePointerId)
                if (activePointerIndex == -1) {
                    return false
                }
                val y = ev.getY(activePointerIndex).toInt()
                val dy = lastMotionY - y
                lastMotionY = y
                // We're being dragged so scroll the ABL
                scroll(parent, child, dy)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val newIndex = if (ev.actionIndex == 0) 1 else 0
                activePointerId = ev.getPointerId(newIndex)
                lastMotionY = (ev.getY(newIndex) + 0.5f).toInt()
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.let {
                    consumeUp = true
                    it.addMovement(ev)
                    it.computeCurrentVelocity(1000)
                    val velocityY = it.getYVelocity(activePointerId)
                    fling(parent, child, velocityY)
                }
                isBeingDragged = false
                activePointerId = INVALID_POINTER
                velocityTracker?.let {
                    it.recycle()
                    velocityTracker = null
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isBeingDragged = false
                activePointerId = INVALID_POINTER
                velocityTracker?.let {
                    it.recycle()
                    velocityTracker = null
                }
            }
        }
        velocityTracker?.addMovement(ev)
        return isBeingDragged || consumeUp
    }

    fun animateOffsetTo(
        parent: CoordinatorLayout,
        child: V,
        offset: Int
    ) {
        mSpringAnimation = mSpringAnimation
            ?: SpringAnimation(
                this, HeaderOffsetFloatProperty(parent, child),
                topAndBottomOffset.toFloat()
            ).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                spring.stiffness = SpringForce.STIFFNESS_LOW
            }
        mSpringAnimation?.cancel()
        mSpringAnimation?.animateToFinalPosition(offset.toFloat())
    }

    fun scroll(
        parent: CoordinatorLayout,
        header: V,
        dy: Int,
        minOffset: Int = getCollapsedOffset(parent, header),
        maxOffset: Int = getFullyExpendOffset(parent, header)
    ): Int {
        return setHeaderTopBottomOffset(
            parent, header,
            topAndBottomOffset - dy,
            minOffset, maxOffset
        )
    }

    fun fling(
        parent: CoordinatorLayout,
        header: V,
        velocityY: Float,
        minOffset: Int = getCollapsedOffset(parent, header),
        maxOffset: Int = getFullyExpendOffset(parent, header),
    ): Boolean {
        stopFling(header)
        scroller = scroller ?: OverScroller(header.context)
        scroller!!.fling(
            0, topAndBottomOffset,            // startX / Y
            0, velocityY.roundToInt(),     // velocityX / Y
            0, 0,                       // minX / maxX
            minOffset, maxOffset                   // minY / maxY
        )
        animateOffsetTo(parent, header, scroller!!.finalY)
        return true
//        return if (scroller!!.computeScrollOffset()) {
//            flingRunnable = FlingRunnable(coordinatorLayout, layout)
//            ViewCompat.postOnAnimation(layout, flingRunnable!!)
//            true
//        } else {
//            onFlingFinished(coordinatorLayout, layout)
//            false
//        }
    }

    fun stopFling(header: V) {
        if (flingRunnable != null) {
            header.removeCallbacks(flingRunnable)
            flingRunnable = null
        }
    }

    inner class FlingRunnable constructor(
        private val parent: CoordinatorLayout,
        private val layout: V?
    ) : Runnable {
        override fun run() {
            if (layout != null && scroller != null) {
                if (scroller!!.computeScrollOffset()) {
                    setHeaderTopBottomOffset(parent, layout, scroller!!.currY)
                    // Post ourselves so that we run on the next animation
                    ViewCompat.postOnAnimation(layout, this)
                } else {
                    onFlingFinished(parent, layout)
                }
            }
        }
    }

    inner class HeaderOffsetFloatProperty(
        private val parent: CoordinatorLayout,
        private val child: V
    ) : FloatPropertyCompat<ExpendHeaderBehavior<V>>("header_offset") {
        override fun getValue(obj: ExpendHeaderBehavior<V>): Float {
            return obj.topAndBottomOffset.toFloat()
        }

        override fun setValue(obj: ExpendHeaderBehavior<V>, value: Float) {
            obj.setHeaderTopBottomOffset(parent, child, value.roundToInt())
        }
    }
}