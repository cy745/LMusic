package com.lalilu.lplaylist.repository

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.reflect.TypeToken
import com.lalilu.common.kv.KVContext
import com.lalilu.common.kv.KVConverter
import com.lalilu.lplaylist.entity.LPlaylist
import kotlin.reflect.KClass

object PlaylistKV : KVContext("playlist") {

    init {
        registerConverter(LPlaylistListKVConverter())
    }

    val playlistList = obtainList<LPlaylist>(key = "PLAYLIST")
        .apply { disableAutoSave() }
}

class LPlaylistListKVConverter : KVConverter {
    val typeToken = object : TypeToken<List<LPlaylist>>() {}

    override fun convert(value: Any?): String {
        val list = (value as? List<*>)
            ?.mapNotNull { it as? LPlaylist }
            ?: return ""

        return runCatching { GsonUtils.toJson(list, typeToken.type) }
            .getOrNull()
            ?: ""
    }

    override fun restore(content: String): Any? {
        return try {
            GsonUtils.fromJson(content, typeToken.type)
        } catch (e: Exception) {
            LogUtils.e(e)
            emptyList<LPlaylist>()
        }
    }

    override fun accept(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): Boolean {
        return baseType == List::class && clazz == LPlaylist::class
    }
}