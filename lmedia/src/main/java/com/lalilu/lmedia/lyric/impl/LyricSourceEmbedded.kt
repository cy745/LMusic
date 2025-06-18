package com.lalilu.lmedia.lyric.impl

import android.content.Context
import androidx.media3.common.MediaItem
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.lyric.LyricSource
import com.lalilu.lmedia.wrapper.Taglib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricSourceEmbedded(private val context: Context) : LyricSource {
    override suspend fun loadLyric(
        song: MediaItem
    ): Pair<String, String?>? = withContext(Dispatchers.IO) {
        val uri = song.requestMetadata.mediaUri ?: return@withContext null

        val lyric = runCatching {
            context.contentResolver.openFileDescriptor(uri, "r").use {
                it ?: return@use null
                Taglib.getLyricWithFD(it.detachFd())
            }
        }.getOrElse {
            LogUtils.e(song, it)
            null
        }

        if (lyric.isNullOrBlank()) return@withContext null
        lyric to null
    }

    override suspend fun hasLyric(song: MediaItem): Boolean = withContext(Dispatchers.Default) {
        loadLyric(song) != null
    }
}
