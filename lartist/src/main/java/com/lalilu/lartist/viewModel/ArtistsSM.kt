package com.lalilu.lartist.viewModel

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lalilu.component.base.songs.ItemSearcher
import com.lalilu.component.base.songs.ItemSorter
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.ItemSelector
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lplayer.LPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal sealed interface ArtistsScreenAction {
    data object LocaleToPlayingItem : ArtistsScreenAction
    data class LocaleToGroupItem(val item: GroupIdentity) : ArtistsScreenAction
}

internal sealed interface ArtistsScreenEvent {
    data class ScrollToItem(val key: Any) : ArtistsScreenEvent
}

internal class ArtistsSM : ScreenModel {
    // 持久化元素的状态
    val showSortPanel = mutableStateOf(false)
    val showJumperDialog = mutableStateOf(false)
    val showSearcherPanel = mutableStateOf(false)
    val supportSortActions = setOf<ListAction?>(
        SortStaticAction.Normal,
        SortStaticAction.Title,
        SortStaticAction.ItemsCount,
        SortStaticAction.Duration,
        SortStaticAction.AddTime,
        SortStaticAction.Shuffle,
    ).filterNotNull()
        .toSet()

    // 数据流
    private fun flow(): Flow<List<LArtist>> = LMedia.getFlow<LArtist>()
    val searcher = ItemSearcher(flow())
    val sorter = ItemSorter(searcher.output, supportSortActions)
    val artists = sorter.output.toState(
        defaultValue = emptyMap(),
        scope = screenModelScope,
    )

    val selector = ItemSelector<LArtist>()
    val recorder = ItemRecorder()


    private val _eventFlow = MutableSharedFlow<ArtistsScreenEvent>()
    val eventFlow: SharedFlow<ArtistsScreenEvent> = _eventFlow

    fun doAction(action: ArtistsScreenAction) = screenModelScope.launch {
        when (action) {
            ArtistsScreenAction.LocaleToPlayingItem -> {
                // 获取正在播放的元素ID
                val mediaId = LPlayer.runtime.info.playingIdFlow.value
                    ?: return@launch

                // 获取该元素
                val item = LMedia.get<LSong>(mediaId)
                    ?: return@launch

                // 获取该元素的所属分组ID
                val artistsIds = item.artists
                    .map { it.id }
                    .takeIf { it.isNotEmpty() }
                    ?: return@launch

                // 获取第一个存在与列表中的元素的Index
                val list = recorder.list()
                artistsIds.firstOrNull { list.contains(it) }?.let {
                    _eventFlow.emit(ArtistsScreenEvent.ScrollToItem(it))
                }
            }

            is ArtistsScreenAction.LocaleToGroupItem -> {
                _eventFlow.emit(ArtistsScreenEvent.ScrollToItem(action.item))
            }

            else -> {}
        }
    }
}