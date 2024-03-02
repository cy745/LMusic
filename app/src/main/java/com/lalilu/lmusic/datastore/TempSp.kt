package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp

class TempSp(private val context: Context) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences(context.packageName + "_TEMP", Application.MODE_PRIVATE)
    }

    val dayOfYear = obtain<Int>("DAY_OF_YEAR")
    val dailyRecommends = obtainList<String>("DAILY_RECOMMENDS")
}