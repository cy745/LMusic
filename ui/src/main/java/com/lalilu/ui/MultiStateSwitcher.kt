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

@SuppressLint("CustomViewStyleable")
class MultiStateSwitcher @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : StateSwitcher(context, attrs, defStyleAttr) {
    val stateChangeListeners = ArrayList<OnStateChangeListener>()

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
            override fun onDown(e: MotionEvent?): Boolean {
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
//                clickListeners.forEach {
//                    it.onClick(checkClickPart(e), e.action)
//                }
                snapAnimateOffsetTo(e.x - singleWidth / 2f)

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
//                scrollListeners.forEach {
//                    it.onScroll((-moveEvent.y).coerceAtLeast(0f))
//                }
                parent.requestDisallowInterceptTouchEvent(true)
                return super.onScroll(downEvent, moveEvent, distanceX, distanceY)
            }
        })

    fun updateOffsetByDelta(delta: Float) {
        updateOffset(thumbOffset + delta, true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (moved && !canceled) {
                    snapAnimateOffsetTo(thumbOffset)
//                    seekToListeners.forEach { it.onSeekTo(nowValue) }
                }
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

    fun snapAnimateOffsetTo(value: Float) {
        var snapTo = -1
        var min = Float.MAX_VALUE
        mStateAnchor.forEachIndexed { index, anchor ->
            val offset = abs(value - anchor)
            if (offset < min) {
                min = offset
                snapTo = index
            }
        }

        if (snapTo in mStateAnchor.indices) {
            animateOffsetTo(mStateAnchor[snapTo])
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

    init {
        thumbOffset = 300f
        mStateText = listOf("靠左", "居中", "靠右")
    }
}