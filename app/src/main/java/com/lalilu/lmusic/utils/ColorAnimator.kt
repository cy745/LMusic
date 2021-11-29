package com.lalilu.lmusic.utils

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import androidx.palette.graphics.Palette

object ColorAnimator {
    private val colorMap: LinkedHashMap<Int, Int> = LinkedHashMap()
    private var transitionDuration = 600L

    fun setBgColorFromPalette(
        palette: Palette,
        viewGroup: ViewGroup
    ) {
        val plColor = palette.getAutomaticColor()
        val oldColor = colorMap[viewGroup.id] ?: Color.DKGRAY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ValueAnimator.ofArgb(oldColor, plColor).apply {
                duration = transitionDuration
                addUpdateListener {
                    val color = it.animatedValue as Int
                    viewGroup.setBackgroundColor(color)
                }
            }.start()
        } else {
            viewGroup.setBackgroundColor(plColor)
        }
        colorMap[viewGroup.id] = plColor
    }
}
