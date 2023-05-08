package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.BaseSortStrategy
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.SortStrategy
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModel(
    lMediaRepo: LMediaRepository,
    private val settingsSp: SettingsSp
) : ViewModel() {
    private val albumIds = MutableStateFlow<List<String>>(emptyList())
    private val albumSource = lMediaRepo.albumsFlow.combine(albumIds) { albums, ids ->
        if (ids.isEmpty()) return@combine albums
        albums.filter { album -> album.id in ids }
    }

    private val sorter = object : ItemsBaseSorter<LAlbum>(albumSource, settingsSp) {
        override fun obtainStrategy(): SortStrategy<LAlbum> = object : BaseSortStrategy<LAlbum>() {
            override fun sortBy(rule: SortRule, list: List<LAlbum>): List<LAlbum> {
                return when (rule) {
                    SortRule.Title -> list.sortedBy { it.requireTitle() }
                    SortRule.FileSize -> list.sortedByDescending { it.requireFileSize() }
                    SortRule.ItemsCount -> list.sortedByDescending { it.requireItemsCount() }
                    SortRule.ItemsDuration -> list.sortedByDescending { it.requireItemsDuration() }
                    else -> list
                }
            }
        }
    }

    val albums = sorter.output
        .mapLatest { it.values.flatten() }
        .toState(emptyList(), viewModelScope)

    fun updateByIds(
        ids: List<String>,
        sortFor: String = Sortable.SORT_FOR_SONGS,
        supportSortRules: List<SortRule>? = null,
        supportGroupRules: List<GroupRule>? = null,
        supportOrderRules: List<OrderRule>? = null
    ) = viewModelScope.launch {
        albumIds.emit(ids)
        sorter.updateSortFor(
            sortFor = sortFor,
            supportSortRules = supportSortRules,
            supportGroupRules = supportGroupRules,
            supportOrderRules = supportOrderRules
        )
    }
}