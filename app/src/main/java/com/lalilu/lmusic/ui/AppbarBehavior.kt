package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class AppbarBehavior(
    context: Context?, attrs: AttributeSet?,
) : CoordinatorLayout.Behavior<CoverAppbar>(context, attrs) {

    constructor(appbar: CoverAppbar) : this(appbar.context, null) {
        ensureHelper(appbar)
    }

    companion object {
        const val INVALID_POINTER = -1
    }

    @ViewCompat.NestedScrollType
    private var lastStartedType = 0
    private var velocityTracker: VelocityTracker? = null
    private var isBeingDragged = false
    private var activePointerId = -1
    private var lastMotionY = 0
    private var lastMotionX = 0
    private var touchSlop = -1

    lateinit var positionHelper: AppbarStateHelper

    private fun ensureHelper(view: CoverAppbar) {
        if (!::positionHelper.isInitialized) {
            positionHelper = AppbarStateHelper(view)
        }
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: CoverAppbar,
        layoutDirection: Int,
    ): Boolean {
        parent.onLayoutChild(child, layoutDirection)

        ensureHelper(child)
        positionHelper.onViewLayout(fromOutside = false)
        return true
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: CoverAppbar,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int,
    ): Boolean {
        val started = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 && target is RecyclerView
        if (started) {
            ensureHelper(child)
            positionHelper.cancelAnimation()
        }

        lastStartedType = type
        return started
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: CoverAppbar,
        target: View,
        type: Int,
    ) {
        if ((lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) && !isBeingDragged) {
            ensureHelper(child)
            positionHelper.snapIfNeeded()
        }
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: CoverAppbar,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int,
    ) {
        if (dy > 0) {
            ensureHelper(child)
            consumed[1] = positionHelper.scrollBy(dy)
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: CoverAppbar,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray,
    ) {
        // 只处理方向向下且Appbar并未被拖住的情况
        if (dyUnconsumed < 0 && !isBeingDragged) {
            ensureHelper(child)
            consumed[1] = positionHelper.scrollBy(dyUnconsumed)
        }
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: CoverAppbar,
        ev: MotionEvent,
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
            println("[onTouch]: $y $yDiff $lastMotionY $touchSlop")
            if (yDiff > touchSlop) {
                lastMotionY = y
                return true
            }
        }

        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            activePointerId = INVALID_POINTER
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            isBeingDragged = parent.isPointInChildBounds(child, x, y) // && !ignoreTouchEvent(ev)
            if (isBeingDragged) {
                lastMotionY = y
                lastMotionX = x
                activePointerId = ev.getPointerId(0)
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()

                parent.requestDisallowInterceptTouchEvent(true)

                // There is an animation in progress. Stop it and catch the view.
                ensureHelper(child)
                positionHelper.cancelAnimation()
            }
        }
        velocityTracker?.addMovement(ev)
        return false
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout,
        child: CoverAppbar,
        ev: MotionEvent,
    ): Boolean {
        var consumeUp = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = ev.findPointerIndex(activePointerId)
                if (activePointerIndex == -1) return false

                val x = ev.getY(activePointerIndex).toInt()
                val y = ev.getY(activePointerIndex).toInt()
                val dx = lastMotionX - x
                val dy = lastMotionY - y
                lastMotionY = y
                lastMotionX = x

                // We're being dragged so scroll the ABL
                println("[onTouchEvent]: $y $dy $lastMotionY $touchSlop")
//                if (abs(dx) > abs(dy) && abs(dx) >= touchSlop) {
//                    parent.requestDisallowInterceptTouchEvent(false)
//                    return false
//                }

                ensureHelper(child)
                positionHelper.scrollBy(dy)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val newIndex = if (ev.actionIndex == 0) 1 else 0
                activePointerId = ev.getPointerId(newIndex)
                lastMotionY = (ev.getY(newIndex) + 0.5f).toInt()
            }

            MotionEvent.ACTION_UP -> {
                isBeingDragged = false
                activePointerId = INVALID_POINTER
                velocityTracker?.let {
                    consumeUp = true
                    it.addMovement(ev)
                    it.computeCurrentVelocity(1000)
                    val velocityY = it.getYVelocity(activePointerId)
                    ensureHelper(child)
                    positionHelper.fling(velocityY)

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
}