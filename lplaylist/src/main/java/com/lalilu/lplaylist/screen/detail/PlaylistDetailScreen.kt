package com.lalilu.lplaylist.screen.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.toCachedFlow
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.zhangke.krouter.annotation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import com.lalilu.component.R as componentR

@Destination("/pages/playlist/detail")
data class PlaylistDetailScreen(
    val playlistId: String
) : Screen, ScreenInfoFactory, ScreenActionFactory {

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.playlist_screen_detail) }
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        return remember { listOf() }
    }

    @Composable
    override fun Content() {

        PlaylistDetailScreen(
            playlistId = playlistId,
        )
    }
}

class PlaylistDetailScreenModel(
    private val playingVM: IPlayingViewModel,
    private val playlistRepo: PlaylistRepository,
) : ScreenModel {
    private val playlistId = MutableStateFlow("")

    val playlist = playlistId
        .combine(playlistRepo.getPlaylistsFlow()) { id, playlists ->
            playlists.firstOrNull { it.id == id }
        }.toCachedFlow()

    val deleteAction = SelectAction.StaticAction.Custom(
        title = R.string.playlist_action_remove_from_playlist,
        forLongClick = true,
        icon = componentR.drawable.ic_delete_bin_6_line,
        color = Color.Red
    ) { selector ->
        val mediaIds = selector.selected.value.filterIsInstance<LSong>()
            .map { it.id }

        playlistRepo.removeMediaIdsFromPlaylist(mediaIds, playlistId.value)
        ToastUtils.showShort("Removed from playlist")
    }

//    val playAllAction = ScreenAction.StaticAction(
//        title = R.string.playlist_action_play_all,
//        icon = componentR.drawable.ic_play_list_2_fill,
//        color = Color(0xFF008521)
//    ) {
//        val mediaIds = playlist.get()?.mediaIds ?: emptyList()
//
//        if (mediaIds.isEmpty()) {
//            ToastUtils.showShort("No item to play")
//        } else {
//            playingVM.play(
//                mediaIds = mediaIds,
//                mediaId = mediaIds.first()
//            )
//        }
//    }
//
//    val playAllRandomlyAction = ScreenAction.StaticAction(
//        title = R.string.playlist_action_play_randomly,
//        icon = componentR.drawable.ic_dice_line,
//        color = Color(0xFF8D01B4)
//    ) {
//        val mediaIds = playlist.get()?.mediaIds ?: emptyList()
//
//        if (mediaIds.isEmpty()) {
//            ToastUtils.showShort("No item to play")
//        } else {
//            playingVM.play(
//                mediaIds = mediaIds.shuffled(),
//                mediaId = mediaIds.random()
//            )
//        }
//    }
//
//    fun updatePlaylistId(playlistId: String) {
//        this.playlistId.tryEmit(playlistId)
//    }
//
//    fun onDragMoveEnd(items: List<Playable>) {
//        val mediaId = items.map { it.mediaId }
//        playlistRepo.updateMediaIdsToPlaylist(mediaId, playlistId.value)
//    }
}
