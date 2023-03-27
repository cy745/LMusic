package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.lmedia.entity.BaseMatchable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.BaseSortStrategy
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.SortStrategy
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmedia.extension.sortFor
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.repository.LMediaRepository
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
    lMusicSp: LMusicSp,
    lMediaRepo: LMediaRepository,
    private val historyRepo: HistoryRepository
) : ViewModel() {
    private val showAllItem: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val songIdsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val songsSource = showAllItem
        .flatMapLatest { if (it) lMediaRepo.allSongsFlow else lMediaRepo.songsFlow }
        .flatMapLatest { songs ->
            songIdsFlow.mapLatest {
                if (it.isEmpty()) return@mapLatest songs
                songs.filter { song -> song.id in it }
            }
        }

    val searcher = ItemsBaseSearcher(songsSource)
    val sorter = object : ItemsBaseSorter<LSong>(searcher.output, lMusicSp) {
        override fun obtainStrategy(): SortStrategy<LSong> = object : BaseSortStrategy<LSong>() {
            override fun Flow<List<LSong>>.getSortedOutputFlow(
                sortRule: Flow<SortRule>,
                supportSortRules: List<SortRule>
            ): Flow<List<LSong>> {
                return sortRule.flatMapLatest { rule ->
                    if (!supportSortRules.contains(rule)) return@flatMapLatest this

                    // 根据播放次数排序
                    if (rule == SortRule.PlayCount) {
                        return@flatMapLatest historyRepo.getHistoriesWithCount(Int.MAX_VALUE)
                            .flatMapLatest { histories ->
                                this.mapLatest { sources ->
                                    sources.sortedBy { song ->
                                        (histories.entries.firstOrNull { it.key.contentId == song.id }
                                            ?.value ?: 0)
                                            .also { song.prefixTemp = it.toString() }
                                    }.reversed()
                                }
                            }
                    }

                    // 根据最后播放时间排序
                    if (rule == SortRule.LastPlayTime) {
                        return@flatMapLatest historyRepo.getHistoriesFlow(Int.MAX_VALUE)
                            .flatMapLatest { histories ->
                                mapLatest { sources ->
                                    sources.sortedBy { song ->
                                        histories.indexOfFirst { it.contentId == song.id }
                                            .takeIf { it != -1 } ?: Int.MAX_VALUE
                                    }
                                }
                            }
                    }

                    // 根据其他规则排序
                    mapLatest { list ->
                        list.forEach { it.prefixTemp = null }
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

    val songsState = sorter.output.toState(emptyMap(), viewModelScope)

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
        showAllItem.value = showAll
        songIdsFlow.value = songIds
        sorter.updateSortFor(sortFor)
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
    private val lMusicSp: LMusicSp
) {
    private val strategy: SortStrategy<T> by lazy { obtainStrategy() }
    val supportSortRules: List<SortRule> by lazy { obtainSupportSortRules() }
    val supportOrderRules: List<OrderRule> by lazy { obtainSupportOrderRules() }
    val supportGroupRules: List<GroupRule> by lazy { obtainSupportGroupRules() }

    open fun obtainStrategy(): SortStrategy<T> = BaseSortStrategy()
    open fun obtainSupportSortRules(): List<SortRule> = listOf(
        SortRule.Normal,
        SortRule.CreateTime,
        SortRule.ModifyTime,
        SortRule.Title,
        SortRule.SubTitle,
        SortRule.ContentType,
        SortRule.ItemsDuration,
        SortRule.FileSize,
        SortRule.PlayCount,
        SortRule.LastPlayTime
    )

    open fun obtainSupportOrderRules(): List<OrderRule> = listOf(
        OrderRule.Normal,
        OrderRule.Reverse,
        OrderRule.Shuffle
    )

    open fun obtainSupportGroupRules(): List<GroupRule> = listOf(
        GroupRule.Normal,
        GroupRule.CreateTime,
        GroupRule.ModifyTime,
        GroupRule.TitleFirstLetter,
        GroupRule.SubTitleFirstLetter,
        GroupRule.PinYinFirstLetter
    )

    private val sortForFlow: MutableStateFlow<String> = MutableStateFlow(Sortable.SORT_FOR_SONGS)

    private val sortRuleFlow: Flow<SortRule> = sortForFlow.flatMapLatest {
        lMusicSp.stringSp("${it}_SORT_RULE")
            .flow(true)
            .mapLatest(SortRule::from)
    }
    private val orderRuleFlow: Flow<OrderRule> = sortForFlow.flatMapLatest {
        lMusicSp.stringSp("${it}_ORDER_RULE")
            .flow(true)
            .mapLatest(OrderRule::from)
    }
    private val groupRuleFlow: Flow<GroupRule> = sortForFlow.flatMapLatest {
        lMusicSp.stringSp("${it}_GROUP_RULE")
            .flow(true)
            .mapLatest(GroupRule::from)
    }

    val output: Flow<Map<Any, List<T>>> = strategy.sortFor(
        inputFlow = sourceFlow,
        sortRuleFlow = sortRuleFlow,
        orderRuleFlow = orderRuleFlow,
        groupRuleFlow = groupRuleFlow,
        supportSortRules = supportSortRules,
        supportOrderRules = supportOrderRules,
        supportGroupRules = supportGroupRules
    )

    fun updateSortFor(sortFor: String) {
        sortForFlow.value = sortFor
    }
}