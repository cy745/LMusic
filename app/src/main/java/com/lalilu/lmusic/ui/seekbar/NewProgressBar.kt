package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import com.lalilu.R

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
     *  圆角半径
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
     *  记录最大值
     */
    var minValue: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  记录最大值
     */
    var maxValue: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  当前的数据
     */
    var nowValue: Float = 0f
        set(value) {
            field = value.coerceIn(minValue, maxValue)
            invalidate()
        }

    var minIncrement: Float = 0f

    /**
     * 当前值文字颜色
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
     * 最大值文字颜色
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
     * 上层滑块颜色
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
     * 外部框背景颜色
     * 绘制时将忽略该值的透明度
     * 由 [outSideAlpha] 控制其透明度
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
     * 外部框背景透明度
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
    private var textHeight: Float = 45f
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
     *  将value转为String以用于绘制
     *  可按需求转成各种格式
     *  eg. 00:00
     */
    open fun valueToText(value: Float): String {
        return value.toString()
    }

    /**
     *  判断当前是否处于深色模式
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

        // 截取外部框范围
        canvas.clipPath(pathOutside)

        // 绘制外部框背景
        canvas.drawARGB(
            outSideAlpha,
            Color.red(outSideColor),
            Color.green(outSideColor),
            Color.blue(outSideColor)
        )

        // 只保留圆角矩形path部分
        canvas.clipPath(pathInside)

        // 绘制背景
        canvas.drawColor(bgColor)

        // 绘制总时长文字
        canvas.drawText(
            maxValueText,
            width - maxValueTextWidth - textPadding,
            textCenterHeight,
            maxTextPaint
        )

        // 绘制进度条滑动块
        canvas.drawRoundRect(
            padding,
            padding,
            thumbWidth,
            height - padding,
            radius,
            radius,
            thumbPaint
        )

        // 绘制进度时间文字
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