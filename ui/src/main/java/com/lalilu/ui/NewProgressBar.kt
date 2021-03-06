package com.lalilu.ui

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import com.blankj.utilcode.util.SizeUtils

const val CURRENT_STATE_UNSPECIFIED = 0
const val CURRENT_STATE_MIN = 1
const val CURRENT_STATE_MIDDLE = 2
const val CURRENT_STATE_MAX = 3

@IntDef(
    CURRENT_STATE_UNSPECIFIED,
    CURRENT_STATE_MIN,
    CURRENT_STATE_MIDDLE,
    CURRENT_STATE_MAX
)
@Retention(AnnotationRetention.SOURCE)
annotation class CurrentState

interface OnProgressChangeListener {
    fun onProgressChange(value: Float, fromUser: Boolean)
}

interface OnProgressToListener {
    fun onProgressToMax(value: Float, fromUser: Boolean) {}
    fun onProgressToMin(value: Float, fromUser: Boolean) {}
    fun onProgressToMiddle(value: Float, fromUser: Boolean) {}
}

open class NewProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    inner class OnProgressBarProgressToListener : OnProgressChangeListener {
        @CurrentState
        private var currentState = 0

        private fun updateCurrentState(@CurrentState state: Int, fromUser: Boolean) {
            if (currentState == state) return
            when (state) {
                CURRENT_STATE_MIN -> progressToListener.forEach {
                    it.onProgressToMin(nowValue, fromUser)
                }
                CURRENT_STATE_MAX -> progressToListener.forEach {
                    it.onProgressToMax(nowValue, fromUser)
                }
                CURRENT_STATE_MIDDLE -> progressToListener.forEach {
                    it.onProgressToMiddle(nowValue, fromUser)
                }
            }
            currentState = state
        }

        override fun onProgressChange(value: Float, fromUser: Boolean) {
            updateCurrentState(
                when {
                    value <= minValue -> CURRENT_STATE_MIN
                    value >= maxValue -> CURRENT_STATE_MAX
                    value > minValue && value < maxValue -> CURRENT_STATE_MIDDLE
                    else -> CURRENT_STATE_UNSPECIFIED
                }, fromUser
            )
        }
    }

    fun updateProgress(value: Float, fromUser: Boolean = false) {
        nowValue = value
        progressChangeListener.forEach { it.onProgressChange(nowValue, fromUser) }
    }

    val progressChangeListener = ArrayList<OnProgressChangeListener>()
    val progressToListener = ArrayList<OnProgressToListener>()

    var bgColor = Color.argb(50, 100, 100, 100)
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  ????????????
     */
    var radius: Float = 30f
        set(value) {
            field = value
            updatePath()
            invalidate()
        }


    var padding: Float = 0f
        set(value) {
            field = value
            updatePath()
            invalidate()
        }

    /**
     *  ???????????????
     */
    var minValue: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  ???????????????
     */
    var maxValue: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  ???????????????
     */
    var nowValue: Float = 0f
        set(value) {
            field = value.coerceIn(minValue, maxValue)
            invalidate()
        }

    var minIncrement: Float = 0f

    /**
     * ?????????????????????
     */
    var nowTextDarkModeColor: Int? = null
    var nowTextColor: Int = Color.WHITE
        get() {
            if (isDarkModeNow()) return nowTextDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            nowTextPaint.color = value
            invalidate()
        }

    /**
     * ?????????????????????
     */
    var maxTextDarkModeColor: Int? = null
    var maxTextColor: Int = Color.WHITE
        get() {
            if (isDarkModeNow()) return maxTextDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            maxTextPaint.color = value
            invalidate()
        }

    /**
     * ??????????????????
     */
    var thumbDarkModeColor: Int? = null
    var thumbColor: Int = Color.DKGRAY
        get() {
            if (isDarkModeNow()) return thumbDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            thumbPaint.color = value
            invalidate()
        }

    /**
     * ?????????????????????
     * ????????????????????????????????????
     * ??? [outSideAlpha] ??????????????????
     */
    var outSideDarkModeColor: Int? = Color.DKGRAY
    var outSideColor: Int = Color.WHITE
        get() {
            if (isDarkModeNow()) return outSideDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            invalidate()
        }

    /**
     * ????????????????????????
     */
    @IntRange(from = 0, to = 255)
    var outSideAlpha: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private var thumbWidth: Float = 0f
    private var maxValueText: String = ""
    private var nowValueText: String = ""
    private var maxValueTextWidth: Float = 0f
    private var nowValueTextWidth: Float = 0f
    private var nowValueTextOffset: Float = 0f
    private var textHeight: Float = SizeUtils.sp2px(18f).toFloat()
    private var textPadding: Long = 40L
    private var pathInside = Path()
    private var pathOutside = Path()
    private var rect = RectF()

    private var thumbPaint: Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.DKGRAY
        }
    private var maxTextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.textSize = textHeight
            it.isSubpixelText = true
        }
    private var nowTextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.textSize = textHeight
            it.isSubpixelText = true
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updatePath()
    }

    /**
     *  ???value??????String???????????????
     *  ??????????????????????????????
     *  eg. 00:00
     */
    open fun valueToText(value: Float): String {
        return value.toString()
    }

    /**
     *  ????????????????????????????????????
     */
    open fun isDarkModeNow(): Boolean {
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        thumbWidth = (nowValue - minValue) / (maxValue - minValue) * (width - padding)
        maxValueText = valueToText(maxValue)
        nowValueText = valueToText(nowValue)
        maxValueTextWidth = maxTextPaint.measureText(maxValueText)
        nowValueTextWidth = nowTextPaint.measureText(nowValueText)

        val textCenterHeight = (height + maxTextPaint.textSize) / 2f - 5
        val offsetTemp = nowValueTextWidth + textPadding * 2

        nowValueTextOffset =
            if (offsetTemp < thumbWidth) thumbWidth else offsetTemp
        nowTextPaint.color = nowTextColor
        maxTextPaint.color = maxTextColor
        thumbPaint.color = thumbColor

        // ?????????????????????
        canvas.clipPath(pathOutside)

        // ?????????????????????
        canvas.drawARGB(
            outSideAlpha,
            Color.red(outSideColor),
            Color.green(outSideColor),
            Color.blue(outSideColor)
        )

        // ?????????????????????path??????
        canvas.clipPath(pathInside)

        // ????????????
        canvas.drawColor(bgColor)

        // ?????????????????????
        canvas.drawText(
            maxValueText,
            width - maxValueTextWidth - textPadding,
            textCenterHeight,
            maxTextPaint
        )

        // ????????????????????????
        canvas.drawRoundRect(
            padding,
            padding,
            thumbWidth,
            height - padding,
            radius,
            radius,
            thumbPaint
        )

        // ????????????????????????
        canvas.drawText(
            nowValueText,
            nowValueTextOffset - nowValueTextWidth - textPadding,
            textCenterHeight,
            nowTextPaint
        )
    }

    open fun updatePath() {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        pathOutside.reset()
        pathOutside.addRoundRect(rect, radius, radius, Path.Direction.CW)

        rect.set(padding, padding, width - padding, height - padding)
        pathInside.reset()
        pathInside.addRoundRect(rect, radius, radius, Path.Direction.CW)
    }

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.NewProgressBar)
        radius = attr.getDimension(R.styleable.NewProgressBar_radius, 30f)
        attr.recycle()
        progressChangeListener.add(OnProgressBarProgressToListener())
    }
}