package com.lalilu.lmusic.manager

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.common.reflect.TypeToken
import com.lalilu.lmusic.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object HistoryManager : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val typeToken = object : TypeToken<List<String>>() {}.type
    private val lastPlayedSp: SPUtils by lazy {
        SPUtils.getInstance(Config.LAST_PLAYED_SP)
    }

    var lastPlayedPosition: Long
        set(value) = lastPlayedSp.put(Config.LAST_PLAYED_POSITION, value)
        get() = lastPlayedSp.getLong(Config.LAST_PLAYED_POSITION, 0L)

    var lastPlayedId: String?
        set(value) = lastPlayedSp.put(Config.LAST_PLAYED_ID, value)
        get() = lastPlayedSp.getString(Config.LAST_PLAYED_ID)

    var lastPlayedListIds: List<String>?
        set(value) = lastPlayedSp.put(Config.LAST_PLAYED_LIST, GsonUtils.toJson(value))
        get() {
            val json = lastPlayedSp.getString(Config.LAST_PLAYED_LIST, null) ?: return null
            return GsonUtils.fromJson<List<String>>(json, typeToken)
        }
}