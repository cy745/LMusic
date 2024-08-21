package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lalilu.common.base.BaseSp
import com.lalilu.component.extension.toState
import com.lalilu.component.viewmodel.SongsSp
import com.lalilu.component.viewmodel.findInstance
import com.lalilu.lhistory.SortRuleLastPlayTime
import com.lalilu.lhistory.SortRulePlayCount
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.BaseMatchable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortDynamicAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

internal sealed interface SongsScreenAction {
    data object ToggleSortPanel : SongsScreenAction
    data object LocaleToPlayingItem : SongsScreenAction
    data class SearchFor(val keyword: String) : SongsScreenAction
    data class SelectSortAction(val action: ListAction) : SongsScreenAction
}

internal sealed interface SongsScreenEvent {
    data class ScrollToItem(val key: String) : SongsScreenEvent
}

internal class SongsSM(
    private val mediaIds: List<String>,
) : ScreenModel {
    // 持久化元素的状态
    val showSortPanel = mutableStateOf(false)
    val supportSortActions = setOf(
        SortStaticAction.Normal,
        SortStaticAction.Title,
        SortStaticAction.AddTime,
        SortStaticAction.Shuffle,
        SortStaticAction.Duration,
        SortRulePlayCount,
        SortRuleLastPlayTime,
//        KoinJavaComponent.getOrNull(ListAction::class.java, named("SortRulePlayCount")),
//        KoinJavaComponent.getOrNull(ListAction::class.java, named("SortRuleLastPlayTime"))
    ).filterNotNull()
        .toSet()

    // 事件流
    private val eventFlow = MutableSharedFlow<SongsScreenEvent>()
    fun event(): SharedFlow<SongsScreenEvent> = eventFlow
    fun action(action: SongsScreenAction) = screenModelScope.launch {
        when (action) {
            SongsScreenAction.LocaleToPlayingItem -> {
                eventFlow.emit(SongsScreenEvent.ScrollToItem(""))
            }

            SongsScreenAction.ToggleSortPanel -> {
                showSortPanel.value = !showSortPanel.value
            }

            is SongsScreenAction.SearchFor -> {
                searcher.search(action.keyword)
            }

            is SongsScreenAction.SelectSortAction -> {
                sorter.selectSortAction(action.action)
            }

            else -> {}
        }
    }

    // 数据流
    private fun flow(): Flow<List<LSong>> {
        return if (mediaIds.isEmpty()) LMedia.getFlow<LSong>()
        else LMedia.flowMapBy<LSong>(mediaIds)
    }

    val searcher = ItemSearcher(flow())
    val sorter = ItemSorter(searcher.output, supportSortActions)
    val songs = sorter.output.toState(emptyMap(), screenModelScope)
}

internal class ItemSearcher<T : BaseMatchable>(
    sourceFlow: Flow<List<T>>
) {
    private val keywordStr = MutableStateFlow("")
    private val keywordFlow = keywordStr.map {
        when {
            it.isBlank() -> emptyList()
            it.contains(' ') -> it.split(' ')
            else -> listOf(it)
        }
    }

    val output: Flow<List<T>> = sourceFlow.combine(keywordFlow) { source, keywords ->
        source.filter { item -> keywords.all { item.matchStr.contains(it) } }
    }

    fun search(keyword: String) {
        keywordStr.value = keyword
    }

    fun clear() {
        keywordStr.value = ""
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class ItemSorter<T : Sortable>(
    sourceFlow: Flow<List<T>>,
    private val supportSortActions: Set<ListAction>,
) {
    private val baseSp: BaseSp by inject(SongsSp::class.java)
    private val sortActionKey = baseSp.obtain("SONGS_SORT_RULE_KEY", "")

    private val sortActionFlow = sortActionKey
        .flow(true)
        .mapLatest { key ->
            supportSortActions.findInstance<ListAction> { it::class.java.name == key }
                ?: SortStaticAction.Normal
        }

    val output = sortActionFlow.flatMapLatest { action ->
        when (action) {
            is SortStaticAction -> sourceFlow.mapLatest { action.doSort(it, false) }
            is SortDynamicAction -> action.doSort(sourceFlow, false)
            else -> flowOf(emptyMap())
        }
    }

    fun selectSortAction(action: ListAction) {
        sortActionKey.value = action::class.java.name
    }

    fun isSortActionSelected(action: ListAction): Boolean {
        return sortActionKey.value == action::class.java.name
    }
}
