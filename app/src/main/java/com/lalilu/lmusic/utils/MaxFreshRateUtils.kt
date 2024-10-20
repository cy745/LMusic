package com.lalilu.lmusic.utils

import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat


fun ComponentActivity.setToMaxFreshRate() {
    // 优先最高帧率运行
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val params: WindowManager.LayoutParams = window.attributes
        val supportedMode = ContextCompat
            .getDisplayOrDefault(this)
            .supportedModes
            .maxBy { it.refreshRate }

        supportedMode?.let {
            params.preferredRefreshRate = it.refreshRate
            params.preferredDisplayModeId = it.modeId
            window.attributes = params
        }
    }
}