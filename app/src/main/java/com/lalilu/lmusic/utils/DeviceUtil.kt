package com.lalilu.lmusic.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 获取设备特有信息的工具集合类
 */
object DeviceUtil {

    /**
     * 【适应旋转】
     * 获取设备竖向高度
     */
    fun getHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return windowManager.currentWindowMetrics.bounds.height()
        }

        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(outMetrics)
        return outMetrics.heightPixels
    }

    /**
     * 【适应旋转】
     * 获取设备横向宽度
     */
    fun getWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return windowManager.currentWindowMetrics.bounds.width()
        }

        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    /**
     * 是否是平板
     *
     * @param context 上下文
     * @return 是平板则返回true，反之返回false
     *
     * ————————————————
     * 版权声明：本文为CSDN博主「Fantasy丶Lin」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
     * 原文链接：https://blog.csdn.net/Fantasy_Lin_/article/details/111828002
     */
    fun isPad(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        display.getMetrics(dm)
        val x = (dm.widthPixels / dm.xdpi).pow(2)
        val y = (dm.heightPixels / dm.ydpi).pow(2)
        val screenInches = sqrt(x + y)
        return screenInches >= 7.0
    }
}