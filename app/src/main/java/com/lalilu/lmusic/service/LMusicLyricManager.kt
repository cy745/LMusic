package com.lalilu.lmusic.service

import android.util.LruCache
import androidx.lifecycle.asLiveData
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.utils.CachedFlow
import com.lalilu.lmusic.utils.UpdatableFlow
import com.lalilu.lmusic.utils.sources.LyricSource
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import com.lalilu.lmusic.utils.toCachedFlow
import com.lalilu.lmusic.utils.toUpdatableFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
object LMusicLyricManager : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private lateinit var lyricSource: LyricSource
    private val cache = LruCache<String, Int>(500)

    fun init(lyricSource: LyricSourceFactory) {
        this.lyricSource = lyricSource
    }

    suspend fun hasLyric(song: LSong): Boolean = withContext(Dispatchers.IO) {
        var result = cache.get(song.id) ?: LYRIC_UNKNOWN
        if (result == LYRIC_UNKNOWN) {
            result = if (getLyric(song) == null) LYRIC_NOT_EXIST else LYRIC_EXIST
        }
        result == LYRIC_EXIST
    }

    suspend fun getLyric(song: LSong): Pair<String, String?>? = withContext(Dispatchers.IO) {
        val lyric = lyricSource.loadLyric(song)
        val result = if (lyric != null && lyric.first.isNotEmpty()) LYRIC_EXIST else LYRIC_NOT_EXIST
        cache.put(song.id, result)

        return@withContext if (result == LYRIC_EXIST) lyric else null
    }

    private const val LYRIC_UNKNOWN = -1
    private const val LYRIC_NOT_EXIST = 0
    private const val LYRIC_EXIST = 1

    val currentLyric: UpdatableFlow<Pair<String, String?>?> =
        LMusicRuntime.currentPlayingFlow.mapLatest { item ->
            val song = item?.let { Library.getSongOrNull(it.id) } ?: return@mapLatest null
            getLyric(song)
        }.toUpdatableFlow()

    val currentLyricEntry: CachedFlow<List<LyricEntry>?> = currentLyric.mapLatest { pair ->
        pair ?: return@mapLatest null
        LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
    }.toCachedFlow()

    val currentLyricLiveData = currentLyric.asLiveData()
}