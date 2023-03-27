package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.BaseSortStrategy
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.SortStrategy
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModel(
    lMediaRepo: LMediaRepository,
    private val historyRepo: HistoryRepository,
    private val lMusicSp: LMusicSp
) : ViewModel() {
    private val albumIds = MutableStateFlow<List<String>>(emptyList())
    private val albumSource = lMediaRepo.albumsFlow.combine(albumIds) { albums, ids ->
        if (ids.isEmpty()) return@combine albums
        albums.filter { album -> album.id in ids }
    }

    val sorter = object : ItemsBaseSorter<LAlbum>(albumSource, lMusicSp) {
        override fun obtainSupportSortRules(): List<SortRule> = listOf(
            SortRule.Normal,
            SortRule.Title,
            SortRule.ItemsCount,
            SortRule.LastPlayTime,
            SortRule.PlayCount
        )

        override fun obtainSupportGroupRules(): List<GroupRule> = emptyList()

        override fun obtainStrategy(): SortStrategy<LAlbum> = object : BaseSortStrategy<LAlbum>() {
            override fun Flow<List<LAlbum>>.getSortedOutputFlow(
                sortRule: Flow<SortRule>,
                supportSortRules: List<SortRule>
            ): Flow<List<LAlbum>> {
                return sortRule.flatMapLatest { rule ->
                    if (!supportSortRules.contains(rule)) return@flatMapLatest this

                    // 根据播放次数排序
                    if (rule == SortRule.PlayCount) {
                        return@flatMapLatest historyRepo.getHistoriesWithCount(Int.MAX_VALUE)
                            .flatMapLatest { histories ->
                                this.mapLatest { sources ->
                                    sources.sortedBy { album ->
                                        histories.entries.firstOrNull { it.key.contentId == album.id }
                                            ?.value ?: 0
                                    }.reversed()
                                }
                            }
                    }

                    // 根据最后播放时间排序
                    if (rule == SortRule.LastPlayTime) {
                        return@flatMapLatest historyRepo.getHistoriesFlow(Int.MAX_VALUE)
                            .flatMapLatest { histories ->
                                mapLatest { sources ->
                                    sources.sortedBy { album ->
                                        histories.indexOfFirst { it.contentId == album.id }
                                            .takeIf { it != -1 } ?: Int.MAX_VALUE
                                    }
                                }
                            }
                    }

                    // 根据其他规则排序
                    mapLatest { list ->
                        when (rule) {
                            SortRule.Normal -> list
                            SortRule.Title -> list.sortedBy { it.requireTitle() }
                            SortRule.SubTitle -> list.sortedBy { it.requireSubTitle() }
                            SortRule.CreateTime -> list.sortedBy { it.requireCreateTime() }
                            SortRule.ModifyTime -> list.sortedBy { it.requireModifyTime() }
                            SortRule.ContentType -> list.sortedBy { it.requireContentType() }
                            SortRule.ItemsDuration -> list.sortedBy { it.requireItemsDuration() }
                            SortRule.FileSize -> list.sortedBy { it.requireFileSize() }
                            else -> list
                        }
                    }
                }
            }
        }
    }

    val albums = sorter.output
        .mapLatest { it.values.flatten() }
        .toState(emptyList(), viewModelScope)

    fun updateByIds(
        ids: List<String>,
        sortFor: String = Sortable.SORT_FOR_SONGS
    ) = viewModelScope.launch {
        albumIds.emit(ids)
        sorter.updateSortFor(sortFor)
    }
}