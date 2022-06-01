package com.lalilu.lmusic.utils.sources

import android.text.TextUtils
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.datasource.extensions.getSongData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricSourceFactory @Inject constructor(
    dataBaseLyricSource: DataBaseLyricSource,
    embeddedLyricSource: EmbeddedLyricSource,
    localLyricSource: LocalLyricSource
) {
    interface LyricSource {
        suspend fun loadLyric(mediaItem: MediaItem): Pair<String, String?>?
    }

    private val sources: MutableList<LyricSource> = ArrayList()

    init {
        sources.add(dataBaseLyricSource)
        sources.add(embeddedLyricSource)
        sources.add(localLyricSource)
    }

    suspend fun getLyric(
        mediaItem: MediaItem,
        callback: (String?, String?) -> Unit = { _, _ -> }
    ): Pair<String, String?>? = withContext(Dispatchers.IO) {
        sources.forEach { source ->
            val pair = source.loadLyric(mediaItem)
            if (pair != null) {
                callback(pair.first, pair.second)
                return@withContext pair
            }
        }
        callback(null, null)
        return@withContext null
    }
}

class EmbeddedLyricSource @Inject constructor() : LyricSourceFactory.LyricSource {
    override suspend fun loadLyric(mediaItem: MediaItem): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val songData = mediaItem.mediaMetadata.getSongData() ?: return@withContext null
            val file = File(songData)
            if (!file.exists()) return@withContext null
            kotlin.runCatching {
                Logger.getLogger("org.jaudiotagger").level = Level.OFF
                val tag = AudioFileIO.read(file).tag
                val lyric = tag.getFields(FieldKey.LYRICS)
                    .run { if (isNotEmpty()) get(0).toString() else null }
                    ?: return@withContext null
                return@withContext if (TextUtils.isEmpty(lyric)) null
                else Pair(lyric, null)
            }
            null
        }
}

class LocalLyricSource @Inject constructor() : LyricSourceFactory.LyricSource {
    override suspend fun loadLyric(mediaItem: MediaItem): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val songData = mediaItem.mediaMetadata.getSongData() ?: return@withContext null
            val path = songData.substring(0, songData.lastIndexOf('.')) + ".lrc"
            val lrcFile = File(path)

            if (!lrcFile.exists()) return@withContext null

            val lyric = lrcFile.readText()
            return@withContext if (TextUtils.isEmpty(lyric)) null
            else Pair(lyric, null)
        }
}

class DataBaseLyricSource @Inject constructor(
    val dataBase: MDataBase
) : LyricSourceFactory.LyricSource {
    override suspend fun loadLyric(mediaItem: MediaItem): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val pair = dataBase.networkDataDao().getById(mediaItem.mediaId)
            pair?.lyric?.let { Pair(it, pair.tlyric) }
        }
}