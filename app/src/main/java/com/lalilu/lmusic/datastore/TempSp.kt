package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class TempSp(context: Context) : BaseSp() {
    override val sp: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName + "_TEMP", Application.MODE_PRIVATE)
    }

    val dayOfYear = intSp("DAY_OF_YEAR")
    val dailyRecommends = stringListSp("DAILY_RECOMMENDS")
}