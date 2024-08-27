package com.lalilu.lplaylist.screen.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.ItemSelector
import com.lalilu.component.extension.rememberSelector
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.checkLine
import com.zhangke.krouter.annotation.Destination
import org.koin.compose.koinInject

@Destination("/playlist/add")
data class PlaylistAddToScreen(
    private val mediaIds: List<String>,
) : Screen, ScreenInfoFactory, ScreenActionFactory {
    override val key: ScreenKey = "${super.key}:${mediaIds.hashCode()}"

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember(this) {
        ScreenInfo(title = R.string.playlist_action_add_to_playlist)
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val playlistRepo: PlaylistRepository = koinInject()

        return remember {
            listOf(
                ScreenAction.Static(
                    title = { stringResource(id = R.string.playlist_action_add_to_playlist) },
                    icon = { RemixIcon.System.checkLine },
                    color = { Color(0xFF008521) },
                    onAction = {
                        val playlistIds = selector?.selected()
                            ?.map { it.id }
                            ?: emptyList()

                        playlistRepo.addMediaIdsToPlaylists(
                            mediaIds = mediaIds,
                            playlistIds = playlistIds
                        )

                        selector?.clear()
                    }
                )
            )
        }
    }

    @Transient
    private var selector: ItemSelector<LPlaylist>? = null

    @Composable
    override fun Content() {
        val playlistRepo: PlaylistRepository = koinInject()
        val playlists = remember { derivedStateOf { playlistRepo.getPlaylists() } }
        val selector = rememberSelector<LPlaylist>()
            .also { this.selector = it }

        PlaylistAddToScreenContent(
            mediaIds = mediaIds,
            selector = selector,
            playlists = { playlists.value }
        )
    }
}