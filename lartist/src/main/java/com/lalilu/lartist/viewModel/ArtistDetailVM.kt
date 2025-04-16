package com.lalilu.lartist.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.lalilu.common.MviWithIntent
import com.lalilu.common.ext.requestFor
import com.lalilu.common.mviImplWithIntent
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.ItemSelector
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortDynamicAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lplayer.MPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.qualifier.named

@Stable
@Immutable
data class ArtistDetailState(
    val artistName: String,

    // control flags
    val showSortPanel: Boolean = false,
    val showJumperDialog: Boolean = false,
    val showSearcherPanel: Boolean = false,

    // control params
    val searchKeyWord: String = "",
    val selectedSortAction: ListAction = SortStaticAction.Normal,
) {
    val distinctKey: Int = searchKeyWord.hashCode() + selectedSortAction.hashCode()

    fun getArtistFlow(): Flow<LArtist?> {
        return LMedia.getFlow<LArtist>(artistName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSongsFlow(): Flow<Map<GroupIdentity, List<LSong>>> {
        val source = LMedia.getFlow<LArtist>(artistName)
            .map { it?.songs ?: emptyList() }

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

sealed interface ArtistDetailEvent {
    data class ScrollToItem(val key: Any) : ArtistDetailEvent
}

sealed interface ArtistDetailAction {
    data object ToggleSortPanel : ArtistDetailAction
    data object ToggleSearcherPanel : ArtistDetailAction
    data object ToggleJumperDialog : ArtistDetailAction

    data object HideSortPanel : ArtistDetailAction
    data object HideSearcherPanel : ArtistDetailAction
    data object HideJumperDialog : ArtistDetailAction

    data object LocaleToPlayingItem : ArtistDetailAction
    data class LocaleToGroupItem(val item: GroupIdentity) : ArtistDetailAction
    data class SearchFor(val keyword: String) : ArtistDetailAction
    data class SelectSortAction(val action: ListAction) : ArtistDetailAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class ArtistDetailVM(
    private val artistName: String,
) : ViewModel(),
    MviWithIntent<ArtistDetailState, ArtistDetailEvent, ArtistDetailAction> by
    mviImplWithIntent(ArtistDetailState(artistName)) {
    val selector = ItemSelector<LSong>()
    val recorder = ItemRecorder()

    val songs = stateFlow()
        .distinctUntilChangedBy { it.distinctKey }
        .flatMapLatest { it.getSongsFlow() }
        .toState(emptyMap(), viewModelScope)
    val artist = stateFlow()
        .flatMapLatest { it.getArtistFlow() }
        .toState(viewModelScope)
    val state = stateFlow()
        .toState(ArtistDetailState(artistName), viewModelScope)

    val supportSortActions: Set<ListAction> =
        setOf<ListAction?>(
            SortStaticAction.Normal,
            SortStaticAction.Title,
            SortStaticAction.AddTime,
            SortStaticAction.Shuffle,
            SortStaticAction.Duration,
            requestFor(named("sort_rule_play_count")),
            requestFor(named("sort_rule_last_play_time")),
        ).filterNotNull()
            .toSet()

    override fun intent(intent: ArtistDetailAction) = viewModelScope.launch {
        when (intent) {
            ArtistDetailAction.ToggleJumperDialog -> reduce { it.copy(showJumperDialog = !it.showJumperDialog) }
            ArtistDetailAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            ArtistDetailAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            ArtistDetailAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            ArtistDetailAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            ArtistDetailAction.HideJumperDialog -> reduce { it.copy(showJumperDialog = false) }
            is ArtistDetailAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is ArtistDetailAction.SelectSortAction -> reduce { it.copy(selectedSortAction = intent.action) }
            is ArtistDetailAction.LocaleToGroupItem -> postEvent {
                ArtistDetailEvent.ScrollToItem(
                    intent.item
                )
            }

            is ArtistDetailAction.LocaleToPlayingItem -> {
                val mediaId = MPlayer.currentMediaItem?.mediaId ?: run {
                    LogUtils.e("can not find playing item's mediaId")
                    return@launch
                }
                postEvent { ArtistDetailEvent.ScrollToItem(mediaId) }
            }

            else -> {
                LogUtils.i("Not implemented action: $intent")
            }
        }
    }
}

