package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.IntRange

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
    fun onProgressChange(value: Float)
}

interface OnProgressToListener {
    fun onProgressToMax(value: Float) {}
    fun onProgressToMin(value: Float) {}
    fun onProgressToMiddle(value: Float) {}
}

open class NewProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    inner class OnProgressBarProgressToListener : OnProgressChangeListener {
        @CurrentState
        var currentState = 0
            set(value) {
                if (field == value) return
                field = value
                when (value) {
                    CURRENT_STATE_MIN -> progressToListener.forEach { it.onProgressToMin(nowValue) }
                    CURRENT_STATE_MAX -> progressToListener.forEach { it.onProgressToMax(nowValue) }
                    CURRENT_STATE_MIDDLE -> progressToListener.forEach {
                        it.onProgressToMiddle(
                            nowValue
                        )
                    }
                }
            }

        override fun onProgressChange(value: Float) {
            currentState = when {
                value <= minValue -> CURRENT_STATE_MIN
                value >= maxValue -> CURRENT_STATE_MAX
                value > minValue && value < maxValue -> CURRENT_STATE_MIDDLE
                else -> CURRENT_STATE_UNSPECIFIED
            }
        }
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
            field = value
            progressChangeListener.forEach { it.onProgressChange(value) }
            invalidate()
        }

    /**
     * 上层滑块颜色
     */
    var thumbColor: Int = Color.DKGRAY
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
    var outSideColor: Int = Color.WHITE
        get() {
            return if (isDarkModeNow()) Color.DKGRAY else field
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
    private var textPaintDayNight =
        TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.textSize = textHeight
            it.color = Color.BLACK
            it.isSubpixelText = true
        }
    private var textPaintWhite =
        TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.textSize = textHeight
            it.color = Color.WHITE
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

        thumbWidth = nowValue / (maxValue - minValue) * (width - padding)
        maxValueText = valueToText(maxValue)
        nowValueText = valueToText(nowValue)
        maxValueTextWidth = textPaintDayNight.measureText(maxValueText)
        nowValueTextWidth = textPaintWhite.measureText(nowValueText)

        val textCenterHeight = (height + textPaintDayNight.textSize) / 2f - 5
        val offsetTemp = nowValueTextWidth + textPadding * 2

        nowValueTextOffset =
            if (offsetTemp < thumbWidth) thumbWidth else offsetTemp
        textPaintDayNight.color =
            if (isDarkModeNow()) Color.WHITE else Color.BLACK

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
            textPaintDayNight
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
            textPaintWhite
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
        progressChangeListener.add(OnProgressBarProgressToListener())
    }
}