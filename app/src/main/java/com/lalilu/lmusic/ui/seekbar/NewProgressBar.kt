package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

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

    /**
     *  记录最大值
     */
    var sumValue: Float = 0f
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
            invalidate()
        }

    /**
     * 上层滑块颜色
     */
    var thumbColor: Int = Color.DKGRAY
        set(value) {
            field = value
            thumbPaint.color = value
        }

    private var thumbWidth: Float = 0f
    private var sunValueText: String = ""
    private var nowValueText: String = ""
    private var sunValueTextWidth: Float = 0f
    private var nowValueTextWidth: Float = 0f
    private var nowValueTextOffset: Float = 0f
    private var textHeight: Float = 45f
    private var textPadding: Long = 40L
    private var path = Path()
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

        thumbWidth = nowValue / sumValue * width

        sunValueText = valueToText(sumValue)
        nowValueText = valueToText(nowValue)
        sunValueTextWidth = textPaintDayNight.measureText(sunValueText)
        nowValueTextWidth = textPaintWhite.measureText(nowValueText)

        val textCenterHeight = (height + textPaintDayNight.textSize) / 2f - 5
        val offsetTemp = nowValueTextWidth + textPadding * 2

        nowValueTextOffset =
            if (offsetTemp < thumbWidth) thumbWidth else offsetTemp
        textPaintDayNight.color =
            if (isDarkModeNow()) Color.WHITE else Color.BLACK

        // 只保留圆角矩形path部分
        canvas.clipPath(path)

        // 绘制背景
        canvas.drawColor(bgColor)

        // 绘制总时长文字
        canvas.drawText(
            sunValueText,
            width - sunValueTextWidth - textPadding,
            textCenterHeight,
            textPaintDayNight
        )

        // 绘制进度条滑动块
        canvas.drawRoundRect(
            0f, 0f, thumbWidth, height.toFloat(), radius, radius, thumbPaint
        )

        // 绘制进度时间文字
        canvas.drawText(
            nowValueText, nowValueTextOffset - nowValueTextWidth - textPadding,
            textCenterHeight,
            textPaintWhite
        )
    }

    private fun updatePath() {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        path.reset()
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
    }
}