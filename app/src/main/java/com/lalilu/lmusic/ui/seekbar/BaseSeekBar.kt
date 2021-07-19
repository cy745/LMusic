package com.lalilu.lmusic.ui.seekbar

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lalilu.lmusic.utils.OnSeekBarChangeListenerAdapter
import kotlin.math.abs

abstract class BaseSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), AbstractSeekBar {
    var onSeekBarChangeListener: OnSeekBarChangeListenerAdapter? = null
    var touching: Boolean = false
    var progress: Double = 0.0
    var maxProgress: Float = 1f
    var minProgress: Float = 0f

    private var scaleAnimatorTo = 1.1f
    private var scaleAnimatorDuration = 200L

    private var downX: Float = -1f
    private var downY: Float = -1f
    private var rawX: Float = -1f
    private var rawY: Float = -1f
    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private var sensitivity: Float = 0.0015f


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                rawX = event.rawX
                rawY = event.rawY
                downX = event.rawX
                downY = event.rawY
                onTouchDown()
                onTouchLongClickStart()
                this.animate()
                    .scaleX(scaleAnimatorTo)
                    .scaleY(scaleAnimatorTo)
                    .setDuration(scaleAnimatorDuration)
                    .start()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                touching = false
                if (!isLongClickStarted) onTouchUp()
                onTouchLongClickCancel()
                onTouchLongClickEnd()
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(scaleAnimatorDuration)
                    .start()
            }
            MotionEvent.ACTION_MOVE -> {
                if (touching && !isLongClickStarted) {
                    if (abs(event.rawX - downX) > 20) {
                        onTouchLongClickCancel()
                    }
                    updateProgress(event, true)
                    onTouchMove()
                }
            }
        }
        invalidate()
        return true
    }

    private var longClickDelay = 100L
    private val longClickHandler = Handler(context.mainLooper)
    private var isLongClickStarted = false
    private var longClickStartTime = -1L

    private val longClickStart = Runnable {
        longClickStartTime = System.currentTimeMillis()
        isLongClickStarted = true
        touching = false
        onSeekBarChangeListener?.onLongClickStart()
    }

    override fun onTouchLongClickStart() {
        longClickHandler.postDelayed(longClickStart, longClickDelay)
    }

    override fun onTouchLongClickEnd() {
        if (isLongClickStarted) {
            isLongClickStarted = false
            val delay = System.currentTimeMillis() - longClickStartTime
            println(delay)
            if (delay < 100) {
                handler.postDelayed({
                    onSeekBarChangeListener?.onLongClickEnd()
                }, 100 - delay)
            } else {
                onSeekBarChangeListener?.onLongClickEnd()
            }
        }
    }

    override fun onTouchLongClickCancel() {
        longClickHandler.removeCallbacks(longClickStart)
        onSeekBarChangeListener?.onLongClickCancel()
    }

    override fun updateProgress(event: MotionEvent, moving: Boolean) {
        deltaX = (event.rawX - rawX)
        deltaY = (event.rawY - rawY)

        if (moving) {
            rawX = event.rawX
            rawY = event.rawY
        } else {
            rawX = -1f
            rawY = -1f
        }

        progress = clamp(
            progress + deltaX * sensitivity,
            maxProgress, minProgress
        ).toDouble()

        when (progress) {
            maxProgress.toDouble() -> onProgressMax()
            minProgress.toDouble() -> onProgressMin()
            in 0.1..0.9 -> onProgressMiddle()
        }
    }

    var progressIsMax = false
    var progressIsMin = true

    override fun onProgressMax() {
        if (!progressIsMax) {
            onSeekBarChangeListener?.onProgressToMax()
            progressIsMax = true
        }
    }

    override fun onProgressMin() {
        if (!progressIsMin) {
            onSeekBarChangeListener?.onProgressToMin()
            progressIsMin = true
        }
    }

    override fun onProgressMiddle() {
        if (progressIsMin || progressIsMax) {
            onSeekBarChangeListener?.onProgressToMiddle()
        }
        progressIsMax = false
        progressIsMin = false
    }

    protected fun clamp(num: Number, max: Number, min: Number): Number {
        if (num.toDouble() < min.toDouble()) return min.toDouble()
        if (num.toDouble() > max.toDouble()) return max.toDouble()
        return num.toDouble()
    }
}