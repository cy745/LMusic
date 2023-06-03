package com.lalilu.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.IntDef
import androidx.core.view.GestureDetectorCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.common.SystemUiUtil
import kotlin.math.abs

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

fun interface OnTapEventListener {
    fun onTapEvent()
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
    val onTapLeaveListeners = HashSet<OnTapEventListener>()
    val onTapEnterListeners = HashSet<OnTapEventListener>()
    var valueToText: ((Float) -> String)? = null
    private var switchToCallbacks = ArrayList<Pair<Drawable, () -> Unit>>()

    private var moved = false
    private var canceled = true
    private var touching = false
    private var switchMode = false

    private var startValue: Float = nowValue
    private var dataValue: Float = nowValue
    private var sensitivity: Float = 1.3f

    private var downX: Float = 0f
    private var downY: Float = 0f
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private val cancelScrollListener =
        object : OnSeekBarScrollToThresholdListener(this::cancelThreshold) {
            override fun onScrollToThreshold() {
                animateValueTo(dataValue)
                animateSwitchModeProgressTo(0f)
                cancelListeners.forEach { it.onCancel() }
                canceled = true
            }

            override fun onScrollRecover() {
                canceled = false
                if (switchMode) {
                    animateSwitchModeProgressTo(100f)
                }
            }
        }

    private val mProgressAnimation: SpringAnimation by lazy {
        springAnimationOf(
            setter = { updateProgress(it, false) },
            getter = { nowValue },
            finalPosition = nowValue
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private val mPaddingAnimation: SpringAnimation by lazy {
        springAnimationOf(
            setter = {
                padding = it
                outSideAlpha = (it * 50f).toInt()
            },
            getter = { padding },
            finalPosition = padding
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private val mOutSideAlphaAnimation: SpringAnimation by lazy {
        springAnimationOf(
            setter = { outSideAlpha = it.toInt() },
            getter = { outSideAlpha.toFloat() },
            finalPosition = outSideAlpha.toFloat()
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private val mAlphaAnimation: SpringAnimation by lazy {
        springAnimationOf(
            setter = { alpha = it / 100f },
            getter = { alpha * 100f },
            finalPosition = 100f
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
    }

    private val switchModeAnimation: SpringAnimation by lazy {
        springAnimationOf(
            setter = { switchModeProgress = it / 100f },
            getter = { switchModeProgress * 100f },
            finalPosition = 100f
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
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
                moved = false
                canceled = false
                switchMode = false
                startValue = nowValue
                dataValue = nowValue
                downX = e.x
                downY = e.y
                lastX = downX
                lastY = downY

                animateScaleTo(SizeUtils.dp2px(3f).toFloat())
                animateOutSideAlphaTo(255f)
                animateAlphaTo(100f)

                onTapEnterListeners.forEach(OnTapEventListener::onTapEvent)
                return super.onDown(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                clickListeners.forEach { it.onClick(checkClickPart(e), e.action) }
                performClick()
                return super.onSingleTapConfirmed(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                clickListeners.forEach { it.onDoubleClick(checkClickPart(e), e.action) }
                return super.onDoubleTap(e)
            }

            override fun onLongPress(e: MotionEvent) {
                clickListeners.forEach { it.onLongClick(checkClickPart(e), e.action) }
                animateValueTo(startValue)
                updateSwitchMoveX(e.x)
                animateSwitchModeProgressTo(100f)
                switchMode = true
            }
        })

    private fun updateValueByDelta(delta: Float) {
        if (touching && !canceled && !switchMode) {
            mProgressAnimation.cancel()
            val value = nowValue + delta / width * (maxValue - minValue) * sensitivity
            updateProgress(value, true)
        }
    }

    fun updateSwitchMoveX(moveX: Float) {
        switchMoveX = moveX
    }

    fun updateValue(value: Float) {
        if (value !in minValue..maxValue) return

        if (!touching || canceled) {
            animateValueTo(value)
        }
        dataValue = value
    }

    fun updateProgress(value: Float, fromUser: Boolean = false) {
        nowValue = value
    }

    fun setSwitchToCallback(vararg callbackPair: Pair<Drawable, () -> Unit>) {
        switchToCallbacks.clear()
        switchToCallbacks.addAll(callbackPair)
        thumbTabs.clear()
        thumbTabs.addAll(switchToCallbacks.map { it.first })
    }

    /**
     * GestureDetector 没有抬起相关的事件回调，
     * 在OnTouchView中自行处理抬起相关逻辑
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                onTapLeaveListeners.forEach(OnTapEventListener::onTapEvent)

                if (moved && !canceled) {
                    if (switchMode) {
                        val index = getIntervalIndex(
                            a = 0f,
                            b = width.toFloat(),
                            n = switchToCallbacks.size,
                            x = switchMoveX
                        )
                        switchToCallbacks.getOrNull(index)?.second?.invoke()
                    } else if (abs(nowValue - startValue) > minIncrement) {
                        seekToListeners.forEach { it.onSeekTo(nowValue) }
                    }
                }
                animateScaleTo(0f)
                animateOutSideAlphaTo(0f)
                animateSwitchModeProgressTo(0f)
                touching = false
                canceled = false
                switchMode = false
                moved = false

                scrollListeners.forEach {
                    if (it is OnSeekBarScrollToThresholdListener) {
                        it.state = THRESHOLD_STATE_UNREACHED
                    }
                }

                parent.requestDisallowInterceptTouchEvent(false)
            }


            // GestureDetector在OnLongPressed后不会再回调OnScrolled，所以自己处理ACTION_MOVE事件
            MotionEvent.ACTION_MOVE -> {
                if (!touching) return true

                val deltaX: Float = event.x - lastX
                val deltaY: Float = event.y - lastY

                moved = true
                if (switchMode) {
                    updateSwitchMoveX(event.x)
                } else {
                    updateValueByDelta(deltaX)
                }

                scrollListeners.forEach { it.onScroll((-event.y).coerceAtLeast(0f)) }
                parent.requestDisallowInterceptTouchEvent(true)

                lastX = event.x
                lastY = event.y
            }
        }
        return true
    }

    private fun getIntervalIndex(a: Float, b: Float, n: Int, x: Float): Int {
        if (x < a) return 0
        if (x > b) return n - 1

        val intervalSize = (b - a) / n              // 区间大小
        val index = ((x - a) / intervalSize).toInt() // 计算区间索引
        return if (index >= n) n - 1 else index
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

    fun animateSwitchModeProgressTo(value: Float) {
        switchModeAnimation.cancel()
        switchModeAnimation.animateToFinalPosition(value)
    }

    init {
        scrollListeners.add(cancelScrollListener)
    }
}