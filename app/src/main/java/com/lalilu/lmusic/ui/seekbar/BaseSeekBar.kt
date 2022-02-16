package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.view.GestureDetectorCompat

const val PROGRESS_STATE_NONE = -1
const val PROGRESS_STATE_MIN = 0
const val PROGRESS_STATE_MIDDLE = 1
const val PROGRESS_STATE_MAX = 2

abstract class BaseSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), AbstractSeekBar {
    private var seekBarListeners: MutableList<OnSeekBarListenerAdapter> = ArrayList()
    var onSeekBarListener: OnSeekBarListenerAdapter? = null
    var touching: Boolean = false

    protected val maxProgress: Float = 100f
    protected val minProgress: Float = 0f

    @FloatRange(from = 0.0, to = 100.0)
    var downProgress: Float = 0f

    @FloatRange(from = 0.0, to = 100.0)
    var dataProgress: Float = 0f

    @FloatRange(from = 0.0, to = 100.0)
    var drawProgress: Float = 0f
        set(value) {
            field = value
            if (tempProgressState != nowProgressState) {
                lastProgressState = tempProgressState
                tempProgressState = nowProgressState
                if (lastProgressState == PROGRESS_STATE_NONE) {
                    when (nowProgressState) {
                        PROGRESS_STATE_MIN -> onProgressMin()
                        PROGRESS_STATE_MIDDLE -> onProgressMiddle()
                        PROGRESS_STATE_MAX -> onProgressMax()
                    }
                }
            }
        }

    private var tempProgressState: Int = PROGRESS_STATE_NONE
    private var lastProgressState: Int = PROGRESS_STATE_NONE
    private val nowProgressState: Int
        get() = when (drawProgress) {
            minProgress -> PROGRESS_STATE_MIN
            maxProgress -> PROGRESS_STATE_MAX
            in 10f..90f -> PROGRESS_STATE_MIDDLE
            else -> PROGRESS_STATE_NONE
        }

    private var scaleAnimatorTo = 1.1f
    private var scaleAnimatorDuration = 200L
    private var sensitivity: Float = 0.15f

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                touching = true
                downProgress = drawProgress
                animateScaleTo(scaleAnimatorTo)
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
                if (downProgress != drawProgress) onTouchUpWithChange()
                animateScaleTo(1f)
            }
        }
        invalidate()
        return true
    }

    override fun updateProgress(distance: Float) {
        drawProgress = (drawProgress + distance * sensitivity)
            .coerceIn(minProgress, maxProgress)
    }

    override fun onProgressMax() {
        onSeekBarListener?.onProgressToMax()
        seekBarListeners.forEach { it.onProgressToMax() }
    }

    override fun onProgressMin() {
        onSeekBarListener?.onProgressToMin()
        seekBarListeners.forEach { it.onProgressToMin() }
    }

    override fun onProgressMiddle() {
        onSeekBarListener?.onProgressToMiddle()
        seekBarListeners.forEach { it.onProgressToMiddle() }
    }

    fun animateScaleTo(scaleValue: Float) {
        this.animate()
            .scaleY(scaleValue)
            .scaleX(scaleValue)
            .setDuration(scaleAnimatorDuration)
            .start()
    }
}