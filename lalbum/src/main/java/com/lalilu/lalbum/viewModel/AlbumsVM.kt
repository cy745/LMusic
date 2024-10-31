package com.lalilu.lalbum.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalilu.common.MviWithIntent
import com.lalilu.common.ext.requestFor
import com.lalilu.common.mviImplWithIntent
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortDynamicAction
import com.lalilu.lmedia.extension.SortStaticAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.qualifier.named


data class AlbumsState(
    val albumIds: List<String> = emptyList(),

    // control flags
    val showText: Boolean = false,
    val showSortPanel: Boolean = false,
    val showSearcherPanel: Boolean = false,

    // control params
    val searchKeyWord: String = "",
    val selectedSortAction: ListAction = SortStaticAction.Normal,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAlbumsFlow(): Flow<Map<GroupIdentity, List<LAlbum>>> {
        val source = LMedia.getFlow<LAlbum>()

        val keywords: List<String> = when {
            searchKeyWord.isBlank() -> emptyList()
            searchKeyWord.contains(' ') -> searchKeyWord.split(' ')
            else -> listOf(searchKeyWord)
        }

        val searchResult = source.mapLatest { flow ->
            flow.filter { item -> keywords.all { item.getMatchStr().contains(it) } }
        }

        return when (selectedSortAction) {
            is SortStaticAction -> searchResult.mapLatest {
                selectedSortAction.doSort(it, false)
            }

            is SortDynamicAction -> selectedSortAction.doSort(searchResult, false)
            else -> flowOf(emptyMap())
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
    data class SelectSortAction(val action: ListAction) : AlbumsAction
}

@KoinViewModel
class AlbumsVM(
    val albumIds: List<String>
) : ViewModel(),
    MviWithIntent<AlbumsState, AlbumsEvent, AlbumsAction> by mviImplWithIntent(AlbumsState(albumIds)) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val albums = stateFlow().flatMapLatest { it.getAlbumsFlow() }
        .toState(emptyMap(), viewModelScope)
    val state = stateFlow().toState(AlbumsState(), viewModelScope)

    val supportSortActions: Set<ListAction> =
        setOf<ListAction?>(
            SortStaticAction.Normal,
            SortStaticAction.Title,
            SortStaticAction.Shuffle,
            SortStaticAction.Duration,
            requestFor(named("sort_rule_play_count")),
            requestFor(named("sort_rule_last_play_time")),
        ).filterNotNull()
            .toSet()

    override fun intent(intent: AlbumsAction): Any = viewModelScope.launch {
        when (intent) {
            AlbumsAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            AlbumsAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            AlbumsAction.HideShowText -> reduce { it.copy(showText = false) }

            AlbumsAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            AlbumsAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            AlbumsAction.ToggleShowText -> reduce { it.copy(showText = !it.showText) }

            is AlbumsAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is AlbumsAction.SelectSortAction -> reduce { it.copy(selectedSortAction = intent.action) }

            AlbumsAction.LocaleToPlayingItem -> {}
        }
    }
}