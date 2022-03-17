package com.lalilu.lmusic.ui.seekbar

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
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.TextUtils


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

interface OnSeekBarClickListener {
    fun onClick(@ClickPart clickPart: Int = CLICK_PART_UNSPECIFIED)
    fun onLongClick(@ClickPart clickPart: Int = CLICK_PART_UNSPECIFIED)
    fun onDoubleClick(@ClickPart clickPart: Int = CLICK_PART_UNSPECIFIED)
}

interface OnSeekBarScrollListener {
    fun onScroll(dX: Float, scrollY: Float)
}

interface OnSeekBarCancelListener {
    fun onCancel()
}

abstract class OnSeekBarScrollToThresholdListener(
    var threshold: Float
) : OnSeekBarScrollListener {
    private var handle: Boolean = false

    abstract fun onScrollToThreshold()
    open fun onScrollRecover() {}
    override fun onScroll(dX: Float, scrollY: Float) {
        if (scrollY >= threshold && !handle) {
            onScrollToThreshold()
            handle = true
        }
        if (scrollY < threshold) {
            onScrollRecover()
            handle = false
        }
    }
}

class NewSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : NewProgressBar(context, attrs) {
    var cancelThreshold = 100f
        set(value) {
            field = value
            cancelScrollListener.threshold = value
        }

    var scaleAnimatorDuration = 100L
    var scaleAnimatorTo = 1.1f


    val scrollListeners = ArrayList<OnSeekBarScrollListener>()
    val clickListeners = ArrayList<OnSeekBarClickListener>()
    val cancelListeners = ArrayList<OnSeekBarCancelListener>()

    private var canceled = true
    private var touching = false
    private var previousLeft = -1
    private var previousRight = -1
    private var nextLeft = -1
    private var nextRight = -1

    var startValue: Float = 0f
    var dataValue: Float = 0f

    private val cancelScrollListener =
        object : OnSeekBarScrollToThresholdListener(cancelThreshold) {
            override fun onScrollToThreshold() {
                cancelListeners.forEach { it.onCancel() }
                animateProgressTo(dataValue)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        previousLeft = left
        previousRight = left + width * 2 / 5
        nextLeft = left + width * 3 / 5
        nextRight = left + width
    }

    /**
     * 判断触摸事件所点击的部分位置
     */
    fun checkClickPart(e: MotionEvent): Int {
        return when (e.x.toInt()) {
            in previousLeft..previousRight -> CLICK_PART_LEFT
            in previousRight..nextLeft -> CLICK_PART_MIDDLE
            in nextLeft..nextRight -> CLICK_PART_RIGHT
            else -> CLICK_PART_UNSPECIFIED
        }
    }

    private val gestureDetector = GestureDetectorCompat(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                startValue = nowValue
                animateScaleTo(scaleAnimatorTo)
                return super.onDown(e)
            }

            override fun onShowPress(e: MotionEvent?) {
                super.onShowPress(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                clickListeners.forEach {
                    it.onClick(checkClickPart(e))
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                clickListeners.forEach {
                    it.onDoubleClick(checkClickPart(e))
                }
                return super.onDoubleTapEvent(e)
            }

            override fun onLongPress(e: MotionEvent) {
                clickListeners.forEach {
                    it.onLongClick(checkClickPart(e))
                }
                super.onLongPress(e)
            }

            override fun onScroll(
                downEvent: MotionEvent,
                moveEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                scrollListeners.forEach {
                    it.onScroll(distanceX, (top - moveEvent.rawY).coerceAtLeast(0f))
                }
                return super.onScroll(downEvent, moveEvent, distanceX, distanceY)
            }
        })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun animateScaleTo(scaleValue: Float) {
        this.animate()
            .scaleY(scaleValue)
            .scaleX(scaleValue)
            .setDuration(scaleAnimatorDuration)
            .start()
    }

    fun animateProgressTo(progress: Float) {
        mProgressAnimation.cancel()
        mProgressAnimation.animateToFinalPosition(progress)
    }

    override fun valueToText(value: Float): String {
        return TextUtils.durationToString(value)
    }

    override fun isDarkModeNow(): Boolean {
        return StatusBarUtil.isDarkMode(context)
    }

    class ProgressFloatProperty :
        FloatPropertyCompat<NewProgressBar>("progress") {
        override fun getValue(obj: NewProgressBar): Float = obj.nowValue
        override fun setValue(obj: NewProgressBar, value: Float) {
            obj.nowValue = value
        }
    }

    init {
        scrollListeners.add(cancelScrollListener)
    }
}