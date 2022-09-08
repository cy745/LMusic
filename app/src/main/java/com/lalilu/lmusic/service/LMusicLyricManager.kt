package com.lalilu.lmusic.service

import androidx.lifecycle.asLiveData
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.utils.CachedFlow
import com.lalilu.lmusic.utils.UpdatableFlow
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import com.lalilu.lmusic.utils.toCachedFlow
import com.lalilu.lmusic.utils.toUpdatableFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
object LMusicLyricManager {
    private var lyricSource: LyricSourceFactory? = null

    fun init(lyricSourceFactory: LyricSourceFactory) {
        lyricSource = lyricSourceFactory
    }

    val currentLyric: UpdatableFlow<Pair<String, String?>?> =
        LMusicRuntime.currentPlayingFlow.mapLatest { item ->
            val song = item?.let { Library.getSongOrNull(it.id) } ?: return@mapLatest null
            lyricSource?.getLyric(song)
        }.toUpdatableFlow()

    val currentLyricEntry: CachedFlow<List<LyricEntry>?> = currentLyric.mapLatest { pair ->
        pair ?: return@mapLatest null
        LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
    }.toCachedFlow()

    val currentLyricLiveData = currentLyric.asLiveData()
}