package com.lalilu.lmusic.utils

import StatusBarLyric.API.StatusBarLyric
import android.app.Application
import androidx.core.graphics.drawable.toDrawable
import com.lalilu.R
import com.lalilu.lmusic.utils.extension.toBitmap


object StatusBarLyricExt {
    private var api: StatusBarLyric? = null

    fun init(application: Application) {
        api = StatusBarLyric(
            application,
            application.getDrawable(R.mipmap.ic_launcher)?.toBitmap()
                ?.toDrawable(application.resources),
            "com.lalilu.lmusic",
            false
        )
    }

    fun send(lrc: String?) {
        if (api?.hasEnable() == true) api?.updateLyric(lrc ?: "")
    }

    fun stop() {
        if (api?.hasEnable() == true) api?.stopLyric()
    }
}