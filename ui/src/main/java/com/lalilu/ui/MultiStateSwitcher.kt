package com.lalilu.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.blankj.utilcode.util.SizeUtils
import kotlin.math.abs
import kotlin.math.roundToInt

interface OnOffsetChangeListener {
    fun onOffsetChange(offset: Float, fromUser: Boolean)
}

interface OnStateChangeListener {
    fun onStateChange(state: Int, fromUser: Boolean)
}

@Deprecated("取消了Preference构建Settings页的做法，替换为Compose实现更方便")
@SuppressLint("CustomViewStyleable")
class MultiStateSwitcher @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : StateSwitcher(context, attrs, defStyleAttr) {
    var state: Int = 0

    val offsetChangeListeners = HashSet<OnOffsetChangeListener>()
    val stateChangeListeners = HashSet<OnStateChangeListener>()

    val mStateAnchor: List<Float>
        get() {
            val single = availableWidth / mStateText.size
            return mStateText.mapIndexed { index, _ -> index * single }
        }

    private val mOffsetAnimation: SpringAnimation by lazy {
        SpringAnimation(this, OffsetFloatProperty(), thumbOffset).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private val mPaddingAnimation: SpringAnimation by lazy {
        SpringAnimation(this, PaddingFloatProperty(), padding).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private val mOutSideAlphaAnimation: SpringAnimation by lazy {
        SpringAnimation(this, OutSideAlphaFloatProperty(), outSideAlpha.toFloat()).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private var moved = false
    private var canceled = true
    private var touching = false

    var startOffset: Float = thumbOffset
    var dataOffset: Float = thumbOffset

    private val gestureDetector = GestureDetectorCompat(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                touching = true
                canceled = false
                moved = false
                startOffset = thumbOffset
                dataOffset = thumbOffset
                animateScaleTo(SizeUtils.dp2px(3f).toFloat())
                animateOutSideAlphaTo(255f)
                return super.onDown(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                snapAnimateOffsetTo(e.x - singleWidth / 2f, false)
                performClick()
                return super.onSingleTapConfirmed(e)
            }

            override fun onScroll(
                downEvent: MotionEvent,
                moveEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                moved = true
                updateOffsetByDelta(-distanceX)
                offsetChangeListeners.forEach {
                    it.onOffsetChange(thumbOffset, true)
                }
                parent.requestDisallowInterceptTouchEvent(true)
                return super.onScroll(downEvent, moveEvent, distanceX, distanceY)
            }
        })

    fun updateOffsetByDelta(delta: Float) {
        updateOffset(thumbOffset + delta, true)
    }

    override fun updateOffset(offset: Float, fromUser: Boolean) {
        super.updateOffset(offset, fromUser)
        closeToStateByOffset(thumbOffset)?.let {
            if (state == it) return
            state = it
            stateChangeListeners.forEach { listener ->
                listener.onStateChange(state, fromUser)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                snapAnimateOffsetTo(thumbOffset, false)
                animateScaleTo(0f)
                animateOutSideAlphaTo(0f)
                touching = false
                canceled = false
                moved = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    /**
     * 计算[offset]最接近的State
     */
    private fun closeToStateByOffset(offset: Float): Int? {
        var snapTo = -1
        var min = Float.MAX_VALUE
        mStateAnchor.forEachIndexed { index, anchor ->
            val value = abs(offset - anchor)
            if (value < min) {
                min = value
                snapTo = index
            }
        }
        if (snapTo !in mStateText.indices) {
            return null
        }
        return snapTo
    }

    fun snapAnimateOffsetTo(offset: Float, force: Boolean) {
        val snapTo = closeToStateByOffset(offset) ?: return
        snapAnimateStateTo(snapTo, force)
    }

    fun snapAnimateStateTo(state: Int, force: Boolean) {
        if (state !in mStateText.indices) return
        val offset = mStateAnchor[state]

        if (force) {
            updateOffset(offset, false)
        } else {
            animateOffsetTo(offset)
        }
    }

    fun animateOffsetTo(value: Float) {
        mOffsetAnimation.cancel()
        mOffsetAnimation.animateToFinalPosition(value)
    }

    fun animateOutSideAlphaTo(value: Float) {
        mOutSideAlphaAnimation.cancel()
        mOutSideAlphaAnimation.animateToFinalPosition(value)
    }

    fun animateScaleTo(value: Float) {
        mPaddingAnimation.cancel()
        mPaddingAnimation.animateToFinalPosition(value)
    }

    class OffsetFloatProperty :
        FloatPropertyCompat<StateSwitcher>("offset") {
        override fun getValue(obj: StateSwitcher): Float = obj.thumbOffset
        override fun setValue(obj: StateSwitcher, value: Float) {
            obj.updateOffset(value, false)
        }
    }

    class PaddingFloatProperty :
        FloatPropertyCompat<StateSwitcher>("padding") {
        override fun getValue(obj: StateSwitcher): Float = obj.padding
        override fun setValue(obj: StateSwitcher, value: Float) {
            obj.padding = value
            obj.outSideAlpha = (value * 50f).toInt()
        }
    }

    class OutSideAlphaFloatProperty :
        FloatPropertyCompat<StateSwitcher>("outside_alpha") {
        override fun getValue(obj: StateSwitcher): Float = obj.outSideAlpha.toFloat()
        override fun setValue(obj: StateSwitcher, value: Float) {
            obj.outSideAlpha = value.roundToInt()
        }
    }
}