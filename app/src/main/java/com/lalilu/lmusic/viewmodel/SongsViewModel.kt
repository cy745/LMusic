package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmedia.extension.getGroupedOutputFlow
import com.lalilu.lmedia.extension.getOrderedOutputFlow
import com.lalilu.lmedia.extension.getSortedOutputFlow
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(
    private val lMusicSp: LMusicSp,
    private val lMediaRepo: LMediaRepository
) : ViewModel() {
    val supportSortRules = listOf(
        SortRule.Normal,
        SortRule.CreateTime,
        SortRule.ModifyTime,
        SortRule.Title,
        SortRule.SubTitle,
        SortRule.ContentType,
        SortRule.ItemsDuration,
        SortRule.FileSize
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

    private val showAllItem: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val songIdsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val sortForFlow: MutableStateFlow<String> = MutableStateFlow(Sortable.SORT_FOR_SONGS)

    private val sortRuleFlow = sortForFlow.flatMapLatest {
        lMusicSp.stringSp("${it}_SORT_RULE")
            .flow(true)
            .mapLatest(SortRule::from)
    }
    private val orderRuleFlow = sortForFlow.flatMapLatest {
        lMusicSp.stringSp("${it}_ORDER_RULE")
            .flow(true)
            .mapLatest(OrderRule::from)
    }
    private val groupRuleFlow = sortForFlow.flatMapLatest {
        lMusicSp.stringSp("${it}_GROUP_RULE")
            .flow(true)
            .mapLatest(GroupRule::from)
    }

    val songsState: State<Map<Any, List<LSong>>> = showAllItem
        .flatMapLatest { if (it) lMediaRepo.allSongsFlow else lMediaRepo.songsFlow }
        .flatMapLatest { songs ->
            songIdsFlow.mapLatest {
                if (it.isEmpty()) return@mapLatest songs
                songs.filter { song -> song.id in it }
            }
        }
        .getSortedOutputFlow(sortRuleFlow, supportSortRules)
        .getOrderedOutputFlow(orderRuleFlow, supportOrderRules)
        .getGroupedOutputFlow(groupRuleFlow, supportGroupRules)
        .toState(emptyMap(), viewModelScope)

    fun updateBySongs(
        songs: List<LSong>,
        showAll: Boolean = false,
        sortFor: String = Sortable.SORT_FOR_SONGS
    ) {
        updateByIds(
            songIds = songs.map { it.id },
            showAll = showAll,
            sortFor = sortFor
        )
    }

    fun updateByIds(
        songIds: List<String>,
        showAll: Boolean = false,
        sortFor: String = Sortable.SORT_FOR_SONGS
    ) = viewModelScope.launch {
        showAllItem.emit(showAll)
        songIdsFlow.emit(songIds)
        sortForFlow.emit(sortFor)
    }
}