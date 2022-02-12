package com.lalilu.material.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
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

    open fun canDragView(view: V): Boolean {
        return false
    }

    open fun onFlingFinished(parent: CoordinatorLayout, layout: V) {
        // no-op
    }

    open fun getMaxDragOffset(view: V): Int {
        return -view.height
    }

    open fun getScrollRangeForDragFling(view: V): Int {
        return view.height
    }

    open fun getTopBottomOffsetForScrollingSibling(): Int {
        return topAndBottomOffset
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
            val offset = MathUtils.clamp(newOffset, minOffset, maxOffset)
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
                scroller?.let {
                    if (!it.isFinished) {
                        it.abortAnimation()
                        return true
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
                scroll(parent, child, dy, -child.upNestedPreScrollRange, parent.height / 3)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val newIndex = if (ev.actionIndex == 0) 1 else 0
                activePointerId = ev.getPointerId(newIndex)
                lastMotionY = (ev.getY(newIndex) + 0.5f).toInt()
            }
            MotionEvent.ACTION_UP -> {
                if (velocityTracker != null) {
                    consumeUp = true
                    velocityTracker!!.addMovement(ev)
                    velocityTracker!!.computeCurrentVelocity(1000)
                    val yvel = velocityTracker!!.getYVelocity(activePointerId)
                    fling(parent, child, -getScrollRangeForDragFling(child), 0, yvel)
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

    fun scroll(
        coordinatorLayout: CoordinatorLayout, header: V, dy: Int, minOffset: Int, maxOffset: Int
    ): Int {
        return setHeaderTopBottomOffset(
            coordinatorLayout,
            header,
            getTopBottomOffsetForScrollingSibling() - dy,
            minOffset,
            maxOffset
        )
    }

    fun fling(
        coordinatorLayout: CoordinatorLayout,
        layout: V,
        minOffset: Int,
        maxOffset: Int,
        velocityY: Float
    ): Boolean {
        if (flingRunnable != null) {
            layout.removeCallbacks(flingRunnable)
            flingRunnable = null
        }
        scroller = scroller ?: OverScroller(layout.context)
        scroller!!.fling(
            0, topAndBottomOffset,            // startX / Y
            0, velocityY.roundToInt(),     // velocityX / Y
            0, 0,                       // minX / maxX
            minOffset, maxOffset                   // minY / maxY
        )
        return if (scroller!!.computeScrollOffset()) {
            flingRunnable = FlingRunnable(coordinatorLayout, layout)
            ViewCompat.postOnAnimation(layout, flingRunnable!!)
            true
        } else {
            onFlingFinished(coordinatorLayout, layout)
            false
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

    class HeaderOffsetFloatProperty(
        private val parent: CoordinatorLayout,
        private val child: AppBarLayout
    ) : FloatPropertyCompat<ExpendHeaderBehavior<AppBarLayout>>("header_offset") {
        override fun getValue(obj: ExpendHeaderBehavior<AppBarLayout>): Float {
            return obj.getTopBottomOffsetForScrollingSibling().toFloat()
        }

        override fun setValue(obj: ExpendHeaderBehavior<AppBarLayout>, value: Float) {
            obj.setHeaderTopBottomOffset(parent, child, value.roundToInt())
        }
    }
}