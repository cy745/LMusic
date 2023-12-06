package com.lalilu.lplaylist.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.base.Playable
import com.lalilu.component.Songs
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.extension.SelectAction
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.extension.replaceForFavourite
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.repository.PlaylistSp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import com.lalilu.component.R as componentR

data class PlaylistDetailScreen(
    val playlistId: String
) : DynamicScreen() {

    @Composable
    override fun Content() {
        val playlistDetailSM: PlaylistDetailScreenModel = getScreenModel()
        playlistDetailSM.updatePlaylistId(playlistId)

        PlaylistDetailScreen(playlistDetailSM = playlistDetailSM)
    }
}

class PlaylistDetailScreenModel(
    sp: PlaylistSp,
    private val playlistRepo: PlaylistRepository
) : ScreenModel {
    private val playlistId = MutableStateFlow("")

    val playlist = playlistId
        .combine(sp.playlistList.flow(true)) { id, playlists ->
            playlists?.firstOrNull { it.id == id }
        }

    val deleteAction = SelectAction.StaticAction.Custom(
        title = R.string.playlist_action_remove_from_playlist,
        forLongClick = true,
        icon = componentR.drawable.ic_delete_bin_6_line,
        color = Color.Red
    ) { selector ->
        val mediaIds = selector.selected.value.filterIsInstance(Playable::class.java)
            .map { it.mediaId }

        playlistRepo.removeMediaIdsFromPlaylist(mediaIds, playlistId.value)
        ToastUtils.showShort("Removed from playlist")
    }

    fun updatePlaylistId(playlistId: String) {
        this.playlistId.tryEmit(playlistId)
    }

    fun onDragMoveEnd(items: List<Playable>) {
        val mediaId = items.map { it.mediaId }
        playlistRepo.updateMediaIdsToPlaylist(mediaId, playlistId.value)
    }
}

@Composable
private fun DynamicScreen.PlaylistDetailScreen(
    playlistDetailSM: PlaylistDetailScreenModel,
) {
    val playlist by playlistDetailSM.playlist.collectAsState(initial = null)

    if (playlist == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Playlist Load Error")
        }
        return
    }

    Songs(
        mediaIds = playlist!!.mediaIds,
        onDragMoveEnd = playlistDetailSM::onDragMoveEnd,
        selectActions = { getAll ->
            listOf(
                SelectAction.StaticAction.SelectAll(getAll),
                SelectAction.StaticAction.ClearAll,
                playlistDetailSM.deleteAction
            )
        },
        sortFor = "PlaylistDetail",
        supportListAction = { emptyList() },
        headerContent = {
            item {
                NavigatorHeader(
                    title = replaceForFavourite(playlist!!, LPlaylist::title),
                    subTitle = replaceForFavourite(playlist!!, LPlaylist::subTitle)
                )
            }
        }
    )
}