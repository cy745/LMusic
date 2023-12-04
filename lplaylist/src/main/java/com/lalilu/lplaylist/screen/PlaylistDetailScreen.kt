package com.lalilu.lplaylist.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.base.Playable
import com.lalilu.component.Songs
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.extension.SelectAction
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.extension.replaceForFavourite
import com.lalilu.component.R as componentR
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.repository.PlaylistSp
import org.koin.compose.koinInject

data class PlaylistDetailScreen(
    val playlistId: String
) : DynamicScreen() {

    @Composable
    override fun Content() {
        PlaylistDetailScreen(playlistId)
    }
}

@Composable
private fun DynamicScreen.PlaylistDetailScreen(
    playlistId: String,
    sp: PlaylistSp = koinInject()
) {
    val playlistRepo = koinInject<PlaylistRepository>()
    val playlist by remember {
        derivedStateOf { sp.playlistList.value.firstOrNull { it.id == playlistId } }
    }

    if (playlist == null) {
        return
    }

    val deleteAction = remember {
        SelectAction.StaticAction.Custom(
            title = R.string.playlist_action_remove_from_playlist,
            forLongClick = true,
            icon = componentR.drawable.ic_delete_bin_6_line,
            color = Color.Red
        ) { selector ->
            val mediaIds = selector.selected.value.filterIsInstance(Playable::class.java)
                .map { it.mediaId }

            playlistRepo.removeMediaIdsFromPlaylist(mediaIds, playlistId)
            ToastUtils.showShort("Removed from playlist")
        }
    }

    Songs(
        mediaIds = playlist!!.mediaIds,
        selectActions = { getAll ->
            listOf(
                SelectAction.StaticAction.SelectAll(getAll),
                SelectAction.StaticAction.ClearAll,
                deleteAction
            )
        },
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