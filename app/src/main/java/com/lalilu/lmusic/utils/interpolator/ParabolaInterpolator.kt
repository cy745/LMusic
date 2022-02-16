package com.lalilu.lmusic.utils.interpolator

import android.view.animation.Interpolator
import kotlin.math.pow


class ParabolaInterpolator : Interpolator {
    override fun getInterpolation(x: Float): Float {
        return 2f * (x - x.pow(2))
    }
}