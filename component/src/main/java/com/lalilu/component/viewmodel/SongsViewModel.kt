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
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortDynamicAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    private val sortRuleFlow = sortForFlow.flatMapLatest { sortFor ->
        supportListActionFlow.flatMapLatest { supportActions ->
            sp.obtain<String>("${sortFor}_SORT_RULE")
                .flow(true)
                .mapLatest { key ->
                    key?.let { supportActions.findInstance<ListAction> { it::class.java.name == key } }
                        ?: SortStaticAction.Normal
                }
        }
    }

    private val reverseOrderFlow = sortForFlow.flatMapLatest { sortFor ->
        sp.obtain<Boolean>("${sortFor}_SORT_RULE_REVERSE_ORDER", false)
            .flow(true)
            .mapLatest { it ?: false }
    }

    private val flattenOverrideFlow = sortForFlow.flatMapLatest { sortFor ->
        sp.obtain<Boolean>("${sortFor}_SORT_RULE_FLATTEN_OVERRIDE", false)
            .flow(true)
            .mapLatest { it ?: false }
    }

    val output: Flow<Map<GroupIdentity, List<T>>> = sortRuleFlow.flatMapLatest { action ->
        reverseOrderFlow.flatMapLatest { reverse ->
            when (action) {
                is SortStaticAction -> sourceFlow.mapLatest { action.doSort(it, reverse) }
                is SortDynamicAction -> action.doSort(sourceFlow, reverse)
                else -> flowOf(emptyMap())
            }
        }
    }.flatMapLatest { result ->
        flattenOverrideFlow.mapLatest {
            if (it) mapOf(GroupIdentity.None to result.values.flatten()) else result
        }
    }

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