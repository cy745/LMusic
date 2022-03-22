package com.lalilu.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat

object StatusBarUtil {
    fun immerseStatusBar(activity: Activity) {
        val window = activity.window
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun isDarkMode(activity: Activity): Boolean {
        val mode = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    fun isDarkMode(application: Application): Boolean {
        val mode = application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    fun isDarkMode(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    @RequiresApi(Build.VERSION_CODES.R)
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

    @RequiresApi(Build.VERSION_CODES.R)
    fun statusTextColorDark(activity: Activity) {
        activity.window?.decorView?.windowInsetsController
            ?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun statusTextColorLight(activity: Activity) {
        activity.window?.decorView?.windowInsetsController
            ?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
    }
}