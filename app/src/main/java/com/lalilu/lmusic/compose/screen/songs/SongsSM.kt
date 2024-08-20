package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.BaseMatchable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal sealed interface SongsScreenAction {
    data object ToggleSortPanel : SongsScreenAction
    data object LocaleToPlayingItem : SongsScreenAction
}

internal sealed interface SongsScreenEvent {
    data class ScrollToItem(val key: String) : SongsScreenEvent
}

internal class SongsSM(
    private val mediaIds: List<String>
) : ScreenModel {
    // 持久化元素的状态
    val showSortPanel = mutableStateOf(false)

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

            else -> {}
        }
    }

    // 数据流
    private fun flow(): Flow<List<LSong>> {
        return if (mediaIds.isEmpty()) LMedia.getFlow<LSong>()
        else LMedia.flowMapBy<LSong>(mediaIds)
    }

    val searcher = ItemSearcher(flow())

    //    val sorter = ItemSorter(searcher.output)
    val grouper = ItemGrouper(searcher.output)
    val songs = grouper.output.toState(emptyMap(), screenModelScope)
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

internal class ItemGrouper<T>(
    sourceFlow: Flow<List<T>>,
) {
    private val groupByFunc = MutableStateFlow<((T) -> Any)?>(null)

    val output: Flow<Map<Any, List<T>>> = sourceFlow.combine(groupByFunc) { source, func ->
        if (func == null) return@combine mapOf("" to source)
        source.groupBy { func(it) }
    }

    fun setGroupByFunc(func: (T) -> Any) {
        groupByFunc.value = func
    }
}

internal class ItemSorter<T : Sortable>(
    sourceFlow: Flow<List<T>>,
) {
    private val sortByFunc = MutableStateFlow<((T) -> Comparable<*>)?>(null)

    val output: Flow<List<T>> = sourceFlow.combine(sortByFunc) { source, func ->
        if (func == null) return@combine source
        source
    }

    fun setSortByFunc(func: (T) -> Comparable<*>) {
        sortByFunc.value = func
    }
}
