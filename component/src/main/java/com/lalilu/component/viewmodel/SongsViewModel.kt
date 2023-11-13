package com.lalilu.component.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.common.base.BaseSp
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.BaseMatchable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupAction
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.GroupRuleDynamic
import com.lalilu.lmedia.extension.GroupRuleStatic
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.OrderAction
import com.lalilu.lmedia.extension.OrderRuleDynamic
import com.lalilu.lmedia.extension.OrderRuleStatic
import com.lalilu.lmedia.extension.SortAction
import com.lalilu.lmedia.extension.SortRuleDynamic
import com.lalilu.lmedia.extension.SortRuleStatic
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class SongsSp(private val context: Context) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences(
            context.packageName + "_SONGS",
            Application.MODE_PRIVATE
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(val sp: SongsSp) : ViewModel() {
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
    private val sorter = ItemsBaseSorter(sourceFlow = searcher.output, sp = sp)
    val output = sorter.output.toState(emptyMap(), viewModelScope)

    fun updateByIds(
        songIds: List<String>,
        showAll: Boolean = false,
        sortFor: String = Sortable.SORT_FOR_SONGS,
        supportSortRules: List<ListAction>? = null,
    ) = viewModelScope.launch {
        songIdsFlow.value = songIds
        showAllFlow.value = showAll
        sorter.updateSortFor(
            sortFor = sortFor,
            supportSortRules = supportSortRules,
        )
    }
}

inline fun <reified T> Collection<Any>.findInstance(check: (T) -> Boolean): T? {
    return this.filterIsInstance(T::class.java)
        .firstOrNull(check)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsBaseSorter<T : Sortable>(
    sourceFlow: Flow<List<T>>,
    private val sp: BaseSp
) {
    private val supportListActionFlow = MutableStateFlow<Set<ListAction>>(emptySet())
    private val sortForFlow = MutableStateFlow(Sortable.SORT_FOR_SONGS)

    private val sortRuleFlow: Flow<SortAction?> = sortForFlow.flatMapLatest { sortFor ->
        supportListActionFlow.flatMapLatest { listActions ->
            sp.obtain<String>("${sortFor}_SORT_RULE")
                .flow(true)
                .mapLatest { key ->
                    if (key == null) return@mapLatest SortRuleStatic.Normal
                    listActions.findInstance<SortAction> { it::class.java.name == key }
                        ?: SortRuleStatic.Normal
                }
        }
    }
    private val orderRuleFlow: Flow<OrderAction> = sortForFlow.flatMapLatest { sortFor ->
        supportListActionFlow.flatMapLatest { listActions ->
            sp.obtain<String>("${sortFor}_ORDER_RULE")
                .flow(true)
                .mapLatest { key ->
                    if (key == null) return@mapLatest OrderRuleStatic.Normal
                    listActions.findInstance<OrderAction> { it::class.java.name == key }
                        ?: OrderRuleStatic.Normal
                }
        }
    }
    private val groupRuleFlow: Flow<GroupAction> = sortForFlow.flatMapLatest { sortFor ->
        supportListActionFlow.flatMapLatest { listActions ->
            sp.obtain<String>("${sortFor}_GROUP_RULE")
                .flow(true)
                .mapLatest { key ->
                    if (key == null) return@mapLatest GroupRuleStatic.Normal
                    listActions.findInstance<GroupAction> { it::class.java.name == key }
                        ?: GroupRuleStatic.Normal
                }
        }
    }

    private fun sortWithFlow(rule: SortAction?, source: Flow<List<T>>): Flow<List<T>> {
        return when (rule) {
            is SortRuleStatic -> source.mapLatest { rule.sort(it) }
            is SortRuleDynamic -> rule.sort(source)
            else -> source
        }
    }

    private fun orderWithFlow(rule: OrderAction?, source: Flow<List<T>>): Flow<List<T>> {
        return when (rule) {
            is OrderRuleStatic -> source.mapLatest { rule.order(it) }
            is OrderRuleDynamic -> rule.order(source)
            else -> source
        }
    }

    private fun groupWithFlow(
        rule: GroupAction?,
        source: Flow<List<T>>
    ): Flow<Map<GroupIdentity, List<T>>> {
        return when (rule) {
            is GroupRuleStatic -> source.mapLatest { rule.group(it) }
            is GroupRuleDynamic -> rule.group(source)
            else -> source.mapLatest { mapOf(GroupIdentity.None to it) }
        }
    }

    private fun Flow<List<T>>.getSortedOutputFlow(
        sortRule: Flow<SortAction?>,
    ): Flow<List<T>> = sortRule.flatMapLatest { rule ->
        sortWithFlow(rule, this)
    }

    private fun Flow<List<T>>.getOrderedOutputFlow(
        orderRule: Flow<OrderAction?>,
    ): Flow<List<T>> = orderRule.flatMapLatest { rule ->
        orderWithFlow(rule, this)
    }

    private fun Flow<List<T>>.getGroupedOutputFlow(
        groupRule: Flow<GroupAction?>,
    ): Flow<Map<GroupIdentity, List<T>>> = groupRule.flatMapLatest { rule ->
        groupWithFlow(rule, this)
    }

    val output = sourceFlow.getSortedOutputFlow(sortRuleFlow)
        .getOrderedOutputFlow(orderRuleFlow)
        .getGroupedOutputFlow(groupRuleFlow)

    fun updateSortFor(
        sortFor: String,
        supportSortRules: Collection<ListAction>?,
    ) {
        sortForFlow.value = sortFor
        supportListActionFlow.value = supportSortRules?.toSet() ?: emptySet()
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