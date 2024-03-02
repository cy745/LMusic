package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp
import com.lalilu.lmusic.Config

class LastPlayedSp(private val context: Context) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences(
            context.packageName + "_LAST_PLAYED",
            Application.MODE_PRIVATE
        )
    }

    val lastPlayedIdKey = obtain<String>(Config.LAST_PLAYED_ID)
    val lastPlayedPositionKey = obtain<Long>(Config.LAST_PLAYED_POSITION)
    val lastPlayedListIdsKey = obtainList<String>(Config.LAST_PLAYED_LIST_IDS)
}