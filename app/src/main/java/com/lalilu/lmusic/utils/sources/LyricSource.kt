package com.lalilu.lmusic.utils.sources

import android.text.TextUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

interface LyricSource {
    suspend fun loadLyric(song: LSong): Pair<String, String?>?

    suspend fun onLyricUpdate(callback: (Pair<String, String?>?) -> Unit) {
        callback(null)
    }
}

@Singleton
class LyricSourceFactory @Inject constructor(
    dataBaseLyricSource: DataBaseLyricSource,
    embeddedLyricSource: EmbeddedLyricSource,
    localLyricSource: LocalLyricSource
) : LyricSource {

    private val sources: MutableList<LyricSource> = ArrayList()

    init {
        sources.add(dataBaseLyricSource)
        sources.add(embeddedLyricSource)
        sources.add(localLyricSource)
        instance = this
    }

    override suspend fun loadLyric(song: LSong): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            for (source in sources) {
                val pair = source.loadLyric(song)
                if (pair != null) return@withContext pair
            }
            return@withContext null
        }

    companion object {
        var instance: LyricSourceFactory? = null
            private set
    }
}

class EmbeddedLyricSource @Inject constructor() : LyricSource {
    override suspend fun loadLyric(song: LSong): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val songData = song.pathStr ?: return@withContext null
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

class LocalLyricSource @Inject constructor() : LyricSource {
    override suspend fun loadLyric(song: LSong): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val songData = song.pathStr ?: return@withContext null
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
) : LyricSource {
    override suspend fun loadLyric(song: LSong): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val pair = dataBase.networkDataDao().getById(song.id)
            pair?.lyric?.let { Pair(it, pair.tlyric) }
        }
}