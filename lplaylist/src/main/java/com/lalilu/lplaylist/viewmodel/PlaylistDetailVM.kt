package com.lalilu.lplaylist.viewmodel

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
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.qualifier.named

@OptIn(ExperimentalCoroutinesApi::class)
@Stable
@Immutable
data class PlaylistDetailState(
    val playlistId: String,

    // control flags
    val showSortPanel: Boolean = false,
    val showJumperDialog: Boolean = false,
    val showSearcherPanel: Boolean = false,

    // control params
    val searchKeyWord: String = "",
    val selectedSortAction: ListAction = SortStaticAction.Normal,
) {
    val distinctKey: Int = searchKeyWord.hashCode() + selectedSortAction.hashCode()

    fun getPlaylistFlow(playlistRepo: PlaylistRepository): Flow<LPlaylist?> {
        return playlistRepo.getPlaylistsFlow()
            .mapLatest { list -> list.firstOrNull { it.id == playlistId } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSongsFlow(playlistRepo: PlaylistRepository): Flow<Map<GroupIdentity, List<LSong>>> {
        val source = getPlaylistFlow(playlistRepo)
            .flatMapLatest {
                LMedia.flowMapBy<LSong>(it?.mediaIds ?: emptyList())
            }

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

sealed interface PlaylistDetailEvent {
    data class ScrollToItem(val key: Any) : PlaylistDetailEvent
}

sealed interface PlaylistDetailAction {
    data object ToggleSortPanel : PlaylistDetailAction
    data object ToggleSearcherPanel : PlaylistDetailAction
    data object ToggleJumperDialog : PlaylistDetailAction

    data object HideSortPanel : PlaylistDetailAction
    data object HideSearcherPanel : PlaylistDetailAction
    data object HideJumperDialog : PlaylistDetailAction

    data object LocaleToPlayingItem : PlaylistDetailAction
    data class LocaleToGroupItem(val item: GroupIdentity) : PlaylistDetailAction
    data class SearchFor(val keyword: String) : PlaylistDetailAction
    data class SelectSortAction(val action: ListAction) : PlaylistDetailAction
    data class UpdatePlaylist(val mediaIds: List<String>) : PlaylistDetailAction
    data class RemoveItems(val mediaIds: List<String>) : PlaylistDetailAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class PlaylistDetailVM(
    private val playlistId: String,
    private val playlistRepo: PlaylistRepository
) : ViewModel(),
    MviWithIntent<PlaylistDetailState, PlaylistDetailEvent, PlaylistDetailAction> by
    mviImplWithIntent(PlaylistDetailState(playlistId)) {
    val selector = ItemSelector<LSong>()
    val recorder = ItemRecorder()

    val songs = stateFlow()
        .distinctUntilChangedBy { it.distinctKey }
        .flatMapLatest { it.getSongsFlow(playlistRepo) }
        .toState(emptyMap(), viewModelScope)
    val playlist = stateFlow()
        .flatMapLatest { it.getPlaylistFlow(playlistRepo) }
        .toState(viewModelScope)
    val state = stateFlow()
        .toState(PlaylistDetailState(playlistId), viewModelScope)

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

    override fun intent(intent: PlaylistDetailAction) = viewModelScope.launch {
        when (intent) {
            PlaylistDetailAction.ToggleJumperDialog -> reduce { it.copy(showJumperDialog = !it.showJumperDialog) }
            PlaylistDetailAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            PlaylistDetailAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            PlaylistDetailAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            PlaylistDetailAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            PlaylistDetailAction.HideJumperDialog -> reduce { it.copy(showJumperDialog = false) }
            is PlaylistDetailAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is PlaylistDetailAction.SelectSortAction -> reduce { it.copy(selectedSortAction = intent.action) }
            is PlaylistDetailAction.LocaleToGroupItem -> postEvent {
                PlaylistDetailEvent.ScrollToItem(
                    intent.item
                )
            }

            is PlaylistDetailAction.LocaleToPlayingItem -> {
                val mediaId = MPlayer.currentMediaItem?.mediaId ?: run {
                    LogUtils.e("can not find playing item's mediaId")
                    return@launch
                }
                postEvent { PlaylistDetailEvent.ScrollToItem(mediaId) }
            }

            is PlaylistDetailAction.UpdatePlaylist -> {
                playlist.value?.copy(mediaIds = intent.mediaIds)
                    ?.let { playlistRepo.save(it) }
            }

            is PlaylistDetailAction.RemoveItems -> {
                playlistRepo.removeMediaIdsFromPlaylist(
                    mediaIds = intent.mediaIds,
                    playlistId = playlistId
                )
            }

            else -> {
                LogUtils.i("Not implemented action: $intent")
            }
        }
    }
}

