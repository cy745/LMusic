package com.lalilu.common

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import androidx.palette.graphics.Palette

fun Palette?.getAutomaticColor(): Int {
    if (this == null) return Color.DKGRAY
    var oldColor = this.getDarkVibrantColor(Color.LTGRAY)
    if (ColorUtils.isLightColor(oldColor))
        oldColor = this.getDarkMutedColor(Color.LTGRAY)
    return oldColor
}

object ColorUtils {
    fun isLightColor(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(
                color
            )) / 255
        return darkness < 0.5
    }

    fun getAutomaticColor(palette: Palette?): Int {
        if (palette == null) return Color.DKGRAY
        var oldColor = palette.getDarkVibrantColor(Color.LTGRAY)
        if (isLightColor(oldColor))
            oldColor = palette.getDarkMutedColor(Color.LTGRAY)
        return oldColor
    }
}

object ColorAnimator {
    private val colorMap: LinkedHashMap<Int, Int> = LinkedHashMap()
    private var transitionDuration = 600L

    fun setBgColorFromPalette(
        palette: Palette?,
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
