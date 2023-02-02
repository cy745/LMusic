package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.*
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.datastore.LibraryDataStore
import com.lalilu.lmusic.repository.LibraryRepository
import com.lalilu.lmusic.utils.extension.toState
import com.lalilu.lmusic.utils.extension.toUpdatableFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.util.*
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel @Inject constructor(
    private val libraryDataStore: LibraryDataStore,
    private val historyRepo: HistoryRepository,
    private val libraryRepo: LibraryRepository,
) : ViewModel() {
    val songs = libraryRepo.songsFlow.toState(emptyList(), viewModelScope)
    val artists = libraryRepo.artistsFlow.toState(emptyList(), viewModelScope)
    val albums = libraryRepo.albumsFlow.toState(emptyList(), viewModelScope)
    val genres = libraryRepo.genresFlow.toState(emptyList(), viewModelScope)
    val dictionaries = libraryRepo.dictionariesFlow.toState(emptyList(), viewModelScope)

    /**
     * 获取最近的播放记录
     *
     * 之前在 Composable 里用 CollectAsState 直接通过Flow获取了，
     * 但是那样每次Recompose的时候就会重新调用生成一个Flow，间接导致每次都会重新查询数据库
     */
    val lastPlayedStack = requirePlayHistory().toState(emptyList(), viewModelScope)
    val recentlyAdded = libraryRepo.getSongsFlow(15).toState(emptyList(), viewModelScope)
    private val randomRecommendsFlow = libraryRepo.getSongsFlow(15, true).toUpdatableFlow()
    val randomRecommends = randomRecommendsFlow.toState(emptyList(), viewModelScope)

    /**
     * 请求获取每日推荐歌曲
     */
    fun requireDailyRecommends(): List<LSong> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        libraryDataStore.apply {
            if (today == this.today.get()) {
                dailyRecommends.get()
                    .takeIf { it.isNotEmpty() }
                    ?.mapNotNull { LMedia.getSongOrNull(it) }
                    ?.let { return it }
            }

            return libraryRepo.getSongs(10, true).toList().also { list ->
                if (list.isNotEmpty()) {
                    this.today.set(today)
                    this.dailyRecommends.set(value = list.map { it.id })
                }
            }
        }
    }

    fun refreshRandomRecommend() {
        randomRecommendsFlow.requireUpdate()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun requirePlayHistory(): Flow<List<LSong>> {
        return historyRepo
            .getHistoriesFlow(5)
            .mapLatest { list ->
                list.mapNotNull { LMedia.getSongOrNull(it.contentId) }
            }
    }
}

val LocalLibraryVM = compositionLocalOf<LibraryViewModel> {
    error("LibraryViewModel hasn't not presented")
}