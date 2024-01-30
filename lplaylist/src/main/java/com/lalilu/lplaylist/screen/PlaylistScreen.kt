package com.lalilu.lplaylist.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.registerSelectPanel
import com.lalilu.lplaylist.PlaylistActions
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.lalilu.component.R as ComponentR

class PlaylistScreenModel : ScreenModel {
    val isSelecting = mutableStateOf(false)
    val selectedItems = mutableStateOf<List<Any>>(emptyList())
}

data object PlaylistScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.playlist_screen_title,
        icon = ComponentR.drawable.ic_play_list_fill
    )

    @Composable
    override fun Content() {
        PlaylistScreen()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DynamicScreen.PlaylistScreen(
    playlistSM: PlaylistScreenModel = rememberScreenModel { PlaylistScreenModel() },
    playlistRepo: PlaylistRepository = koinInject(),
    navigator: GlobalNavigator = koinInject()
) {
    val playlists = playlistRepo.getPlaylists()
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyColumnState(listState) { from, to ->
        playlists.value = playlists.value.toMutableList().apply {
            val toIndex = indexOfFirst { it.id == to.key }
            val fromIndex = indexOfFirst { it.id == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyColumnState

            add(toIndex, removeAt(fromIndex))
        }
        playlists.save()
    }

    LaunchedEffect(Unit) {
        playlistRepo.checkFavouriteExist()
    }

    val selectHelper = rememberItemSelectHelper(
        isSelecting = playlistSM.isSelecting,
        selected = playlistSM.selectedItems
    )

    registerSelectPanel(
        selectActions = { listOf(PlaylistActions.removePlaylists) },
        selector = selectHelper
    )

    LLazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            NavigatorHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                title = stringResource(id = R.string.playlist_screen_title)
            ) {
                IconButton(
                    onClick = { navigator.navigateTo(PlaylistCreateOrEditScreen()) }
                ) {
                    Icon(
                        painter = painterResource(ComponentR.drawable.ic_add_line),
                        contentDescription = null
                    )
                }
            }
        }

        items(
            items = playlists.value,
            key = { it.id },
            contentType = { LPlaylist::class.java }
        ) { playlist ->
            ReorderableItem(
                reorderableLazyListState = reorderableState,
                key = playlist.id
            ) { isDragging ->
                PlaylistCard(
                    playlist = playlist,
                    draggingModifier = Modifier.draggableHandle(),
                    isDragging = { isDragging },
                    isSelected = { selectHelper.isSelected(playlist) },
                    isSelecting = { selectHelper.isSelecting.value },
                    onClick = {
                        if (selectHelper.isSelecting()) {
                            selectHelper.onSelect(playlist)
                        } else {
                            navigator.navigateTo(PlaylistDetailScreen(playlistId = playlist.id))
                        }
                    },
                    onLongClick = { selectHelper.onSelect(playlist) }
                )
            }
        }
    }
}