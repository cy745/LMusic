package com.lalilu.lplaylist.repository

import com.blankj.utilcode.util.GsonUtils
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

        return GsonUtils.toJson(list, typeToken.type)
    }

    override fun restore(content: String): Any? {
        return GsonUtils.fromJson(content, typeToken.type)
    }

    override fun accept(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): Boolean {
        return baseType == List::class && clazz == LPlaylist::class
    }
}