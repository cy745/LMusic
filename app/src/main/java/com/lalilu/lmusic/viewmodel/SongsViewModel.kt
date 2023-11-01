package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.BaseMatchable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.BaseSortStrategy
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.SortStrategy
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.repository.HistoryRepository
import com.lalilu.common.base.BaseSp
import com.lalilu.common.base.Playable
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.toState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(
    private val settingsSp: SettingsSp,
    private val historyRepo: HistoryRepository
) : ViewModel() {
    private val showAllFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val songIdsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val songsSource = songIdsFlow.flatMapLatest { mediaIds ->
        showAllFlow.flatMapLatest { showAll ->
            if (mediaIds.isEmpty() && showAll) {
                LMedia.getFlow<LSong>()
            } else {
                LMedia.flowMapBy<LSong>(mediaIds)
            }
        }
    }

    private val searcher = ItemsBaseSearcher(songsSource)
    private val sorter = object : ItemsBaseSorter<LSong>(searcher.output, settingsSp) {
        override fun obtainStrategy(): SortStrategy<LSong> = object : BaseSortStrategy<LSong>() {
            override fun sortWithFlow(
                rule: SortRule,
                source: Flow<List<LSong>>
            ): Flow<List<LSong>> {
                // 根据播放次数排序
                if (rule == SortRule.PlayCount) {
                    return historyRepo
                        .getHistoriesIdsMapWithCount()
                        .combine(source) { map, sources ->
                            sources.sortedByDescending { song -> map[song.id] }
                        }
                }

                // 根据最后播放时间排序
                if (rule == SortRule.LastPlayTime) {
                    return historyRepo
                        .getHistoriesIdsMapWithLastTime()
                        .combine(source) { map, sources ->
                            sources.sortedByDescending { song -> map[song.id] }
                        }
                }
                return super.sortWithFlow(rule, source)
            }

            override fun sortBy(rule: SortRule, list: List<LSong>): List<LSong> {
                list.forEach { it.prefixTemp = null }
                return super.sortBy(rule, list)
            }
        }
    }

    val songsState: State<Map<GroupIdentity, List<Playable>>> =
        sorter.output.toState(emptyMap(), viewModelScope)

    fun updateByIds(
        songIds: List<String>,
        showAll: Boolean = false,
        sortFor: String = Sortable.SORT_FOR_SONGS,
        supportSortRules: List<SortRule>? = null,
        supportGroupRules: List<GroupRule>? = null,
        supportOrderRules: List<OrderRule>? = null
    ) = viewModelScope.launch {
        songIdsFlow.value = songIds
        showAllFlow.value = showAll
        sorter.updateSortFor(
            sortFor = sortFor,
            supportSortRules = supportSortRules,
            supportGroupRules = supportGroupRules,
            supportOrderRules = supportOrderRules
        )
    }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
open class ItemsBaseSearcher<T : BaseMatchable>(
    sourceFlow: Flow<List<T>>
) {
    private val keywordStr = MutableStateFlow("")
    private val keywords = keywordStr
        .debounce { if (it.isEmpty()) 0 else 200 }
        .mapLatest {
            if (it.isEmpty()) return@mapLatest emptyList()
            it.trim().uppercase().split(' ')
        }

    val output = sourceFlow.combine(keywords) { items, keywordList ->
        if (keywordList.isEmpty()) return@combine items
        items.filter { item -> keywordList.all { item.matchStr.contains(it) } }
    }

    fun search(keyword: String) {
        keywordStr.value = keyword
    }

    fun clear() {
        keywordStr.value = ""
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
open class ItemsBaseSorter<T : Sortable>(
    sourceFlow: Flow<List<T>>,
    private val sp: BaseSp
) {
    private val strategy: SortStrategy<T> by lazy { obtainStrategy() }
    open fun obtainStrategy(): SortStrategy<T> = BaseSortStrategy()

    private val supportSortRuleFlow: MutableStateFlow<List<SortRule>>
            by lazy { MutableStateFlow(emptyList()) }
    private val supportGroupRuleFlow: MutableStateFlow<List<GroupRule>>
            by lazy { MutableStateFlow(emptyList()) }
    private val supportOrderRuleFlow: MutableStateFlow<List<OrderRule>>
            by lazy { MutableStateFlow(emptyList()) }

    private val sortForFlow: MutableStateFlow<String> = MutableStateFlow(Sortable.SORT_FOR_SONGS)

    private val sortRuleFlow: Flow<SortRule> = sortForFlow.flatMapLatest { sortFor ->
        sp.obtain<String>("${sortFor}_SORT_RULE")
            .flow(true)
            .flatMapLatest { str ->
                supportSortRuleFlow.mapLatest { list ->
                    list.firstOrNull { it.name == str } ?: SortRule.Normal
                }
            }
    }
    private val orderRuleFlow: Flow<OrderRule> = sortForFlow.flatMapLatest { sortFor ->
        sp.obtain<String>("${sortFor}_ORDER_RULE")
            .flow(true)
            .flatMapLatest { str ->
                supportOrderRuleFlow.mapLatest { list ->
                    list.firstOrNull { it.name == str } ?: OrderRule.Normal
                }
            }
    }
    private val groupRuleFlow: Flow<GroupRule> = sortForFlow.flatMapLatest { sortFor ->
        sp.obtain<String>("${sortFor}_GROUP_RULE")
            .flow(true)
            .flatMapLatest { str ->
                supportGroupRuleFlow.mapLatest { list ->
                    list.firstOrNull { it.name == str } ?: GroupRule.Normal
                }
            }
    }

    val output: Flow<Map<GroupIdentity, List<T>>> = strategy.sortFor(
        inputFlow = sourceFlow,
        sortRuleFlow = sortRuleFlow,
        orderRuleFlow = orderRuleFlow,
        groupRuleFlow = groupRuleFlow,
        supportSortRules = supportSortRuleFlow,
        supportOrderRules = supportOrderRuleFlow,
        supportGroupRules = supportGroupRuleFlow
    )

    fun updateSortFor(
        sortFor: String,
        supportSortRules: List<SortRule>?,
        supportGroupRules: List<GroupRule>?,
        supportOrderRules: List<OrderRule>?
    ) {
        sortForFlow.value = sortFor
        supportSortRuleFlow.value = supportSortRules ?: emptyList()
        supportGroupRuleFlow.value = supportGroupRules ?: emptyList()
        supportOrderRuleFlow.value = supportOrderRules ?: emptyList()
    }
}