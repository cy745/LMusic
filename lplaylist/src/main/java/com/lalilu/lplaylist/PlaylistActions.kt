package com.lalilu.lplaylist

import androidx.compose.ui.graphics.Color
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.base.Playable
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.screen.PlaylistAddToScreen
import org.koin.java.KoinJavaComponent
import com.lalilu.component.R as componentR

object PlaylistActions {
    val navigator: GlobalNavigator by KoinJavaComponent.inject(GlobalNavigator::class.java)

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

        navigator.navigateTo(PlaylistAddToScreen(ids = mediaIds))
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

        // todo
        ToastUtils.showShort("已添加${mediaIds.size}首歌曲至我喜欢")
    }
}