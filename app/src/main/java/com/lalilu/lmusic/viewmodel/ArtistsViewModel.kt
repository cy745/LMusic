package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.extension.BaseSortStrategy
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.SortStrategy
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistsViewModel(
    lMediaRepo: LMediaRepository,
    lMusicSp: LMusicSp,
) : ViewModel() {
    private val artistIds = MutableStateFlow<List<String>>(emptyList())
    private val artistSource = lMediaRepo.artistsFlow.combine(artistIds) { artists, ids ->
        if (ids.isEmpty()) return@combine artists
        artists.filter { artist -> artist.name in ids }
    }

    private val sorter = object : ItemsBaseSorter<LArtist>(artistSource, lMusicSp) {
        override fun obtainStrategy(): SortStrategy<LArtist> =
            object : BaseSortStrategy<LArtist>() {

            }
    }

    val artists = sorter.output
        .mapLatest { it.values.flatten() }
        .toState(emptyList(), viewModelScope)

    fun updateByIds(
        ids: List<String>,
        sortFor: String = Sortable.SORT_FOR_ARTISTS,
        supportSortRules: List<SortRule>? = null,
        supportGroupRules: List<GroupRule>? = null,
        supportOrderRules: List<OrderRule>? = null
    ) = viewModelScope.launch {
        artistIds.emit(ids)
        sorter.updateSortFor(
            sortFor = sortFor,
            supportSortRules = supportSortRules,
            supportGroupRules = supportGroupRules,
            supportOrderRules = supportOrderRules
        )
    }
}