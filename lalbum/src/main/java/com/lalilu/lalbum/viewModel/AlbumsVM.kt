package com.lalilu.lalbum.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.common.MviWithIntent
import com.lalilu.common.ext.requestFor
import com.lalilu.common.mviImplWithIntent
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.sortable.SortAction
import com.lalilu.lmedia.extension.sortable.SortConfig
import com.lalilu.lmedia.extension.sortable.SortManager
import com.lalilu.lmedia.extension.sortable.doSortState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@Stable
@Immutable
data class AlbumsState(
    val albumIds: List<String> = emptyList(),

    // control flags
    val showText: Boolean = false,
    val showSortPanel: Boolean = false,
    val showSearcherPanel: Boolean = false,

    // control params
    val searchKeyWord: String = "",
) {
    val distinctKey: Int =
        albumIds.hashCode() + searchKeyWord.hashCode()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAlbumsFlow(): Flow<List<LAlbum>> {
        val source = LMedia.getFlow<LAlbum>()

        val keywords: List<String> = when {
            searchKeyWord.isBlank() -> emptyList()
            searchKeyWord.contains(' ') -> searchKeyWord.split(' ')
            else -> listOf(searchKeyWord)
        }

        return source.mapLatest { flow ->
            flow.filter { item -> keywords.all { item.getMatchStr().contains(it) } }
        }
    }
}

sealed interface AlbumsEvent {
    data class ScrollToItem(val key: Any) : AlbumsEvent
}

sealed interface AlbumsAction {
    data object ToggleSortPanel : AlbumsAction
    data object ToggleSearcherPanel : AlbumsAction
    data object ToggleShowText : AlbumsAction

    data object HideSortPanel : AlbumsAction
    data object HideSearcherPanel : AlbumsAction
    data object HideShowText : AlbumsAction

    data object LocaleToPlayingItem : AlbumsAction
    data class SearchFor(val keyword: String) : AlbumsAction
    data class SelectSortAction(val action: SortAction) : AlbumsAction
    data class UpdateSortConfig(val config: SortConfig) : AlbumsAction
}

@KoinViewModel
class AlbumsVM(
    val albumIds: List<String>
) : ViewModel(),
    MviWithIntent<AlbumsState, AlbumsEvent, AlbumsAction> by mviImplWithIntent(AlbumsState(albumIds)) {
    val sorter = SortManager(
        prefix = "albums_",
        supportedActions = requestFor<SortAction>(
            "sort_rule_normal",
            "sort_rule_title",
            "sort_rule_items_count",
            "sort_rule_duration",
            "sort_rule_shuffle",
            "sort_rule_play_count",
            "sort_rule_last_play_time"
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val albums = stateFlow()
        .distinctUntilChangedBy { it.distinctKey }
        .flatMapLatest { it.getAlbumsFlow() }
        .doSortState(sorter, viewModelScope)
    val state = stateFlow()
        .toState(AlbumsState(), viewModelScope)

    override fun intent(intent: AlbumsAction): Any = viewModelScope.launch {
        when (intent) {
            AlbumsAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            AlbumsAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            AlbumsAction.HideShowText -> reduce { it.copy(showText = false) }

            AlbumsAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            AlbumsAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            AlbumsAction.ToggleShowText -> reduce { it.copy(showText = !it.showText) }

            is AlbumsAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is AlbumsAction.SelectSortAction -> sorter.setAction(action = intent.action)
            is AlbumsAction.UpdateSortConfig -> sorter.setConfig(config = intent.config)

            AlbumsAction.LocaleToPlayingItem -> {}
        }
    }
}