package com.lalilu.lmusic.repository

import androidx.compose.runtime.*
import androidx.lifecycle.asLiveData
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.*
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
            item?.let { LMedia.getSongOrNull(it.id) }
                ?.let { getLyric(it) }
        }.toUpdatableFlow()

    val currentLyricEntry: CachedFlow<List<LyricEntry>?> = currentLyric.mapLatest { pair ->
        pair ?: return@mapLatest null
        LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
    }.toCachedFlow()

    @OptIn(FlowPreview::class)
    val currentLyricSentence: Flow<String?> = currentLyricEntry.flatMapLatest { lyrics ->
        runtime.positionFlow.mapLatest {
            findShowLine(lyrics, it + 500)
        }.distinctUntilChanged()
            .mapLatest { lyrics?.getOrNull(it)?.text }
    }.debounce(100)

    val currentLyricLiveData = currentLyric.asLiveData()
}