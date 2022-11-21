package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.*
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.datastore.LibraryDataStore
import com.lalilu.lmusic.datastore.SettingsDataStore
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
    private val settingsDataStore: SettingsDataStore,
    private val historyRepo: HistoryRepository,
    private val libraryRepo: LibraryRepository,
) : ViewModel() {
    private val sortRuleFlow = settingsDataStore.run {
        songsSortRule.flow().mapLatest {
            println("mapLatest:SortRule $it")
            SortRule.from(it)
        }
    }

    private val orderRuleFlow = settingsDataStore.run {
        songsOrderRule.flow().mapLatest {
            println("mapLatest:OrderRule $it")
            OrderRule.from(it)
        }
    }

    private val groupRuleFlow = settingsDataStore.run {
        songsGroupRule.flow().mapLatest {
            println("mapLatest:GroupRule $it")
            GroupRule.from(it)
        }
    }


    val supportSortRules = listOf(
        SortRule.Normal,
        SortRule.CreateTime,
        SortRule.ModifyTime,
        SortRule.Title,
        SortRule.SubTitle,
        SortRule.ContentType,
        SortRule.ItemsDuration
    )
    val supportOrderRules = listOf(
        OrderRule.ASC, OrderRule.DESC
    )
    val supportGroupRules = listOf(
        GroupRule.Normal,
        GroupRule.CreateTime,
        GroupRule.ModifyTime,
        GroupRule.TitleFirstLetter,
        GroupRule.SubTitleFirstLetter
    )

    val songs = libraryRepo.songsFlow
        .getSortedOutputFlow(sortRuleFlow, supportSortRules)
        .getOrderedOutputFlow(orderRuleFlow, supportOrderRules)
        .getGroupedOutputFlow(groupRuleFlow, supportGroupRules)
        .toState(emptyMap(), viewModelScope)
    val artists = libraryRepo.artistsFlow.toState(emptyList(), viewModelScope)
    val albums = libraryRepo.albumsFlow.toState(emptyList(), viewModelScope)
    val genres = libraryRepo.genresFlow.toState(emptyList(), viewModelScope)

    /**
     * 获取最近的播放记录
     *
     * 之前在 Composable 里用 CollectAsState 直接通过Flow获取了，
     * 但是那样每次Recompose的时候就会重新调用生成一个Flow，间接导致每次都会重新查询数据库
     */
    private val randomRecommendsFlow = Library.getSongsFlow(15, true).toUpdatableFlow()
    val lastPlayedStack = requirePlayHistory().toState(emptyList(), viewModelScope)
    val recentlyAdded = Library.getSongsFlow(15).toState(emptyList(), viewModelScope)
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
                    ?.mapNotNull { Library.getSongOrNull(it) }
                    ?.let { return it }
            }

            return Library.getSongs(num = 10, random = true).toList().also { list ->
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
            .getHistoriesFlow(20)
            .mapLatest { list ->
                list.distinctBy { it.contentId }
                    .mapNotNull { Library.getSongOrNull(it.contentId) }
                    .take(5)
            }
    }
}

val LocalLibraryVM = compositionLocalOf<LibraryViewModel> {
    error("LibraryViewModel hasn't not presented")
}