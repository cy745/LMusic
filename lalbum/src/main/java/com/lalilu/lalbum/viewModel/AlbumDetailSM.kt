package com.lalilu.lalbum.viewModel

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lalilu.common.base.Playable
import com.lalilu.component.base.songs.ItemSearcher
import com.lalilu.component.base.songs.ItemSorter
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.ItemSelector
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lplayer.LPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal sealed interface AlbumDetailScreenAction {
    data object LocaleToPlayingItem : AlbumDetailScreenAction
    data class LocaleToGroupItem(val item: GroupIdentity) : AlbumDetailScreenAction
}

internal sealed interface AlbumDetailScreenEvent {
    data class ScrollToItem(val key: Any) : AlbumDetailScreenEvent
}

internal class AlbumDetailSM(
    private val albumId: String
) : ScreenModel {
    // 持久化元素的状态
    val showSortPanel = mutableStateOf(false)
    val showJumperDialog = mutableStateOf(false)
    val showSearcherPanel = mutableStateOf(false)
    val supportSortActions = setOf<ListAction?>(
        SortStaticAction.Normal,
        SortStaticAction.Title,
        SortStaticAction.Duration,
        SortStaticAction.AddTime,
        SortStaticAction.Shuffle,
    ).filterNotNull()
        .toSet()

    // 数据流
    private fun flow(): Flow<LAlbum?> = LMedia.getFlow<LAlbum>(albumId)
    val searcher = ItemSearcher(flow().map { it?.songs ?: emptyList() })
    val sorter = ItemSorter(searcher.output, supportSortActions)

    val album = flow().toState(
        defaultValue = null,
        scope = screenModelScope
    )
    val songs = sorter.output.toState(
        defaultValue = emptyMap(),
        scope = screenModelScope,
    )

    val selector = ItemSelector<Playable>()
    val recorder = ItemRecorder()

    private val _eventFlow = MutableSharedFlow<AlbumDetailScreenEvent>()
    val eventFlow: SharedFlow<AlbumDetailScreenEvent> = _eventFlow

    fun doAction(action: AlbumDetailScreenAction) = screenModelScope.launch {
        when (action) {
            AlbumDetailScreenAction.LocaleToPlayingItem -> {
                // 获取正在播放的元素ID
                val mediaId = LPlayer.runtime.info.playingIdFlow.value
                    ?: return@launch

                // 获取该元素
                val item = LMedia.get<LSong>(mediaId)
                    ?.album
                    ?: return@launch

                _eventFlow.emit(AlbumDetailScreenEvent.ScrollToItem(item.id))
            }

            is AlbumDetailScreenAction.LocaleToGroupItem -> {
                _eventFlow.emit(AlbumDetailScreenEvent.ScrollToItem(action.item))
            }

            else -> {}
        }
    }
}