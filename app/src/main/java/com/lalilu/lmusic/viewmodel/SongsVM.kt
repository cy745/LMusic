package com.lalilu.lmusic.viewmodel

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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.qualifier.named


@Stable
@Immutable
data class SongsState(
    // initialize values
    val mediaIds: List<String> = emptyList(),

    // control flags
    val showSortPanel: Boolean = false,
    val showJumperDialog: Boolean = false,
    val showSearcherPanel: Boolean = false,

    // control params
    val searchKeyWord: String = "",
    val selectedSortAction: ListAction = SortStaticAction.Normal,
) {
    val distinctKey: Int =
        mediaIds.hashCode() + searchKeyWord.hashCode() + selectedSortAction.hashCode()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSongsFlow(): Flow<Map<GroupIdentity, List<LSong>>> {
        val source = if (mediaIds.isEmpty()) LMedia.getFlow<LSong>()
        else LMedia.flowMapBy<LSong>(mediaIds)

        val keywords: List<String> = when {
            searchKeyWord.isBlank() -> emptyList()
            searchKeyWord.contains(' ') -> searchKeyWord.split(' ')
            else -> listOf(searchKeyWord)
        }

        val searchResult = source.mapLatest { flow ->
            flow.filter { item -> keywords.all { item.getMatchStr().contains(it.uppercase()) } }
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

sealed interface SongsEvent {
    data class ScrollToItem(val key: Any) : SongsEvent
}

sealed interface SongsAction {
    data object ToggleSortPanel : SongsAction
    data object ToggleSearcherPanel : SongsAction
    data object ToggleJumperDialog : SongsAction

    data object HideSortPanel : SongsAction
    data object HideSearcherPanel : SongsAction
    data object HideJumperDialog : SongsAction

    data object LocaleToPlayingItem : SongsAction
    data class LocaleToGroupItem(val item: GroupIdentity) : SongsAction
    data class SearchFor(val keyword: String) : SongsAction
    data class SelectSortAction(val action: ListAction) : SongsAction
}

@KoinViewModel
class SongsVM(
    private val mediaIds: List<String>,
) : ViewModel(),
    MviWithIntent<SongsState, SongsEvent, SongsAction> by mviImplWithIntent(SongsState(mediaIds)) {
    val selector = ItemSelector<LSong>()
    val recorder = ItemRecorder()

    @OptIn(ExperimentalCoroutinesApi::class)
    val songs = stateFlow()
        .distinctUntilChangedBy { it.distinctKey }
        .flatMapLatest { it.getSongsFlow() }
        .toState(emptyMap(), viewModelScope)
    val state = stateFlow().toState(SongsState(), viewModelScope)

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

    override fun intent(intent: SongsAction) = viewModelScope.launch {
        when (intent) {
            SongsAction.ToggleJumperDialog -> reduce { it.copy(showJumperDialog = !it.showJumperDialog) }
            SongsAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            SongsAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            SongsAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            SongsAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            SongsAction.HideJumperDialog -> reduce { it.copy(showJumperDialog = false) }
            is SongsAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is SongsAction.SelectSortAction -> reduce { it.copy(selectedSortAction = intent.action) }
            is SongsAction.LocaleToGroupItem -> postEvent { SongsEvent.ScrollToItem(intent.item) }
            is SongsAction.LocaleToPlayingItem -> {
                val mediaId = MPlayer.currentMediaItem?.mediaId ?: run {
                    LogUtils.e("can not find playing item's mediaId")
                    return@launch
                }
                postEvent { SongsEvent.ScrollToItem(mediaId) }
            }

            else -> {
                LogUtils.i("Not implemented action: $intent")
            }
        }
    }
}

