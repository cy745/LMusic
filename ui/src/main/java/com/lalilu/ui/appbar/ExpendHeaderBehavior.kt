package com.lalilu.ui.appbar

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.OverScroller
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
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

const val STATE_NORMAL = 1
const val STATE_MIDDLE = 2
const val STATE_COLLAPSED = 3
const val STATE_EXPENDED = 4
const val STATE_FULLY_EXPENDED = 5

abstract class ExpendHeaderBehavior<V : AppBarLayout>(
    private val mContext: Context?, attrs: AttributeSet?
) : ViewOffsetExpendBehavior<V>(mContext, attrs), AntiMisTouchEvent {

    /**
     * [ExpendHeaderBehavior]
     *  可能的几种状态
     */
    @IntDef(
        STATE_NORMAL,
        STATE_MIDDLE,
        STATE_COLLAPSED,
        STATE_EXPENDED,
        STATE_FULLY_EXPENDED
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

    /**
     * 监听状态变化的基础接口
     */
    interface OnStateChangeListener {
        fun onStateChange(@State lastState: Int, @State nowState: Int)
    }

    abstract class OnScrollToThresholdListener : OnStateChangeListener {
        abstract fun onScrollToThreshold()
        override fun onStateChange(@State lastState: Int, @State nowState: Int) {
            if (lastState != STATE_MIDDLE && nowState == STATE_MIDDLE) {
                onScrollToThreshold()
            }
        }
    }

    abstract class OnScrollToStateListener(
        @State private val startState: Int,
        @State private val targetState: Int
    ) : OnStateChangeListener {
        abstract fun onScrollToStateListener()
        override fun onStateChange(@State lastState: Int, @State nowState: Int) {
            if (lastState == startState && nowState == targetState && nowState != lastState) {
                onScrollToStateListener()
            }
        }
    }

    open val interceptSize: Int = 100

    private var mScrollAnimation: SpringAnimation? = null
    private var scroller: OverScroller? = null
    private var velocityTracker: VelocityTracker? = null
    private var lastInsets: WindowInsetsCompat? = null
    private var stateChangeListeners: MutableList<OnStateChangeListener> = ArrayList()

    private var isBeingDragged = false
    private var activePointerId = INVALID_POINTER
    private var lastMotionY = 0
    private var touchSlop = -1

    @State
    private var tempState: Int = STATE_EXPENDED

    @State
    private var lastState: Int = STATE_EXPENDED

    @State
    val nowState: Int
        get() {
            val min = getCollapsedOffset()
            val dragOffset = (getMaxDragOffset() * getMaxDragThreshold()).toInt()
            val max = getFullyExpendOffset()

            return when (topAndBottomOffset) {
                in min until (min + dragOffset) -> STATE_COLLAPSED
                in (min + dragOffset) until -dragOffset -> STATE_NORMAL
                in -dragOffset until dragOffset -> STATE_EXPENDED
                in dragOffset until (max - dragOffset) -> STATE_MIDDLE
                else -> STATE_FULLY_EXPENDED
            }
        }

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

    open fun getMaxDragOffset(): Float {
        return 200f
    }

    @FloatRange(from = 0.0, to = 1.0)
    open fun getMaxDragThreshold(): Float {
        return 0.6f
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
        mContext?.let { context ->
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(outMetrics)
            parent?.let { return outMetrics.heightPixels - parent.width }
        }
        return 0
    }

    fun addOnStateChangeListener(listener: OnStateChangeListener) {
        if (!stateChangeListeners.contains(listener)) {
            stateChangeListeners.add(listener)
        }
    }

    override fun setTopAndBottomOffset(offset: Int): Boolean {
        val result = super.setTopAndBottomOffset(offset)
        if (tempState != nowState) {
            lastState = tempState
            stateChangeListeners.forEach { it.onStateChange(lastState, nowState) }
            tempState = nowState
        }
        return result
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
                mScrollAnimation?.let {
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
                isBeingDragged = false
                activePointerId = INVALID_POINTER
                velocityTracker?.let {
                    consumeUp = true
                    it.addMovement(ev)
                    it.computeCurrentVelocity(1000)
                    val velocityY = it.getYVelocity(activePointerId)
                    fling(velocityY)
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

    /**
     * 在此获取 [lastInsets]
     * 用于获取状态栏高度等等用途
     */
    override fun onApplyWindowInsets(
        parent: CoordinatorLayout,
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
     * 根据当前状态 [nowState] 和前一个状态 [lastState] 判断应该贴合的目标边offset
     * 并调用 [animateOffsetTo] 贴合至指定的边
     *
     */
    fun snapToChildIfNeeded() {
        val offsetTo = when (nowState) {
            STATE_EXPENDED -> 0
            STATE_COLLAPSED -> getCollapsedOffset()
            STATE_FULLY_EXPENDED -> getFullyExpendOffset()
            STATE_NORMAL -> calculateSnapOffset(
                topAndBottomOffset, 0, getCollapsedOffset()
            )
            STATE_MIDDLE -> when (lastState) {
                STATE_FULLY_EXPENDED -> 0
                STATE_NORMAL,
                STATE_EXPENDED -> getFullyExpendOffset()
                else -> null
            }
            else -> null
        }
        offsetTo?.let { animateOffsetTo(it) }
    }

    /**
     * 用于计算与当前位置最近的边
     * @param value 当前位置
     * @param snapTo 目标边
     * @return 返回最近的边的位置
     */
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
        mScrollAnimation?.cancel()
    }

    fun animateOffsetTo(
        offset: Int
    ) {
        mScrollAnimation = mScrollAnimation
            ?: SpringAnimation(
                this, HeaderOffsetFloatProperty(),
                topAndBottomOffset.toFloat()
            ).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                spring.stiffness = SpringForce.STIFFNESS_LOW
            }
        mScrollAnimation?.cancel()
        mScrollAnimation?.animateToFinalPosition(offset.toFloat())
    }

    /**
     * 用于计算阻尼衰减后的位置
     * @param oldOffset 前一个位置
     * @param newOffset 当前位置
     * @return 阻尼衰减后的目标位置
     */
    open fun checkDampOffset(oldOffset: Int, newOffset: Int): Int {
        var nextPosition = newOffset
        if (newOffset > oldOffset) {
            val percent = 1f - oldOffset.toFloat() / getMaxDragOffset()
            if (percent in 0F..1F) nextPosition =
                (oldOffset + (newOffset - oldOffset) * percent).toInt()
        }
        return nextPosition
    }

    /**
     * 滚动指定的距离
     * @param dy 滚动的距离
     */
    fun scroll(
        dy: Int,
        minOffset: Int = getCollapsedOffset(),
        maxOffset: Int = getFullyExpendOffset()
    ): Int {
        return setHeaderTopBottomOffset(
            checkDampOffset(topAndBottomOffset, topAndBottomOffset - dy),
            minOffset,
            maxOffset
        )
    }

    /**
     * 松手后根据速度进行滑动
     * @param velocityY
     */
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
        when (nowState) {
            STATE_COLLAPSED,
            STATE_NORMAL,
            STATE_EXPENDED -> {
                animateOffsetTo(
                    calculateSnapOffset(scroller!!.finalY, 0, minOffset)
                )
            }
            STATE_MIDDLE,
            STATE_FULLY_EXPENDED -> {
                snapToChildIfNeeded()
            }
        }
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