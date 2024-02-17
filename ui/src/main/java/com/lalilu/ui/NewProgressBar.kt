package com.lalilu.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.blankj.utilcode.util.SizeUtils

fun interface OnValueChangeListener {
    fun onValueChange(value: Float)
}

open class NewProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

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
            onValueChange(value)
            invalidate()
        }

    protected open fun onValueChange(value: Float) {
        onValueChangeListener.forEach { it.onValueChange(value) }
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

    @FloatRange(from = 0.0, to = 1.0)
    var switchModeProgress: Float = 0f
        set(value) {
            if (thumbTabs.isEmpty()) return
            field = value
            invalidate()
        }

    var switchMoveX: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    val onValueChangeListener = HashSet<OnValueChangeListener>()
    protected val thumbTabs = ArrayList<Drawable>()
    private var thumbWidth: Float = 0f
    private var maxValueText: String = ""
    private var nowValueText: String = ""
    private var maxValueTextWidth: Float = 0f
    private var nowValueTextWidth: Float = 0f
    private var nowValueTextOffset: Float = 0f
    private var thumbLeft: Float = 0f
    private var thumbRight: Float = 0f
    private val thumbCount: Int
        get() = if (thumbTabs.size > 0) thumbTabs.size else 3

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

        val actualWidth = width - padding * 2f

        // 通过Value计算Progress，从而获取滑块应有的宽度
        thumbWidth = normalize(nowValue, minValue, maxValue) * actualWidth
        thumbWidth = lerp(thumbWidth, actualWidth / thumbCount, switchModeProgress)

        maxValueText = valueToText(maxValue)
        nowValueText = valueToText(nowValue)
        maxValueTextWidth = maxTextPaint.measureText(maxValueText)
        nowValueTextWidth = nowTextPaint.measureText(nowValueText)

        val textCenterHeight = height / 2f - (maxTextPaint.ascent() + maxTextPaint.descent()) / 2f
        val offsetTemp = nowValueTextWidth + textPadding * 2

        nowValueTextOffset = if (offsetTemp < thumbWidth) thumbWidth else offsetTemp
        nowTextPaint.color = nowTextColor
        maxTextPaint.color = maxTextColor

        nowTextPaint.alpha = lerp(0f, 255f, 1f - switchModeProgress).toInt()
        maxTextPaint.alpha = nowTextPaint.alpha
        thumbPaint.color = thumbColor

        thumbLeft = padding
        val switchProgress = normalize(switchMoveX, thumbWidth / 2f, width - thumbWidth / 2f)
        val switchOffset = lerp(thumbLeft, width - thumbLeft - thumbWidth, switchProgress)
        thumbLeft = lerp(thumbLeft, switchOffset, switchModeProgress)
        thumbRight = (thumbLeft + thumbWidth).coerceIn(0f, width.toFloat())

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

        if (nowTextPaint.alpha != 0) {
            // 绘制总时长文字
            canvas.drawText(
                maxValueText,
                width - maxValueTextWidth - textPadding,
                textCenterHeight,
                maxTextPaint
            )
        }

        // 绘制进度条滑动块
        canvas.drawRoundRect(
            thumbLeft, padding,
            thumbRight, height - padding,
            radius, radius, thumbPaint
        )

        if (nowTextPaint.alpha != 0) {
            // 绘制进度时间文字
            canvas.drawText(
                nowValueText,
                nowValueTextOffset - nowValueTextWidth - textPadding,
                textCenterHeight,
                nowTextPaint
            )
        }

        val switchThumbWidth = width / thumbCount
        val switchModeAlpha = lerp(0f, 255f, switchModeProgress).toInt()
        if (switchModeAlpha > 0) {
            var drawX = 0f
            for (tab in thumbTabs) {
                tab.apply {
                    alpha = switchModeAlpha

                    // 计算Drawable的原始宽高比
                    val ratio = (intrinsicWidth.toFloat() / intrinsicHeight.toFloat())
                        .takeIf { it > 0 } ?: 1f

                    val itemHeight = textHeight * 1.2f
                    val itemWidth = itemHeight * ratio

                    val itemLeft = drawX + (switchThumbWidth - itemWidth) / 2f
                    val itemTop = (height - itemHeight) / 2f

                    setBounds(
                        itemLeft.toInt(),
                        itemTop.toInt(),
                        (itemLeft + itemWidth).toInt(),
                        (itemTop + itemHeight).toInt()
                    )
                    draw(canvas)
                }
                drawX += switchThumbWidth
            }
        }
    }

    private fun normalize(value: Float, min: Float, max: Float): Float {
        return ((value - min) / (max - min))
            .coerceIn(0f, 1f)
    }

    private fun lerp(from: Float, to: Float, fraction: Float): Float {
        return (from + (to - from) * fraction)
            .coerceIn(minOf(from, to), maxOf(from, to))
    }

    open fun updatePath() {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        pathOutside.reset()
        pathOutside.addRoundRect(rect, radius * 1.2f, radius * 1.2f, Path.Direction.CW)

        rect.set(padding, padding, width - padding, height - padding)
        pathInside.reset()
        pathInside.addRoundRect(rect, radius, radius, Path.Direction.CW)
    }

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.NewProgressBar)
        radius = attr.getDimension(R.styleable.NewProgressBar_radius, 30f)
        attr.recycle()
    }
}