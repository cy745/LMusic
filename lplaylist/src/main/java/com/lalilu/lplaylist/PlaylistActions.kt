package com.lalilu.lplaylist

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.RemixIcon
import com.lalilu.common.base.Playable
import com.lalilu.common.ext.requestFor
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.remixicon.HealthAndMedical
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.healthandmedical.heart3Line
import com.lalilu.remixicon.media.playListAddLine
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [ScreenAction::class])
@Named("add_to_playlist_action")
fun provideAddToPlaylistAction(
    selectedItems: () -> Collection<Playable>
): ScreenAction.Static = ScreenAction.Static(
    title = { "添加到歌单" },
    icon = { RemixIcon.Media.playListAddLine },
    color = { Color(0xFF24A800) },
    onAction = {
        val items = selectedItems()

        AppRouter.route("/playlist/add")
            .with("mediaIds", items.map { it.mediaId })
            .jump()
    }
)

@Factory(binds = [ScreenAction::class])
@Named("add_to_favourite_action")
fun provideAddToFavouriteAction(
    selectedItems: () -> Collection<Playable>
): ScreenAction.Static = ScreenAction.Static(
    title = { "添加到我喜欢" },
    icon = { RemixIcon.HealthAndMedical.heart3Line },
    color = { MaterialTheme.colors.primary },
    onAction = {
        val items = selectedItems().map { it.mediaId }
        val playlistRepo = requestFor<PlaylistRepository>()

        playlistRepo?.let {
            it.addMediaIdsToFavourite(items)
            ToastUtils.showShort("已添加${items.size}首歌曲至我喜欢")
        }
    }
)