package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.stringPreferencesKey
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
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.datastore.SettingsDataStore
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
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
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

    private val songListFlow: MutableStateFlow<List<LSong>> = MutableStateFlow(emptyList())
    private val sortForFlow: MutableStateFlow<String> = MutableStateFlow(Sortable.SORT_FOR_SONGS)

    private val sortRuleFlow = sortForFlow.flatMapLatest {
        settingsDataStore.run {
            stringPreferencesKey("${it}_SORT_RULE").flow().mapLatest { SortRule.from(it) }
        }
    }
    private val orderRuleFlow = sortForFlow.flatMapLatest {
        settingsDataStore.run {
            stringPreferencesKey("${it}_ORDER_RULE").flow().mapLatest { OrderRule.from(it) }
        }
    }
    private val groupRuleFlow = sortForFlow.flatMapLatest {
        settingsDataStore.run {
            stringPreferencesKey("${it}_GROUP_RULE").flow().mapLatest { GroupRule.from(it) }
        }
    }
    val songsState: State<Map<Any, List<LSong>>> =
        songListFlow.getSortedOutputFlow(sortRuleFlow, supportSortRules)
            .getOrderedOutputFlow(orderRuleFlow, supportOrderRules)
            .getGroupedOutputFlow(groupRuleFlow, supportGroupRules)
            .toState(emptyMap(), viewModelScope)

    fun updateByLibrary() {
        updateBySongs(Library.getSongs())
    }

    fun updateByIds(songIds: List<String>) {
        updateBySongs(songIds.mapNotNull { Library.getSongOrNull(it) })
    }

    fun updateBySongs(songs: List<LSong>) = viewModelScope.launch {
        songListFlow.emit(songs)
    }
}

val LocalSongsVM = compositionLocalOf<SongsViewModel> {
    error("LocalSongsVM hasn't not presented")
}