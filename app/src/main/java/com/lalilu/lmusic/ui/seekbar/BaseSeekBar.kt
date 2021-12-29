package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat

abstract class BaseSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), AbstractSeekBar {
    var onSeekBarListener: OnSeekBarListenerAdapter? = null
    var touching: Boolean = false
    var progress: Double = 0.0
    var maxProgress: Float = 1f
    var minProgress: Float = 0f

    private var scaleAnimatorTo = 1.1f
    private var scaleAnimatorDuration = 200L

    private var downProgress = 0.0
    private var sensitivity: Float = 0.0015f

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                touching = true
                downProgress = progress
                this@BaseSeekBar.animate()
                    .scaleX(scaleAnimatorTo)
                    .scaleY(scaleAnimatorTo)
                    .setDuration(scaleAnimatorDuration)
                    .start()
                return super.onDown(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                onSeekBarListener?.onPlayPause()
                return super.onSingleTapConfirmed(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val previousLeft = this@BaseSeekBar.x
                val previousRight = previousLeft + this@BaseSeekBar.width * 2 / 5
                val nextLeft = previousLeft + this@BaseSeekBar.width * 3 / 5
                val nextRight = previousLeft + this@BaseSeekBar.width
                when (e.rawX) {
                    in previousLeft..previousRight -> onSeekBarListener?.onPlayPrevious()
                    in nextLeft..nextRight -> onSeekBarListener?.onPlayNext()
                }
                return super.onDoubleTap(e)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                updateProgress(-distanceX)
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                touching = false
                if (downProgress != progress) onTouchUpWithChange()
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(scaleAnimatorDuration)
                    .start()
            }
        }
        invalidate()
        return true
    }

    override fun updateProgress(distance: Float) {
        progress = clamp(
            progress + distance * sensitivity,
            maxProgress, minProgress
        ).toDouble()

        when (progress) {
            maxProgress.toDouble() -> onProgressMax()
            minProgress.toDouble() -> onProgressMin()
            in 0.1..0.9 -> onProgressMiddle()
        }
    }

    private var progressIsMax = false
    private var progressIsMin = true

    override fun onProgressMax() {
        if (!progressIsMax) {
            onSeekBarListener?.onProgressToMax()
            progressIsMax = true
        }
    }

    override fun onProgressMin() {
        if (!progressIsMin) {
            onSeekBarListener?.onProgressToMin()
            progressIsMin = true
        }
    }

    override fun onProgressMiddle() {
        if (progressIsMin || progressIsMax) {
            onSeekBarListener?.onProgressToMiddle()
        }
        progressIsMax = false
        progressIsMin = false
    }

    private fun clamp(num: Number, max: Number, min: Number): Number {
        if (num.toDouble() < min.toDouble()) return min.toDouble()
        if (num.toDouble() > max.toDouble()) return max.toDouble()
        return num.toDouble()
    }
}