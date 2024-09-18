package com.lalilu.component.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp

class SongsSp(private val context: Context) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences(
            context.packageName + "_SONGS",
            Application.MODE_PRIVATE
        )
    }
}
