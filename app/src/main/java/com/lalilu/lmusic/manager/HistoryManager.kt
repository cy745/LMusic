package com.lalilu.lmusic.manager

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.common.reflect.TypeToken
import com.lalilu.lmusic.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object HistoryManager : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val typeToken = object : TypeToken<List<String>>() {}.type
    private val lastPlayedSp: SPUtils by lazy {
        SPUtils.getInstance(Config.LAST_PLAYED_SP)
    }

    fun saveLastPlayedPosition(position: Long) = launch {
        lastPlayedSp.put(Config.LAST_PLAYED_POSITION, position)
    }

    fun saveLastPlayedId(mediaId: String?) = launch {
        lastPlayedSp.put(Config.LAST_PLAYED_ID, mediaId)
    }

    fun saveLastPlayedListIds(mediaIds: List<String>) = launch {
        lastPlayedSp.put(Config.LAST_PLAYED_LIST, GsonUtils.toJson(mediaIds))
    }

    fun getLastPlayedPosition(): Long {
        return lastPlayedSp.getLong(Config.LAST_PLAYED_POSITION, 0L)
    }

    fun getLastPlayedId(): String? {
        return lastPlayedSp.getString(Config.LAST_PLAYED_ID)
    }

    fun getLastPlayedListIds(): List<String>? {
        val json = lastPlayedSp.getString(Config.LAST_PLAYED_LIST, null) ?: return null
        return GsonUtils.fromJson<List<String>>(json, typeToken)
    }
}