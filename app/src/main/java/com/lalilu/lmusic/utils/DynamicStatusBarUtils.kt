package com.lalilu.lmusic.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

fun ComponentActivity.dynamicUpdateStatusBarColor(
    showLog: Boolean = false,
    delay: Long = 100,
) = lifecycleScope.launch(Dispatchers.Default) {
    repeatOnLifecycle(Lifecycle.State.RESUMED) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val statusBarHeight = getStatusBarHeight().takeIf { it > 0 } ?: 100
        val width = window.decorView.width.takeIf { it > 0 } ?: 100
        val bitmap = Bitmap.createBitmap(width, statusBarHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val handler = Handler(Looper.getMainLooper())
        val targetRect = Rect(0, 0, bitmap.width, bitmap.height)

        while (isActive) {
            delay(delay)

            if (!isActive) break
            //  截取Bitmap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val result = suspendCancellableCoroutine { continuation ->
                    runCatching {
                        PixelCopy.request(
                            window,
                            targetRect,
                            bitmap,
                            { continuation.resume(it) },
                            handler
                        )
                    }.getOrElse {
                        if (showLog) println("PixelCopy: ${it.message}")
                        continuation.resume(PixelCopy.ERROR_UNKNOWN)
                    }
                }
                if (result != PixelCopy.SUCCESS) continue
            } else {
                window.decorView.draw(canvas)
            }

            if (!isActive) break
            // 计算Bitmap内的平均亮度
            val averageLuminance = (0..<bitmap.width)
                .map { x ->
                    (0..<bitmap.height).map { y ->
                        Color(bitmap.getPixel(x, y)).luminance()
                    }
                }
                .flatten()
                .average()

            if (!isActive) break
            // 更新状态栏上的内容颜色
            if (showLog) println("averageLuminance: $averageLuminance")
            val target = averageLuminance > 0.5f
            if (controller.isAppearanceLightStatusBars != target) {
                withContext(Dispatchers.Main) {
                    controller.isAppearanceLightStatusBars = target
                }
            }
        }

        canvas.setBitmap(null)
        bitmap.recycle()
    }
}

@SuppressLint("InternalInsetResource")
private fun ComponentActivity.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}