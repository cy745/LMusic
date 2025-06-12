package com.lalilu.lplaylist.repository

import com.lalilu.common.kv.KVContext
import com.lalilu.common.kv.KVConverter
import com.lalilu.lplaylist.entity.LPlaylist
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass

object PlaylistKV : KVContext("playlist") {

    init {
        registerConverter(LPlaylistListKVConverter())
    }

    val playlistList = obtainList<LPlaylist>(key = "PLAYLIST")
        .apply { disableAutoSave() }
}

class LPlaylistListKVConverter : KVConverter {
    private val json by KoinPlatform.getKoin().inject<Json>()

    override fun convert(value: Any?): String {
        val list = (value as? List<*>)
            ?.mapNotNull { it as? LPlaylist }
            ?: return ""

        return runCatching { json.encodeToString(list) }
            .getOrNull()
            ?: ""
    }

    override fun restore(content: String): Any? {
        return json.decodeFromString<List<LPlaylist>>(content)
    }

    override fun accept(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): Boolean {
        return baseType == List::class && clazz == LPlaylist::class
    }
}