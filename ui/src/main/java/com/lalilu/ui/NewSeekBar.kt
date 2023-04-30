package com.lalilu.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.IntDef
import androidx.core.view.GestureDetectorCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.common.SystemUiUtil
import kotlin.math.abs
import kotlin.math.roundToInt

const val CLICK_PART_UNSPECIFIED = 0
const val CLICK_PART_LEFT = 1
const val CLICK_PART_MIDDLE = 2
const val CLICK_PART_RIGHT = 3

@IntDef(
    CLICK_PART_UNSPECIFIED,
    CLICK_PART_LEFT,
    CLICK_PART_MIDDLE,
    CLICK_PART_RIGHT
)
@Retention(AnnotationRetention.SOURCE)
annotation class ClickPart

const val THRESHOLD_STATE_UNREACHED = 0
const val THRESHOLD_STATE_REACHED = 1
const val THRESHOLD_STATE_RETURN = 2

@IntDef(
    THRESHOLD_STATE_REACHED,
    THRESHOLD_STATE_UNREACHED,
    THRESHOLD_STATE_RETURN
)
@Retention(AnnotationRetention.SOURCE)
annotation class ThresholdState

fun interface OnSeekBarScrollListener {
    fun onScroll(scrollValue: Float)
}

fun interface OnSeekBarCancelListener {
    fun onCancel()
}

fun interface OnSeekBarSeekToListener {
    fun onSeekTo(value: Float)
}

fun interface OnTapLeaveListener {
    fun onLeave()
}

interface OnSeekBarClickListener {
    fun onClick(
        @ClickPart clickPart: Int = CLICK_PART_UNSPECIFIED,
        action: Int
    )

    fun onLongClick(
        @ClickPart clickPart: Int = CLICK_PART_UNSPECIFIED,
        action: Int
    )

    fun onDoubleClick(
        @ClickPart clickPart: Int = CLICK_PART_UNSPECIFIED,
        action: Int
    )
}

abstract class OnSeekBarScrollToThresholdListener(
    private val threshold: () -> Number
) : OnSeekBarScrollListener {
    abstract fun onScrollToThreshold()
    open fun onScrollRecover() {}

    @ThresholdState
    var state: Int = THRESHOLD_STATE_UNREACHED
        set(value) {
            if (field == value) return
            when (value) {
                THRESHOLD_STATE_REACHED -> onScrollToThreshold()
                THRESHOLD_STATE_RETURN -> onScrollRecover()
            }
            field = value
        }

    override fun onScroll(scrollValue: Float) {
        state = if (scrollValue >= threshold().toFloat()) {
            THRESHOLD_STATE_REACHED
        } else {
            if (state == THRESHOLD_STATE_REACHED) THRESHOLD_STATE_RETURN
            else THRESHOLD_STATE_UNREACHED
        }
    }
}

class NewSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : NewProgressBar(context, attrs) {
    var cancelThreshold = 100f

    val scrollListeners = HashSet<OnSeekBarScrollListener>()
    val clickListeners = HashSet<OnSeekBarClickListener>()
    val cancelListeners = HashSet<OnSeekBarCancelListener>()
    val seekToListeners = HashSet<OnSeekBarSeekToListener>()
    val onTapLeaveListeners = HashSet<OnTapLeaveListener>()
    var valueToText: ((Float) -> String)? = null

    private var moved = false
    private var canceled = true
    private var touching = false

    var startValue: Float = nowValue
    var dataValue: Float = nowValue
    var sensitivity: Float = 1.3f

    private val cancelScrollListener =
        object : OnSeekBarScrollToThresholdListener(this::cancelThreshold) {
            override fun onScrollToThreshold() {
                animateValueTo(dataValue)
                cancelListeners.forEach { it.onCancel() }
                canceled = true
            }

            override fun onScrollRecover() {
                canceled = false
            }
        }


    private val mProgressAnimation: SpringAnimation by lazy {
        SpringAnimation(this, ProgressFloatProperty(), nowValue).apply {
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

    private val mAlphaAnimation: SpringAnimation by lazy {
        SpringAnimation(this, AlphaFloatProperty(), 100f).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    override fun valueToText(value: Float): String {
        return valueToText?.invoke(value) ?: TimeUtils.millis2String(value.toLong(), "mm:ss")
    }

    override fun isDarkModeNow(): Boolean {
        return SystemUiUtil.isDarkMode(context)
    }

    /**
     * 判断触摸事件所点击的部分位置
     */
    fun checkClickPart(e: MotionEvent): Int {
        return when (e.x.toInt()) {
            in 0..(width * 1 / 3) -> CLICK_PART_LEFT
            in (width * 1 / 3)..(width * 2 / 3) -> CLICK_PART_MIDDLE
            in (width * 2 / 3)..width -> CLICK_PART_RIGHT
            else -> CLICK_PART_UNSPECIFIED
        }
    }

    private val gestureDetector = GestureDetectorCompat(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                touching = true
                canceled = false
                moved = false
                startValue = nowValue
                dataValue = nowValue
                animateScaleTo(SizeUtils.dp2px(3f).toFloat())
                animateOutSideAlphaTo(255f)
                animateAlphaTo(100f)
                return super.onDown(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                clickListeners.forEach {
                    it.onClick(checkClickPart(e), e.action)
                }
                performClick()
                return super.onSingleTapConfirmed(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                clickListeners.forEach {
                    it.onDoubleClick(checkClickPart(e), e.action)
                }
                return super.onDoubleTap(e)
            }

            override fun onLongPress(e: MotionEvent) {
                clickListeners.forEach {
                    it.onLongClick(checkClickPart(e), e.action)
                }
                animateValueTo(startValue)
                canceled = true
            }

            override fun onScroll(
                downEvent: MotionEvent,
                moveEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                moved = true
                updateValueByDelta(-distanceX)
                scrollListeners.forEach {
                    it.onScroll((-moveEvent.y).coerceAtLeast(0f))
                }
                parent.requestDisallowInterceptTouchEvent(true)
                return super.onScroll(downEvent, moveEvent, distanceX, distanceY)
            }
        })

    fun updateValueByDelta(delta: Float) {
        if (touching && !canceled) {
            mProgressAnimation.cancel()
            val value = nowValue + delta / width * (maxValue - minValue) * sensitivity
            updateProgress(value, true)
        }
    }

    fun updateValue(value: Float) {
        if (value !in minValue..maxValue) return

        if (!touching || canceled) {
            animateValueTo(value)
        }
        dataValue = value
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                onTapLeaveListeners.forEach(OnTapLeaveListener::onLeave)
                if (moved && !canceled && abs(nowValue - startValue) > minIncrement) {
                    seekToListeners.forEach { it.onSeekTo(nowValue) }
                }
                animateScaleTo(0f)
                animateOutSideAlphaTo(0f)
                touching = false
                canceled = false
                moved = false
                scrollListeners.forEach {
                    if (it is OnSeekBarScrollToThresholdListener) {
                        it.state = THRESHOLD_STATE_UNREACHED
                    }
                }
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    fun animateOutSideAlphaTo(value: Float) {
        mOutSideAlphaAnimation.cancel()
        mOutSideAlphaAnimation.animateToFinalPosition(value)
    }

    fun animateScaleTo(value: Float) {
        mPaddingAnimation.cancel()
        mPaddingAnimation.animateToFinalPosition(value)
    }

    fun animateValueTo(value: Float) {
        mProgressAnimation.cancel()
        mProgressAnimation.animateToFinalPosition(value)
    }

    fun animateAlphaTo(value: Float) {
        mAlphaAnimation.cancel()
        mAlphaAnimation.animateToFinalPosition(value)
    }

    class ProgressFloatProperty :
        FloatPropertyCompat<NewProgressBar>("progress") {
        override fun getValue(obj: NewProgressBar): Float = obj.nowValue
        override fun setValue(obj: NewProgressBar, value: Float) {
            obj.updateProgress(value, false)
        }
    }

    class PaddingFloatProperty :
        FloatPropertyCompat<NewProgressBar>("padding") {
        override fun getValue(obj: NewProgressBar): Float = obj.padding
        override fun setValue(obj: NewProgressBar, value: Float) {
            obj.padding = value
            obj.outSideAlpha = (value * 50f).toInt()
        }
    }

    class OutSideAlphaFloatProperty :
        FloatPropertyCompat<NewProgressBar>("outside_alpha") {
        override fun getValue(obj: NewProgressBar): Float = obj.outSideAlpha.toFloat()
        override fun setValue(obj: NewProgressBar, value: Float) {
            obj.outSideAlpha = value.roundToInt()
        }
    }

    class AlphaFloatProperty : FloatPropertyCompat<NewProgressBar>("alpha") {
        override fun getValue(obj: NewProgressBar): Float = obj.alpha * 100f
        override fun setValue(obj: NewProgressBar, value: Float) {
            obj.alpha = value / 100f
        }
    }

    init {
        scrollListeners.add(cancelScrollListener)
    }
}