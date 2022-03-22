package com.lalilu.common

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object HapticUtils {
    enum class Strength(var value: Int) {
        HAPTIC_WEAK(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.KEYBOARD_RELEASE
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
        ),
        HAPTIC_STRONG(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.KEYBOARD_PRESS
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
        )
    }

    fun haptic(view: View, strength: Strength) {
        view.performHapticFeedback(strength.value)
    }

    fun haptic(view: View) {
        haptic(view, Strength.HAPTIC_STRONG)
    }

    fun weakHaptic(view: View) {
        haptic(view, Strength.HAPTIC_WEAK)
    }

    fun doubleHaptic(view: View) {
        haptic(view)
        view.postDelayed({ haptic(view) }, 100)
    }
}