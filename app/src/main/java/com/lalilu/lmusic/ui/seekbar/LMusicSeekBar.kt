package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextPaint
import android.util.AttributeSet
import com.lalilu.common.TextUtils
import java.util.*
import kotlin.concurrent.schedule

class LMusicSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseSeekBar(context, attrs, defStyleAttr) {
    private var positionTimer: Timer? = null

    override fun updatePosition(playbackStateCompat: PlaybackStateCompat) {
        var currentDuration = getPositionFromPlaybackStateCompat(playbackStateCompat)
        positionTimer?.cancel()
        if (playbackStateCompat.state == PlaybackStateCompat.STATE_PLAYING) {
            positionTimer = Timer().apply {
                this.schedule(0, 16) {
                    updatePosition(currentDuration)
                    currentDuration += 16
                    if (currentDuration >= sumDuration) this.cancel()
                }
            }
        } else {
            updatePosition(currentDuration)
        }
    }

    override fun updatePosition(position: Long) {
        if (!touching) {
            nowDuration = position
            progress = nowDuration.toDouble() / sumDuration.toDouble()
            invalidate()
        }
    }

    override fun onTouchUp() {
        positionTimer?.cancel()
        if (downDuration != nowDuration) {
            onTouchUpWithChange()
            onSeekBarChangeListener?.onStopTrackingTouch(nowDuration)
        } else {
            rootView.performHapticFeedback(31011);
            performClick()
        }
    }

    override fun onTouchUpWithChange() {
        nowDuration = (progress * sumDuration).toLong()
    }

    override fun onTouchMove() {
        onSeekBarChangeListener?.onPositionChanged(nowDuration, true)
    }

    override fun onTouchDown() {
        downDuration = nowDuration
        onSeekBarChangeListener?.onStartTrackingTouch(nowDuration)
    }

    fun setSumDuration(duration: Long) {
        sumDuration = duration
        invalidate()
    }

    fun setThumbColor(color: Int) {
        paint.color = color
        invalidate()
    }

    private var downDuration: Long = -1L

    private var sumDuration: Long = 0L
    private var nowDuration: Long = 0L

    private var radius = 30f
    private var textPadding: Long = 40L
    private var textHeight: Float = 45f

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.DKGRAY
    }
    private var textPaint: TextPaint = TextPaint().also {
        it.textSize = textHeight
        it.color = Color.BLACK
        it.isSubpixelText = true
    }
    private var textPaintWhite: TextPaint = TextPaint().also {
        it.textSize = textHeight
        it.color = Color.WHITE
        it.isSubpixelText = true
    }
    private var backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.argb(50, 100, 100, 100)
    }

    private var progressWidth: Float = 0F
    private var sumDurationText = ""
    private var sumDurationTextWidth: Float = 0F
    private var nowDurationText = ""
    private var nowDurationTextWidth: Float = 0F
    private var nowDurationTextOffset: Float = 0F

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        val destinyWidth = (progress * width).toFloat()
//        progressWidth =
//            clamp(progressWidth + (destinyWidth - progressWidth) * 0.1f, destinyWidth, 0).toFloat()

        progressWidth = (progress * width).toFloat()
        nowDuration = (progress * sumDuration).toLong()

        sumDurationText = TextUtils.durationToString(sumDuration)
        sumDurationTextWidth = textPaint.measureText(sumDurationText)
        nowDurationText = TextUtils.durationToString(nowDuration)
        nowDurationTextWidth = textPaintWhite.measureText(nowDurationText)

        val textCenterHeight = (height + textPaint.textSize) / 2f - 5
        val offsetTemp = nowDurationTextWidth + textPadding * 2
        nowDurationTextOffset = if (offsetTemp < progressWidth) progressWidth else offsetTemp

        paint.alpha = clamp(progressWidth / radius / 2 * 255, 255, 0).toInt()

        // draw background
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            radius,
            radius,
            backgroundPaint
        )

        // draw sumDuration
        canvas.drawText(
            sumDurationText,
            width - sumDurationTextWidth - textPadding,
            textCenterHeight,
            textPaint
        )

        // draw thumb
        canvas.drawRoundRect(
            0f, 0f, progressWidth, height.toFloat(), radius, radius, paint
        )

        // draw nowDuration
        canvas.drawText(
            nowDurationText, nowDurationTextOffset - nowDurationTextWidth - textPadding,
            textCenterHeight,
            textPaintWhite
        )
    }

    private fun getPositionFromPlaybackStateCompat(playbackStateCompat: PlaybackStateCompat): Long {
        return playbackStateCompat.position + (playbackStateCompat.playbackSpeed * (SystemClock.elapsedRealtime() - playbackStateCompat.lastPositionUpdateTime)).toLong()
    }
}