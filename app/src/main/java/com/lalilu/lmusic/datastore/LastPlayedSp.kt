package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lalilu.lmusic.Config

class LastPlayedSp(context: Context) : BaseSp() {
    override val sp: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName + "_LAST_PLAYED", Application.MODE_PRIVATE)
    }

    val lastPlayedIdKey = stringSp(Config.LAST_PLAYED_ID)
    val lastPlayedPositionKey = longSp(Config.LAST_PLAYED_POSITION)
    val lastPlayedListIdsKey = stringListSp(Config.LAST_PLAYED_LIST_IDS)
}