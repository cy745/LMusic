package com.lalilu.lplaylist

import androidx.compose.ui.graphics.Color
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.base.Playable
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.screen.PlaylistAddToScreen
import org.koin.java.KoinJavaComponent
import com.lalilu.component.R as componentR

object PlaylistActions {
    private val playlistRepo: PlaylistRepository by KoinJavaComponent.inject(PlaylistRepository::class.java)

    /**
     * 将指定歌曲添加至播放列表
     */
    val addToPlaylistAction = SelectAction.StaticAction.Custom(
        title = R.string.playlist_action_add_to_playlist,
        icon = componentR.drawable.ic_play_list_add_line,
        color = Color(0xFF04B931),
    ) { selector ->
        val mediaIds = selector.selected.value
            .mapNotNull { (it as? Playable)?.mediaId }

        AppRouter.intent(
            NavIntent.Push(
                PlaylistAddToScreen(
                    ids = mediaIds,
                    callback = {
                        selector.clear()

                        AppRouter.intent(NavIntent.Pop)
                    }
                )
            )
        )
    }

    /**
     * 将指定歌曲添加至播放列表
     */
    val addToFavorite = SelectAction.StaticAction.Custom(
        title = R.string.playlist_action_add_to_favorites,
        icon = componentR.drawable.ic_heart_3_fill,
        color = Color(0xFFE91E63),
    ) { selector ->
        val mediaIds = selector.selected.value
            .mapNotNull { (it as? Playable)?.mediaId }

        playlistRepo.addMediaIdsToFavourite(mediaIds)
        ToastUtils.showShort("已添加${mediaIds.size}首歌曲至我喜欢")
    }

    /**
     * 删除指定的播放列表
     */
    internal val removePlaylists = SelectAction.StaticAction.Custom(
        title = R.string.playlist_action_remove_playlist,
        forLongClick = true,
        icon = componentR.drawable.ic_delete_bin_6_line,
        color = Color(0xFFE91E1E),
    ) { selector ->
        val selectedPlaylist = selector.selected.value.filterIsInstance<LPlaylist>()
        val playlistIds = selectedPlaylist.map { it.id }

        runCatching {
            playlistRepo.removeByIds(ids = playlistIds)
            ToastUtils.showShort("已删除${playlistIds.size}个歌单")
            selector.remove(selectedPlaylist)
        }.getOrElse {
            it.printStackTrace()
            ToastUtils.showShort("删除失败")
        }
    }
}