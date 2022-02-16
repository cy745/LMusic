package com.lalilu.material.appbar

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.util.ObjectsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.roundToInt

const val INVALID_POINTER = -1

abstract class ExpendHeaderBehavior<V : AppBarLayout>(
    private val mContext: Context?, attrs: AttributeSet?
) : ViewOffsetExpendBehavior<V>(mContext, attrs), AntiMisTouchEvent {
    private val interceptSize: Int = 100

    private var mSpringAnimation: SpringAnimation? = null
    private var scroller: OverScroller? = null
    private var velocityTracker: VelocityTracker? = null
    private var lastInsets: WindowInsetsCompat? = null

    private var isBeingDragged = false
    private var activePointerId = INVALID_POINTER
    private var lastMotionY = 0
    private var touchSlop = -1
    private var lastFullyExpendedOffset = Int.MAX_VALUE

    protected var parentWeakReference: WeakReference<CoordinatorLayout>? = null
    protected var childWeakReference: WeakReference<V>? = null

    override fun isInPlaceToIntercept(rawY: Float): Boolean {
        childWeakReference?.get()?.let {
            return rawY >= (it.height - interceptSize) && rawY <= it.height
        }
        return false
    }

    override fun isTimeToIntercept(): Boolean {
        return topAndBottomOffset >= getFullyExpendOffset() - interceptSize
    }

    private fun getTopInset(): Int {
        return lastInsets?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0
    }

    open fun canDragView(view: V): Boolean {
        return false
    }

    open fun invalidOffset() {
        lastFullyExpendedOffset = Int.MAX_VALUE
    }

    open fun getCollapsedOffset(
        parent: View? = parentWeakReference?.get(),
        child: V? = childWeakReference?.get()
    ): Int {
        return -(child?.totalScrollRange ?: 0)
    }

    open fun getFullyExpendOffset(
        parent: View? = parentWeakReference?.get(),
        child: V? = childWeakReference?.get()
    ): Int {
        if (lastFullyExpendedOffset != Int.MAX_VALUE) {
            return lastFullyExpendedOffset
        }

        mContext?.let { context ->
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(outMetrics)
            parent?.let {
                lastFullyExpendedOffset = outMetrics.heightPixels - parent.width
                return lastFullyExpendedOffset
            }
        }
        return 0
    }

    open fun setHeaderTopBottomOffset(
        newOffset: Int,
        minOffset: Int = getCollapsedOffset(),
        maxOffset: Int = getFullyExpendOffset()
    ): Int {
        var consumed = 0
        val curOffset = topAndBottomOffset
        if (minOffset != 0 && curOffset in minOffset..maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            val offset = newOffset.coerceIn(minOffset, maxOffset)
            if (curOffset != offset) {
                setTopAndBottomOffset(offset)
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
                    if (it.isRunning) it.cancel()
                }
            }
        }
        velocityTracker?.addMovement(ev)
        return false
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout, child: V, ev: MotionEvent
    ): Boolean {
        if (checkTouchEvent(ev)) return true

        var consumeUp = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = ev.findPointerIndex(activePointerId)
                if (activePointerIndex == -1) return false
                val y = ev.getY(activePointerIndex).toInt()
                val dy = lastMotionY - y
                lastMotionY = y
                // We're being dragged so scroll the ABL
                scroll(dy)
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
                    fling(velocityY)
                    it.recycle()
                    velocityTracker = null
                }
                isBeingDragged = false
                activePointerId = INVALID_POINTER
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

    override fun onApplyWindowInsets(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        insets: WindowInsetsCompat
    ): WindowInsetsCompat {
        if (ViewCompat.getFitsSystemWindows(child) &&
            !ObjectsCompat.equals(lastInsets, insets)
        ) {
            lastInsets = insets
        }
        return insets
    }

    /**
     * 贴合至特定的边
     */
    fun snapToChildIfNeeded(parent: CoordinatorLayout, child: V) {
        val topInset: Int = getTopInset() + child.paddingTop
        val offset = topAndBottomOffset - topInset

        val newOffset: Int = calculateSnapOffset(
            offset, 0,
            getCollapsedOffset(parent, child),
            getFullyExpendOffset(parent, child)
        )
        animateOffsetTo(newOffset)
    }

    private fun calculateSnapOffset(value: Int, vararg snapTo: Int): Int {
        var min = Int.MAX_VALUE
        var result = value
        snapTo.forEach { i ->
            val temp = abs(value - i)
            if (temp < min) {
                min = temp
                result = i
            }
        }
        return result
    }

    fun cancelAnimation() {
        mSpringAnimation?.cancel()
    }

    fun animateOffsetTo(
        offset: Int
    ) {
        mSpringAnimation = mSpringAnimation
            ?: SpringAnimation(
                this, HeaderOffsetFloatProperty(),
                topAndBottomOffset.toFloat()
            ).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                spring.stiffness = SpringForce.STIFFNESS_LOW
            }
        mSpringAnimation?.cancel()
        mSpringAnimation?.animateToFinalPosition(offset.toFloat())
    }

    fun scroll(
        dy: Int,
        minOffset: Int = getCollapsedOffset(),
        maxOffset: Int = getFullyExpendOffset()
    ): Int {
        var nextPosition = topAndBottomOffset - dy
        if (dy < 0) {
            val percent = 1f - topAndBottomOffset / 200f
            if (percent in 0F..1F) nextPosition = topAndBottomOffset - (dy * percent).toInt()
        }

        return setHeaderTopBottomOffset(
            nextPosition,
            minOffset,
            maxOffset
        )
    }

    fun fling(
        velocityY: Float,
        minOffset: Int = getCollapsedOffset(),
        maxOffset: Int = getFullyExpendOffset(),
    ): Boolean {
        scroller = scroller ?: OverScroller(mContext)
        scroller!!.fling(
            0, topAndBottomOffset,            // startX / Y
            0, velocityY.roundToInt(),     // velocityX / Y
            0, 0,                       // minX / maxX
            minOffset, maxOffset                   // minY / maxY
        )
        val finalY = calculateSnapOffset(
            scroller!!.finalY, 0,
            minOffset, maxOffset
        )
        animateOffsetTo(finalY)
        return true
    }

    inner class HeaderOffsetFloatProperty :
        FloatPropertyCompat<ExpendHeaderBehavior<V>>("header_offset") {
        override fun getValue(obj: ExpendHeaderBehavior<V>): Float {
            return obj.topAndBottomOffset.toFloat()
        }

        override fun setValue(obj: ExpendHeaderBehavior<V>, value: Float) {
            obj.setHeaderTopBottomOffset(value.roundToInt())
        }
    }
}