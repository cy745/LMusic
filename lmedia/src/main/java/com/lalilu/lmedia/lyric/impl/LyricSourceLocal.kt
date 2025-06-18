package com.lalilu.lmedia.lyric.impl

import androidx.media3.common.MediaItem
import com.lalilu.lmedia.lyric.LyricSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LyricSourceLocal : LyricSource {
    override suspend fun loadLyric(
        song: MediaItem
    ): Pair<String, String?>? = withContext(Dispatchers.IO) {
        val lyricFile = getLyricFile(mediaItem = song)
            ?: return@withContext null

        val lyric = lyricFile.readText()
            .takeIf { it.isNotBlank() }
            ?: return@withContext null

        lyric to null
    }

    override suspend fun hasLyric(
        song: MediaItem
    ): Boolean = withContext(Dispatchers.Default) {
        getLyricFile(mediaItem = song) != null
    }

    private fun getLyricFile(mediaItem: MediaItem): File? {
        val songData = mediaItem.requestMetadata.extras?.getString("DATA")
            ?: return null

        val path = songData.substring(0, songData.lastIndexOf('.')) + ".lrc"

        return File(path)
            .takeIf { it.exists() && it.canRead() }
    }
}
