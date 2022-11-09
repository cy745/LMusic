package com.lalilu.lmusic.repository

import android.util.LruCache
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
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
    private val listener = LruCache<String, MutableStateFlow<Boolean>>(50)

    @Composable
    fun rememberHasLyric(song: LSong): State<Boolean> {
        return remember { mutableStateOf(false) }.also { state ->
            LaunchedEffect(song) {
                listener.get(song.id)?.let { flow ->
                    flow.collectLatest { state.value = it }
                } ?: listener.put(song.id, MutableStateFlow(state.value)
                    .apply { collectLatest { state.value = it } })
                getLyric(song)
            }
//            DisposableEffect(Unit) {
//                onDispose {
//                    listener.remove(song.id)
//                }
//            }
        }
    }

    suspend fun hasLyric(song: LSong): Boolean = withContext(Dispatchers.IO) {
        getLyric(song) == null
    }

    suspend fun getLyric(song: LSong): Pair<String, String?>? = withContext(Dispatchers.IO) {
        val lyric = lyricSource.loadLyric(song)
        val result = lyric != null && lyric.first.isNotEmpty()

        listener.get(song.id)?.emit(result)

        return@withContext if (result) lyric else null
    }

    val currentLyric: UpdatableFlow<Pair<String, String?>?> =
        runtime.playingFlow.mapLatest { item ->
            val song = item?.let { Library.getSongOrNull(it.id) } ?: return@mapLatest null
            getLyric(song)
        }.toUpdatableFlow()

    val currentLyricEntry: CachedFlow<List<LyricEntry>?> = currentLyric.mapLatest { pair ->
        pair ?: return@mapLatest null
        LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
    }.toCachedFlow()

    val currentLyricLiveData = currentLyric.asLiveData()
}