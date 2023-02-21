package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmedia.extension.getGroupedOutputFlow
import com.lalilu.lmedia.extension.getOrderedOutputFlow
import com.lalilu.lmedia.extension.getSortedOutputFlow
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.extension.toState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SongsViewModel @Inject constructor(
    private val lMusicSp: LMusicSp
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

    private val songListFlow: MutableStateFlow<List<LSong>> = MutableStateFlow(emptyList())
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
    val songsState: State<Map<Any, List<LSong>>> =
        songListFlow.getSortedOutputFlow(sortRuleFlow, supportSortRules)
            .getOrderedOutputFlow(orderRuleFlow, supportOrderRules)
            .getGroupedOutputFlow(groupRuleFlow, supportGroupRules)
            .toState(emptyMap(), viewModelScope)

    fun updateByLibrary() {
        updateBySongs(
            songs = LMedia.getSongs(),
            sortFor = Sortable.SORT_FOR_SONGS
        )
    }

    fun updateByIds(
        songIds: List<String>,
        sortFor: String = Sortable.SORT_FOR_SONGS
    ) {
        updateBySongs(
            songs = songIds.mapNotNull { LMedia.getSongOrNull(it) },
            sortFor = sortFor
        )
    }

    fun updateBySongs(
        songs: List<LSong>,
        sortFor: String = Sortable.SORT_FOR_SONGS
    ) = viewModelScope.launch {
        songListFlow.emit(songs)
        sortForFlow.emit(sortFor)
    }
}

val LocalSongsVM = compositionLocalOf<SongsViewModel> {
    error("LocalSongsVM hasn't not presented")
}