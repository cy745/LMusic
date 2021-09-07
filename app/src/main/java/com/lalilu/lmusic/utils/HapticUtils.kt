package com.lalilu.lmusic.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

class HapticUtils {
    companion object {
        fun haptic(view: View) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
}