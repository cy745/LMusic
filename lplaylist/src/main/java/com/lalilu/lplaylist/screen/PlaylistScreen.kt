package com.lalilu.lplaylist.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.lalilu.component.LLazyColumn
import com.lalilu.component.SelectPanelWrapper
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistSp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.compose.koinInject
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
        val sp: PlaylistSp = koinInject()

        PlaylistScreen(sp = sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DynamicScreen.PlaylistScreen(
    sp: PlaylistSp = koinInject(),
    playlistSM: PlaylistScreenModel = rememberScreenModel { PlaylistScreenModel() },
    navigator: GlobalNavigator = koinInject()
) {
    val playlist = sp.obtainList<LPlaylist>("Playlist", autoSave = false)
    val state = rememberReorderableLazyListState(
        maxScrollPerFrame = 200.dp,
        onMove = { from, to ->
            val toIndex = playlist.value.indexOfFirst { it.id == to.key }
            val fromIndex = playlist.value.indexOfFirst { it.id == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyListState

            playlist.value = playlist.value.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        },
        canDragOver = { draggedOver, _ ->
            playlist.value.any { it.id == draggedOver.key }
        },
        onDragEnd = { _, _ -> playlist.save() }
    )

    DisposableEffect(Unit) {
        onDispose {
            playlist.save()
        }
    }

    SelectPanelWrapper(
        selector = rememberItemSelectHelper(
            isSelecting = playlistSM.isSelecting,
            selected = playlistSM.selectedItems
        )
    ) { selectHelper ->
        LLazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Playlist")

                    IconButton(
                        onClick = {
                            val id = System.currentTimeMillis().toString()
                            playlist.add(
                                index = 0,
                                item = LPlaylist(
                                    id = System.currentTimeMillis().toString(),
                                    title = "Playlist_$id",
                                    subTitle = "",
                                    coverUri = "",
                                    mediaIds = emptyList()
                                )
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(ComponentR.drawable.ic_add_line),
                            contentDescription = null
                        )
                    }
                }
            }

            items(
                items = playlist.value,
                key = { it.id },
                contentType = { LPlaylist::class.java }
            ) { playlist ->
                ReorderableItem(
                    defaultDraggingModifier = Modifier.animateItemPlacement(),
                    state = state,
                    key = playlist.id
                ) { isDragging ->
                    PlaylistCard(
                        playlist = playlist,
                        draggingModifier = Modifier.detectReorder(state),
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
}