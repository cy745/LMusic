package com.lalilu.lmedia.lyric

import android.content.Context
import androidx.media3.common.MediaItem
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.wrapper.Taglib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface LyricSource {
    suspend fun loadLyric(song: MediaItem): Pair<String, String?>?
    suspend fun hasLyric(song: MediaItem): Boolean
    suspend fun clearLyricCache(song: MediaItem) {}
}

enum class LyricExistState {
    Exist, Blank, Unknown
}

class LyricSourceFactory(
    private vararg val sources: LyricSource
) : LyricSource {
    private val lyricExistMap = mutableMapOf<String, LyricExistState>()

    override suspend fun loadLyric(song: MediaItem): Pair<String, String?>? =
        withContext(Dispatchers.IO) { sources.firstNotNullOfOrNull { it.loadLyric(song) } }

    override suspend fun hasLyric(song: MediaItem): Boolean {
        var state = lyricExistMap[song.mediaId] ?: LyricExistState.Unknown

        if (state == LyricExistState.Unknown) {
            val exist = sources.any { it.hasLyric(song) }
            state = if (exist) LyricExistState.Exist else LyricExistState.Blank
            lyricExistMap[song.mediaId] = state
        }

        return state == LyricExistState.Exist
    }

    // TODO 待实现监听某一首歌曲更新的逻辑后调用
    override suspend fun clearLyricCache(song: MediaItem) {
        lyricExistMap[song.mediaId] = LyricExistState.Unknown
    }
}

class LyricSourceEmbedded(private val context: Context) : LyricSource {
    override suspend fun loadLyric(song: MediaItem): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
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

    override suspend fun hasLyric(song: MediaItem): Boolean =
        withContext(Dispatchers.Default) {
            loadLyric(song) != null
        }
}

class LyricSourceLocal : LyricSource {
    override suspend fun loadLyric(song: MediaItem): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            // TODO 待完成获取文件路径的逻辑
            val songData = song.requestMetadata.extras?.getString("DATA")
                ?: return@withContext null

            val path = songData.substring(0, songData.lastIndexOf('.')) + ".lrc"
            val lrcFile = File(path)

            if (!lrcFile.exists()) return@withContext null

            val lyric = lrcFile.readText()
            if (lyric.isBlank()) return@withContext null
            lyric to null
        }

    override suspend fun hasLyric(song: MediaItem): Boolean =
        withContext(Dispatchers.Default) {
            val songData = song.requestMetadata.extras?.getString("DATA")
                ?: return@withContext false

            val path = songData.substring(0, songData.lastIndexOf('.')) + ".lrc"
            val lrcFile = File(path)
            return@withContext lrcFile.exists()
        }
}


