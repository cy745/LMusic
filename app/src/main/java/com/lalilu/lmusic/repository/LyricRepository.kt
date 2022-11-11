package com.lalilu.lmusic.repository

import androidx.compose.runtime.*
import androidx.lifecycle.asLiveData
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.CachedFlow
import com.lalilu.lmusic.utils.extension.UpdatableFlow
import com.lalilu.lmusic.utils.extension.toCachedFlow
import com.lalilu.lmusic.utils.extension.toUpdatableFlow
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class LyricRepository @Inject constructor(
    private val lyricSource: LyricSourceFactory,
    private val runtime: LMusicRuntime
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Composable
    fun rememberHasLyric(song: LSong): State<Boolean> {
        return remember { mutableStateOf(false) }.also { state ->
            LaunchedEffect(song) {
                if (isActive) {
                    state.value = hasLyric(song)
                }
            }
        }
    }

    suspend fun hasLyric(song: LSong): Boolean = withContext(Dispatchers.IO) {
        getLyric(song) != null
    }

    suspend fun getLyric(song: LSong): Pair<String, String?>? = withContext(Dispatchers.IO) {
        return@withContext lyricSource.loadLyric(song)
            ?.takeIf { it.first.isNotEmpty() }
    }

    val currentLyric: UpdatableFlow<Pair<String, String?>?> =
        runtime.playingFlow.mapLatest { item ->
            item?.let { Library.getSongOrNull(it.id) }
                ?.let { getLyric(it) }
        }.toUpdatableFlow()

    val currentLyricEntry: CachedFlow<List<LyricEntry>?> = currentLyric.mapLatest { pair ->
        pair ?: return@mapLatest null
        LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
    }.toCachedFlow()

    val currentLyricLiveData = currentLyric.asLiveData()
}