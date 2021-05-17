package com.lalilu.lmusic.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi

@SuppressLint("Recycle")
@RequiresApi(Build.VERSION_CODES.O)
class ColorAnimator constructor(fromColor: Int, toColor: Int) : ValueAnimator(),
    ValueAnimator.AnimatorUpdateListener {
    var rFrom: Float = 0f
    var gFrom: Float = 0f
    var bFrom: Float = 0f
    var rTo: Float = 0f
    var gTo: Float = 0f
    var bTo: Float = 0f

    private lateinit var listen: ((Int) -> Unit)

    fun setColorChangedListener(listen: ((Int) -> Unit)): ColorAnimator {
        this.listen = listen
        return this
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Float
        val rResult = rFrom + (rTo - rFrom) * value
        val gResult = gFrom + (gTo - gFrom) * value
        val bResult = bFrom + (bTo - bFrom) * value
        val result = Color.rgb(rResult, gResult, bResult)
        listen.invoke(result)
    }

    override fun setDuration(duration: Long): ValueAnimator {
        super.setDuration(duration)
        return this@ColorAnimator
    }

    fun start(duration: Number) {
        this.duration = duration.toLong()
        start()
    }

    init {
        this.setFloatValues(0f, 1f)
        this.setColor(fromColor, toColor)
        this.addUpdateListener(this)
    }

    private fun setColor(fromColor: Int, toColor: Int): ColorAnimator {
        rFrom = Color.valueOf(fromColor).red()
        gFrom = Color.valueOf(fromColor).green()
        bFrom = Color.valueOf(fromColor).blue()
        rTo = Color.valueOf(toColor).red()
        gTo = Color.valueOf(toColor).green()
        bTo = Color.valueOf(toColor).blue()
        return this
    }
}