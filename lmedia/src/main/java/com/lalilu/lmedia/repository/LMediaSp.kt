package com.lalilu.lmedia.repository

import android.app.Application
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp

class LMediaSp(private val context: Application) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences("LMEDIA", Application.MODE_PRIVATE)
    }

    val includePath = obtainSet<String>("INCLUDE_PATH")
}
