package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.compose.LayoutWrapper
import com.lalilu.lmusic.utils.AccumulatedValue
import java.lang.ref.WeakReference
import kotlin.math.abs

open class AppbarBehavior(
    context: Context?, attrs: AttributeSet?,
) : CoordinatorLayout.Behavior<CoverAppbar>(context, attrs) {

    constructor(appbar: CoverAppbar) : this(appbar.context, null) {
        ensureHelper(appbar)
    }

    @ViewCompat.NestedScrollType
    private var lastStartedType = 0
    private var isScrolling = false
    private var touchSlop = -1
    private val gestureDetector = GestureDetector(context, MyGestureListener())
    private var parentRef: WeakReference<CoordinatorLayout> = WeakReference(null)
    private var childRef: WeakReference<CoverAppbar> = WeakReference(null)

    lateinit var positionHelper: AppbarStateHelper

    open fun requestDisallowIntercept(value: Boolean) {
        LayoutWrapper.enableUserScroll.value = !value
    }

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
        if ((lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) && !isScrolling) {
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
        if (dyUnconsumed < 0 && !isScrolling) {
            ensureHelper(child)
            consumed[1] = positionHelper.scrollBy(dyUnconsumed)
        }
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: CoverAppbar,
        ev: MotionEvent
    ): Boolean {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(parent.context).scaledTouchSlop
        }

        parentRef = WeakReference(parent)
        childRef = WeakReference(child)
        return parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout,
        child: CoverAppbar,
        ev: MotionEvent
    ): Boolean {
        gestureDetector.onTouchEvent(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                isScrolling = false
                // 当手指抬起，允许父级拦截触摸事件
                requestDisallowIntercept(false)
                ensureHelper(child)
                positionHelper.fling(0f)
            }
        }

        return parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        private var accumulatedValue = AccumulatedValue()
        private var downX = 0f
        private var downY = 0f

        override fun onDown(e: MotionEvent): Boolean {
            downX = e.x
            downY = e.y

            childRef.get()?.let {
                ensureHelper(it)
                positionHelper.cancelAnimation()
            }

            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!isScrolling) {
                val xDiff = abs(downX - e2.x)
                val yDiff = abs(downY - e2.y)

                if (xDiff > touchSlop || yDiff > touchSlop) {
                    isScrolling = true

                    // 当纵向滑动大于横向运动时请求父级不要拦截后续事件
                    requestDisallowIntercept(yDiff > xDiff)
                }
            } else {
                childRef.get()?.let {
                    ensureHelper(it)
                    positionHelper.scrollBy(accumulatedValue.accumulate(distanceY))
                }
            }
            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            childRef.get()?.let {
                ensureHelper(it)
                positionHelper.fling(velocityY)
            }
            return true
        }
    }
}