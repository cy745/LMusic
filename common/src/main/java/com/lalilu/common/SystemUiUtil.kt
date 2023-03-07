package com.lalilu.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat

object SystemUiUtil {

    private var fixedStatusBarHeight: Int = -1

    /**
     * 获取系统资源中固定的状态栏高度数据
     */
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getFixedStatusHeight(context: Context): Int {
        if (fixedStatusBarHeight >= 0) {
            return fixedStatusBarHeight
        }
        val resourceId = context.resources
            .getIdentifier("status_bar_height", "dimen", "android")
            .takeIf { it > 0 }

        fixedStatusBarHeight =
            resourceId?.let { context.resources.getDimensionPixelSize(it) } ?: 128
        return fixedStatusBarHeight
    }

    fun immerseStatusBar(activity: Activity) {
        val window = activity.window
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    fun immerseNavigationBar(activity: Activity) {
        // https://www.jianshu.com/p/add47d6bde29
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = Color.TRANSPARENT
    }

    fun immersiveCutout(window: Window) {
        window.attributes.apply {
            // 刘海挖孔等异形屏适配，横竖屏都显示内容到被裁切的区域
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
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