package com.lalilu.lmusic.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 重写onTouchEvent，修改其计算dy的逻辑，解决RecyclerView嵌入Compose结合NestedScroll时，
 * RecyclerView内MotionEvent的getY获取到的值出现异常波动的问题
 */
class ComposeNestedScrollRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {
    private val TAG = "CustomRecyclerView"

    private val mInterceptingOnItemTouchListener = getField("mInterceptingOnItemTouchListener")
    private val mReusableIntPair by lazy { getField("mReusableIntPair").get(this) as IntArray }
    private val mScrollOffset by lazy { getField("mScrollOffset").get(this) as IntArray }
    private val mNestedOffsets by lazy { getField("mNestedOffsets").get(this) as IntArray }
    private val mVelocityTracker = getField("mVelocityTracker")
    private val mScrollPointerId = getField("mScrollPointerId")
    private val mInitialTouchX = getField("mInitialTouchX")
    private val mLastTouchX = getField("mLastTouchX")
    private val mLastTouchY = getField("mLastTouchY")
    private val mGapWorker = getField("mGapWorker")

    private val cancelScroll = getMethod("cancelScroll")
    private val setScrollStateMethod = getMethod("setScrollState", Int::class.java)
    private val scrollByInternalMethod = getMethod(
        "scrollByInternal",
        Int::class.java,
        Int::class.java,
        MotionEvent::class.java,
        Int::class.java
    )
    private val releaseHorizontalGlowMethod = getMethod(
        "releaseHorizontalGlow", Int::class.java, Float::class.java
    )
    private val releaseVerticalGlowMethod = getMethod(
        "releaseVerticalGlow", Int::class.java, Float::class.java
    )
    private val dispatchToOnItemTouchListenersMethod = getMethod(
        "dispatchToOnItemTouchListeners", MotionEvent::class.java
    )
    private val findInterceptingOnItemTouchListenerMethod = getMethod(
        "findInterceptingOnItemTouchListener", MotionEvent::class.java
    )

    private val gapWorkerClass by lazy {
        Class.forName("androidx.recyclerview.widget.GapWorker")
    }
    private val postFromTraversalMethod by lazy {
        gapWorkerClass.getDeclaredMethod(
            "postFromTraversal",
            RecyclerView::class.java,
            Int::class.java,
            Int::class.java
        ).also { it.isAccessible = true }
    }

    private var xLastTouchY = 0
    private var xInitialTouchY = 0
    private var xTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        val action = e.actionMasked

        if (action == MotionEvent.ACTION_DOWN) {
            xLastTouchY = (e.rawY + 0.5f).toInt()
            xInitialTouchY = xLastTouchY
        }

        if (action == MotionEvent.ACTION_MOVE) {
            mInterceptingOnItemTouchListener.set(this, null)
            if (findInterceptingOnItemTouchListenerMethod.invoke(this, e) as Boolean) {
                cancelScroll.invoke(this)
                return true
            }

            val pointerId = mScrollPointerId.getInt(this)
            val index = e.findPointerIndex(pointerId)
            if (index < 0) {
                Log.e(
                    TAG, "Error processing scroll; pointer index for id "
                            + pointerId + " not found. Did any MotionEvents get skipped?"
                )
                return false
            }

            val canScrollHorizontally = layoutManager?.canScrollHorizontally() ?: false
            val canScrollVertically = layoutManager?.canScrollVertically() ?: false

            val x = (e.getX(index) + 0.5f).toInt()
            val y = (e.getY(index) + 0.5f).toInt()
            val rawY = (e.rawY + 0.5f).toInt()
            if (scrollState != SCROLL_STATE_DRAGGING) {
                val dx = x - mInitialTouchX.getInt(this)
                val dy = rawY - xInitialTouchY

                var startScroll = false
                if (canScrollHorizontally && abs(dx) > xTouchSlop) {
                    mLastTouchX.setInt(this, x)
                    startScroll = true
                }
                if (canScrollVertically && abs(dy) > xTouchSlop) {
                    mLastTouchY.setInt(this, y)
                    xLastTouchY = rawY
                    startScroll = true
                }
                if (startScroll) {
                    setScrollStateMethod.invoke(this, SCROLL_STATE_DRAGGING)
                }
            }
            return scrollState == SCROLL_STATE_DRAGGING
        }

        return super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val action = e.actionMasked
        val canScrollHorizontally = layoutManager?.canScrollHorizontally() ?: false
        val canScrollVertically = layoutManager?.canScrollVertically() ?: false

        val motionEvent = MotionEvent.obtain(e)
        motionEvent.setLocation(e.rawX, e.rawY)
        // VelocityTracker计算时使用的是MotionEvent的getX, getY方法
        // 所以这里需要将MotionEvent的坐标设置为屏幕(rawX,rawY)的坐标
        // 避免波动的(getX,getY)值被用于计算速度
        motionEvent.offsetLocation(mNestedOffsets[0].toFloat(), mNestedOffsets[1].toFloat())

        if (action == MotionEvent.ACTION_DOWN) {
            xLastTouchY = (e.rawY + 0.5f).toInt()
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (dispatchToOnItemTouchListenersMethod.invoke(this, e) as Boolean) {
                cancelScroll.invoke(this)
                return true
            }

            val pointerId = mScrollPointerId.getInt(this)
            val index = e.findPointerIndex(pointerId)
            if (index < 0) {
                Log.e(
                    TAG, "Error processing scroll; pointer index for id "
                            + pointerId + " not found. Did any MotionEvents get skipped?"
                )
                return false
            }

            val x = (e.getX(index) + 0.5f).toInt()
            val y = (e.getY(index) + 0.5f).toInt()
            val rawY = (e.rawY + 0.5f).toInt()

            var dx = mLastTouchX.getInt(this) - x
            var dy = xLastTouchY - rawY

            if (scrollState != SCROLL_STATE_DRAGGING) {
                var startScroll = false
                if (canScrollHorizontally) {
                    dx = if (dx > 0) max(0, dx - xTouchSlop) else min(0, dx + xTouchSlop)
                    if (dx != 0) startScroll = true
                }
                if (canScrollVertically) {
                    dy = if (dy > 0) max(0, dy - xTouchSlop) else min(0, dy + xTouchSlop)
                    if (dy != 0) startScroll = true
                }
                if (startScroll) {
                    setScrollStateMethod.invoke(this, SCROLL_STATE_DRAGGING)
                }
            }

            if (scrollState == SCROLL_STATE_DRAGGING) {
                mReusableIntPair[0] = 0
                mReusableIntPair[1] = 0
                dx -= releaseHorizontalGlowMethod.invoke(this, dx, e.y) as Int
                dy -= releaseVerticalGlowMethod.invoke(this, dy, e.x) as Int
                if (dispatchNestedPreScroll(
                        if (canScrollHorizontally) dx else 0,
                        if (canScrollVertically) dy else 0,
                        mReusableIntPair, mScrollOffset, ViewCompat.TYPE_TOUCH
                    )
                ) {
                    dx -= mReusableIntPair[0]
                    dy -= mReusableIntPair[1]
                    // Updated the nested offsets
                    mNestedOffsets[0] += mScrollOffset[0]
                    mNestedOffsets[1] += mScrollOffset[1]
                    // Scroll has initiated, prevent parents from intercepting
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                mLastTouchX.setInt(this, x - mScrollOffset[0])
                mLastTouchY.setInt(this, y - mScrollOffset[1])
                xLastTouchY = rawY - mScrollOffset[1]

                if (scrollByInternalMethod.invoke(
                        this,
                        if (canScrollHorizontally) dx else 0,
                        if (canScrollVertically) dy else 0,
                        e, ViewCompat.TYPE_TOUCH
                    ) as Boolean
                ) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                val mGapWorker = mGapWorker.get(this)
                if (mGapWorker != null && (dx != 0 || dy != 0)) {
                    postFromTraversalMethod.invoke(mGapWorker, this, dx, dy)
                }
            }

            val velocityTracker = mVelocityTracker.get(this) as? VelocityTracker
            velocityTracker?.addMovement(motionEvent)
            motionEvent.recycle()
            return true
        }

        return super.onTouchEvent(e)
    }

    private fun getMethod(
        name: String,
        vararg parameterTypes: Class<*>
    ): Method = RecyclerView::class.java
        .getDeclaredMethod(name, *parameterTypes)
        .also { it.isAccessible = true }

    private fun getField(name: String): Field = RecyclerView::class.java
        .getDeclaredField(name)
        .also { it.isAccessible = true }
}