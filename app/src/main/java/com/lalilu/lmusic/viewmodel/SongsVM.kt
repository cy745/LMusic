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
import com.lalilu.lmedia.extension.sortable.GroupId
import com.lalilu.lmedia.extension.sortable.SortAction
import com.lalilu.lmedia.extension.sortable.SortConfig
import com.lalilu.lmedia.extension.sortable.SortManager
import com.lalilu.lmedia.extension.sortable.doSortState
import com.lalilu.lplayer.MPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


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
) {
    val distinctKey: Int =
        mediaIds.hashCode() + searchKeyWord.hashCode()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSongsFlow(): Flow<List<LSong>> {
        val source = if (mediaIds.isEmpty()) LMedia.getFlow<LSong>()
        else LMedia.flowMapBy<LSong>(mediaIds)

        val keywords: List<String> = when {
            searchKeyWord.isBlank() -> emptyList()
            searchKeyWord.contains(' ') -> searchKeyWord.split(' ')
            else -> listOf(searchKeyWord)
        }

        return source.mapLatest { flow ->
            flow.filter { item -> keywords.all { item.getMatchStr().contains(it.uppercase()) } }
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
    data class LocaleToGroupItem(val item: GroupId) : SongsAction
    data class SearchFor(val keyword: String) : SongsAction
    data class SelectSortAction(val action: SortAction) : SongsAction
    data class UpdateSortConfig(val config: SortConfig) : SongsAction
}

@KoinViewModel
class SongsVM(
    private val mediaIds: List<String>,
) : ViewModel(),
    MviWithIntent<SongsState, SongsEvent, SongsAction> by mviImplWithIntent(SongsState(mediaIds)) {
    val selector = ItemSelector<LSong>()
    val recorder = ItemRecorder()
    val sorter = SortManager(
        prefix = "songs_",
        supportedActions = requestFor<SortAction>(
            "sort_rule_normal",
            "sort_rule_title",
            "sort_rule_add_time",
            "sort_rule_duration",
            "sort_rule_shuffle",
            "sort_rule_play_count",
            "sort_rule_last_play_time"
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val songs = stateFlow()
        .distinctUntilChangedBy { it.distinctKey }
        .flatMapLatest { it.getSongsFlow() }
        .doSortState(sorter, viewModelScope)
    val state = stateFlow().toState(SongsState(), viewModelScope)


    override fun intent(intent: SongsAction) = viewModelScope.launch {
        when (intent) {
            SongsAction.ToggleJumperDialog -> reduce { it.copy(showJumperDialog = !it.showJumperDialog) }
            SongsAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            SongsAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            SongsAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            SongsAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            SongsAction.HideJumperDialog -> reduce { it.copy(showJumperDialog = false) }
            is SongsAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is SongsAction.SelectSortAction -> sorter.setAction(intent.action)
            is SongsAction.UpdateSortConfig -> sorter.setConfig(intent.config)
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

