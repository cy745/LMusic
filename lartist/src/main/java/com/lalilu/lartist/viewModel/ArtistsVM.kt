package com.lalilu.lartist.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.lalilu.common.MviWithIntent
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class ArtistsState(
    // control flags
    val showSortPanel: Boolean = false,
    val showJumperDialog: Boolean = false,
    val showSearcherPanel: Boolean = false,

    // control params
    val searchKeyWord: String = "",
    val selectedSortAction: ListAction = SortStaticAction.Normal,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getArtistsFlow(): Flow<Map<GroupIdentity, List<LArtist>>> {
        val source = LMedia.getFlow<LArtist>()

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

sealed interface ArtistsEvent {
    data class ScrollToItem(val key: Any) : ArtistsEvent
}

sealed interface ArtistsAction {
    data object ToggleSortPanel : ArtistsAction
    data object ToggleSearcherPanel : ArtistsAction
    data object ToggleJumperDialog : ArtistsAction

    data object HideSortPanel : ArtistsAction
    data object HideSearcherPanel : ArtistsAction
    data object HideJumperDialog : ArtistsAction

    data object LocaleToPlayingItem : ArtistsAction
    data class LocaleToGroupItem(val item: GroupIdentity) : ArtistsAction
    data class SearchFor(val keyword: String) : ArtistsAction
    data class SelectSortAction(val action: ListAction) : ArtistsAction
}

@KoinViewModel
class ArtistsVM : ViewModel(),
    MviWithIntent<ArtistsState, ArtistsEvent, ArtistsAction> by mviImplWithIntent(ArtistsState()) {
    val selector = ItemSelector<LArtist>()
    val recorder = ItemRecorder()

    @OptIn(ExperimentalCoroutinesApi::class)
    val artists = stateFlow().flatMapLatest { it.getArtistsFlow() }
        .toState(emptyMap(), viewModelScope)
    val state = stateFlow().toState(ArtistsState(), viewModelScope)

    val supportSortActions = setOf<ListAction?>(
        SortStaticAction.Normal,
        SortStaticAction.Title,
        SortStaticAction.ItemsCount,
        SortStaticAction.Duration,
        SortStaticAction.AddTime,
        SortStaticAction.Shuffle,
    ).filterNotNull()
        .toSet()

    override fun intent(intent: ArtistsAction) = viewModelScope.launch {
        when (intent) {
            ArtistsAction.ToggleJumperDialog -> reduce { it.copy(showJumperDialog = !it.showJumperDialog) }
            ArtistsAction.ToggleSearcherPanel -> reduce { it.copy(showSearcherPanel = !it.showSearcherPanel) }
            ArtistsAction.ToggleSortPanel -> reduce { it.copy(showSortPanel = !it.showSortPanel) }
            ArtistsAction.HideSortPanel -> reduce { it.copy(showSortPanel = false) }
            ArtistsAction.HideSearcherPanel -> reduce { it.copy(showSearcherPanel = false) }
            ArtistsAction.HideJumperDialog -> reduce { it.copy(showJumperDialog = false) }
            is ArtistsAction.SearchFor -> reduce { it.copy(searchKeyWord = intent.keyword) }
            is ArtistsAction.SelectSortAction -> reduce { it.copy(selectedSortAction = intent.action) }
            is ArtistsAction.LocaleToGroupItem -> postEvent { ArtistsEvent.ScrollToItem(intent.item) }
            is ArtistsAction.LocaleToPlayingItem -> {
                val mediaId = MPlayer.currentMediaItem?.mediaId ?: run {
                    LogUtils.e("can not find playing item's mediaId")
                    return@launch
                }

                // 获取该元素
                val item = LMedia.get<LSong>(mediaId)
                    ?: return@launch

                // 获取该元素的所属分组ID
                val artistsIds = item.artists
                    .map { it.id }
                    .takeIf { it.isNotEmpty() }
                    ?: return@launch

                // 获取第一个存在与列表中的元素的Index
                artistsIds.firstOrNull { recorder.list().contains(it) }
                    ?.let { postEvent { ArtistsEvent.ScrollToItem(mediaId) } }
            }

            else -> {
                LogUtils.i("Not implemented action: $intent")
            }
        }
    }
}