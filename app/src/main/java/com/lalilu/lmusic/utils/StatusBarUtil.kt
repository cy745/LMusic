package com.lalilu.lmusic.utils

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.view.WindowInsetsController
import android.view.WindowManager

object StatusBarUtil {
    fun immerseStatusBar(activity: Activity) {
        val window = activity.window
        window.statusBarColor = Color.TRANSPARENT
        window.setDecorFitsSystemWindows(false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    fun isDarkMode(activity: Activity): Boolean {
        val mode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    fun statusTextColorWithDayNight(activity: Activity) {
        if (isDarkMode(activity)) {
            activity.window?.decorView?.windowInsetsController
                ?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            activity.window?.decorView?.windowInsetsController
                ?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
        }
    }
}