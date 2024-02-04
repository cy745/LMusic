package com.lalilu.lplaylist.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DialogScreen
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.extension.ItemSelectHelper
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import org.koin.compose.koinInject
import com.lalilu.component.R as componentR

data class PlaylistAddToScreen(
    private val ids: List<String>,
    @Transient private val callback: () -> Unit = {} // callback内若有其他对象的引用会影响到Voyager的序列化
) : DynamicScreen(), DialogScreen {
    override val key: ScreenKey = "${super<DynamicScreen>.key}:${ids.hashCode()}"

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.playlist_action_add_to_playlist
    )

    @Composable
    override fun Content() {
        val playlistRepo: PlaylistRepository = koinInject()
        val playlists = remember { derivedStateOf { playlistRepo.getPlaylists() } }
        val selector = rememberItemSelectHelper()

        RegisterActions {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.playlist_action_add_to_playlist,
                    icon = componentR.drawable.ic_check_line,
                    color = Color(0xFF008521)
                ) {
                    val playlistIds = selector.selected.value
                        .filterIsInstance(LPlaylist::class.java)
                        .map { it.id }

                    playlistRepo.addMediaIdsToPlaylists(
                        mediaIds = ids,
                        playlistIds = playlistIds
                    )

                    callback.invoke()
                }
            )
        }

        PlaylistAddToScreen(
            mediaIds = ids,
            selector = selector,
            playlists = { playlists.value }
        )
    }
}

@Composable
private fun DynamicScreen.PlaylistAddToScreen(
    mediaIds: List<String>,
    selector: ItemSelectHelper,
    playlists: () -> List<LPlaylist>,
) {
    val navigator = koinInject<GlobalNavigator>()

    LLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            NavigatorHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                title = stringResource(id = R.string.playlist_action_add_to_playlist),
                subTitle = "[S: ${mediaIds.size}] -> [P: ${selector.selected.value.size}]"
            ) {
                IconButton(
                    onClick = { navigator.navigateTo(PlaylistCreateOrEditScreen()) }
                ) {
                    Icon(
                        painter = painterResource(componentR.drawable.ic_add_line),
                        contentDescription = null
                    )
                }
            }
        }

        items(
            items = playlists(),
            key = { it.id },
            contentType = { LPlaylist::class.java }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                isSelected = { selector.isSelected(playlist) },
                onClick = { selector.onSelect(playlist) }
            )
        }
    }
}