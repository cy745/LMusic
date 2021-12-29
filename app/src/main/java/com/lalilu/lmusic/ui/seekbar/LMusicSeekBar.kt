package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.TextUtils

class LMusicSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseSeekBar(context, attrs, defStyleAttr) {

    override fun updatePosition(position: Long) {
        if (!touching) {
            nowDuration = position
            progress = nowDuration.toDouble() / sumDuration.toDouble()
            when (progress) {
                maxProgress.toDouble() -> onProgressMax()
                minProgress.toDouble() -> onProgressMin()
                in 0.1..0.9 -> onProgressMiddle()
            }
            invalidate()
        }
    }

    override fun onTouchUpWithChange() {
        nowDuration = (progress * sumDuration).toLong()
        onSeekBarListener?.onPositionUpdate(nowDuration)
    }

    fun setSumDuration(duration: Long) {
        sumDuration = duration
        invalidate()
    }

    fun setThumbColor(color: Int) {
        paint.color = color
        invalidate()
    }

    private var sumDuration: Long = 0L
    private var nowDuration: Long = 0L

    private var radius = 30f
    private var textPadding: Long = 40L
    private var textHeight: Float = 45f

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.DKGRAY
    }
    private var textPaintDayNight = TextPaint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = textHeight
        it.color = Color.BLACK
        it.isSubpixelText = true
    }
    private var textPaintWhite = TextPaint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = textHeight
        it.color = Color.WHITE
        it.isSubpixelText = true
    }
    private val backgroundColor = Color.argb(50, 100, 100, 100)

    private var progressWidth: Float = 0F
    private var sumDurationText = ""
    private var sumDurationTextWidth: Float = 0F
    private var nowDurationText = ""
    private var nowDurationTextWidth: Float = 0F
    private var nowDurationTextOffset: Float = 0F

    private var rect = RectF()
    private var path = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        progressWidth = (progress * width).toFloat()
        nowDuration = (progress * sumDuration).toLong()

        sumDurationText = TextUtils.durationToString(sumDuration)
        nowDurationText = TextUtils.durationToString(nowDuration)
        sumDurationTextWidth = textPaintDayNight.measureText(sumDurationText)
        nowDurationTextWidth = textPaintWhite.measureText(nowDurationText)

        val textCenterHeight = (height + textPaintDayNight.textSize) / 2f - 5
        val offsetTemp = nowDurationTextWidth + textPadding * 2

        nowDurationTextOffset =
            if (offsetTemp < progressWidth) progressWidth else offsetTemp
        textPaintDayNight.color =
            if (StatusBarUtil.isDarkMode(context)) Color.WHITE else Color.BLACK

        // 只保留圆角矩形path部分
        canvas.clipPath(path)

        // 绘制背景
        canvas.drawColor(backgroundColor)

        // 绘制总时长文字
        canvas.drawText(
            sumDurationText,
            width - sumDurationTextWidth - textPadding,
            textCenterHeight,
            textPaintDayNight
        )

        // 绘制进度条滑动块
        canvas.drawRoundRect(
            0f, 0f, progressWidth, height.toFloat(), radius, radius, paint
        )

        // 绘制进度时间文字
        canvas.drawText(
            nowDurationText, nowDurationTextOffset - nowDurationTextWidth - textPadding,
            textCenterHeight,
            textPaintWhite
        )
    }
}