package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.base.SongsSelectWrapper
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistDetailViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import okhttp3.internal.toImmutableList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@PlaylistNavGraph
@Destination
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playingVM: PlayingViewModel = get(),
    playlistsVM: PlaylistsViewModel = get(),
    playlistDetailVM: PlaylistDetailViewModel = get(),
    navigator: DestinationsNavigator
) {
    val haptic = LocalHapticFeedback.current
    val playlist by playlistDetailVM.getPlaylistFlow(playlistId).collectAsState(initial = null)
    LaunchedEffect(Unit) {
        playlistDetailVM.getPlaylistDetailById(playlistId, this)
    }

    val dragState = rememberReorderableLazyListState(
        onMove = playlistDetailVM::onMoveItem,
        canDragOver = playlistDetailVM::canDragOver,
        onDragEnd = playlistDetailVM::onDragEnd
    )

    SongsSelectWrapper(
        extraActionsContent = { selector ->
            IconTextButton(
                text = "删除",
                color = Color(0xFF006E7C),
                onClick = {
                    playlist?.let {
                        playlistsVM.removeSongsFromPlaylist(
                            songs = selector.selectedItems.toImmutableList(),
                            playlist = it
                        )
                    }
                    selector.clear()
                }
            )
        }
    ) { selector ->
        SmartContainer.LazyColumn(
            state = dragState.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(dragState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item(key = "PLAYLIST_HEADER", contentType = LPlaylist::class) {
                NavigatorHeader(
                    title = playlist?.name ?: "未知歌单",
                    subTitle = "共 ${playlistDetailVM.songs.size} 首歌曲"
                )
            }
            items(
                items = playlistDetailVM.songs,
                key = { item -> item.id },
                contentType = { LSong::class }
            ) { item ->
                ReorderableItem(
                    defaultDraggingModifier = Modifier.animateItemPlacement(),
                    state = dragState,
                    key = item.id
                ) { isDragging ->
                    SongCard(
                        song = { item },
                        hasLyric = playingVM.lyricRepository.rememberHasLyric(song = item),
                        onClick = {
                            if (selector.isSelecting.value) {
                                selector.onSelected(item)
                            } else {
                                playingVM.playSongWithPlaylist(playlistDetailVM.songs, item)
                            }
                        },
                        dragModifier = Modifier.detectReorder(dragState),
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigator.navigate(SongDetailScreenDestination(item.id))
                        },
                        onEnterSelect = { selector.onSelected(item) },
                        isSelected = { isDragging || selector.selectedItems.any { it.id == item.id } }
                    )
                }
            }
        }
    }
}