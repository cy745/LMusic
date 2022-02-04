package com.lalilu.lmusic.utils

import android.text.TextUtils
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

object EmbeddedDataUtils {
    private val tagCache = LruCache<String, Tag?>(100)

    @Throws(Exception::class)
    fun getTag(songData: String?): Tag? {
        songData ?: return null
        Logger.getLogger("org.jaudiotagger").level = Level.OFF

        val tag = tagCache.get(songData)
        if (tag != null) return tag

        val file = File(songData)
        if (!file.exists()) return null
        return AudioFileIO.read(file).tag.also {
            tagCache.put(songData, it)
        }
    }

    fun loadLyric(songData: String?): String? {
        songData ?: return null

        val audioTag = getTag(songData) ?: return null
        val lyric = audioTag.getFields(FieldKey.LYRICS)
            .run { if (isNotEmpty()) get(0).toString() else null }

        if (TextUtils.isEmpty(lyric)) return null
        return lyric
    }

    suspend fun loadLyric(songData: String?, callback: (lyric: String?) -> Unit) =
        withContext(Dispatchers.IO) {
            callback(loadLyric(songData))
        }
}