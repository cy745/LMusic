package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lalilu.lmusic.R
import com.lalilu.lmusic.utils.DurationUtils

class SeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var downDuration: Long = -1L

    private var sumDuration: Long = 0L
    private var nowDuration: Long = 0L

    private var textPadding: Long = 40L
    private var textHeight: Float = 45f
    private var textPaintWhite: TextPaint
    private var textPaint: TextPaint
    private var paint: Paint
    private var backgroundPaint: Paint
    private var touching: Boolean = false

    private var scaleDuration = 200L
    private var scaleTo = 1.1f
    private var radius = 30f

    private var rawX: Float = -1f
    private var rawY: Float = -1f
    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private var progress: Double = 0.6
    private var maxProgress: Float = 1f
    private var minProgress: Float = 0f
    private var sensitivity: Float = 0.0015f

    private lateinit var callback: (selectDuration: Long) -> Unit
    fun setOnActionUp(callback: (selectDuration: Long) -> Unit) {
        this.callback = callback
    }

    fun setSumDuration(duration: Long) {
        sumDuration = duration
        invalidate()
    }

    fun updateDuration(duration: Long) {
        if (!touching) {
            nowDuration = duration
            progress = nowDuration / sumDuration.toDouble()
            invalidate()
        }
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.SeekBar, defStyleAttr, 0
        )
        paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.DKGRAY
        }
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.argb(50, 100, 100, 100)
        }
        textPaint = TextPaint().also {
            it.textSize = textHeight
            it.color = Color.BLACK
            it.isSubpixelText = true
        }
        textPaintWhite = TextPaint().also {
            it.textSize = textHeight
            it.color = Color.WHITE
            it.isSubpixelText = true
        }

        a.recycle()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                rawX = event.rawX
                rawY = event.rawY
                downDuration = nowDuration
                this.animate()
                    .scaleX(scaleTo)
                    .scaleY(scaleTo)
                    .setDuration(scaleDuration)
                    .start()
            }
            MotionEvent.ACTION_UP -> {
                touching = false
                rawX = -1f
                rawY = -1f
                println("downDuration: $downDuration")
                println("nowDuration: $nowDuration")

                if (downDuration != nowDuration) {
                    println("seek to $nowDuration")
                    callback(nowDuration)
                }
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(scaleDuration)
                    .start()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                touching = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (touching) {
                    deltaX = (event.rawX - rawX)
                    deltaY = (event.rawY - rawY)
                    // Y轴用于取消拖动进度条
                    if (deltaY < -300) {
                        touching = false
                    }
                    rawX = event.rawX
                    progress = clamp(
                        progress + deltaX * sensitivity,
                        maxProgress, minProgress
                    )
                }
            }
        }
        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        nowDuration = (progress * sumDuration).toLong()

        val progressWidth = progress * width

        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            radius,
            radius,
            backgroundPaint
        )

        val duration1 = DurationUtils.durationToString(sumDuration)
        val textWidth1 = textPaint.measureText(duration1)
        canvas.drawText(
            duration1,
            width - textWidth1 - textPadding,
            (height + textPaint.textSize) / 2f - 5,
            textPaint
        )

        canvas.drawRoundRect(
            0f, 0f,
            progressWidth.toFloat(), height.toFloat(), radius, radius, paint
        )

        val duration2 = DurationUtils.durationToString(nowDuration)
        val textWidth2 = textPaintWhite.measureText(duration2)
        val textLeft: Float = if (textWidth2 + textPadding * 2 < progressWidth) {
            progressWidth.toFloat()
        } else {
            textWidth2 + textPadding * 2
        }
        canvas.drawText(
            duration2,
            textLeft - textWidth2 - textPadding,
            (height + textPaint.textSize) / 2f - 5,
            textPaintWhite
        )
    }

    private fun clamp(num: Number, max: Number, min: Number): Double {
        if (num.toDouble() < min.toDouble()) {
            return min.toDouble()
        }
        if (num.toDouble() > max.toDouble()) {
            return max.toDouble()
        }
        return num.toDouble()
    }
}