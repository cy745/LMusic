package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistDetailViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import okhttp3.internal.toImmutableList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    mainVM: MainViewModel,
    playingVM: PlayingViewModel,
    playlistsVM: PlaylistsViewModel,
    playlistDetailVM: PlaylistDetailViewModel
) {
    val navToSongAction = ScreenActions.navToSong(hapticType = HapticFeedbackType.LongPress)
    val playlist by playlistDetailVM.getPlaylistFlow(playlistId).collectAsState(initial = null)
    val navToAddToPlaylist = mainVM.navToAddToPlaylist()

    LaunchedEffect(playlistId) {
        playlistDetailVM.getPlaylistDetailById(playlistId, this)
    }

    val state = rememberReorderableLazyListState(
        onMove = playlistDetailVM::onMoveItem,
        canDragOver = playlistDetailVM::canDragOver,
        onDragEnd = playlistDetailVM::onDragEnd
    )

    val selectedItems = remember { mutableStateListOf<LSong>() }
    val selector = rememberSelectState(
        defaultState = false,
        selectedItems = selectedItems
    )

    LaunchedEffect(selector.isSelecting.value) {
        if (selector.isSelecting.value) {
            SmartBar.setExtraBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconTextButton(
                        text = "取消",
                        color = Color(0xFF006E7C),
                        onClick = { selector.clear() }
                    )
                    Text(text = "已选择 ${selectedItems.size}")
                    IconTextButton(
                        text = "删除",
                        color = Color(0xFF006E7C),
                        onClick = {
                            playlist?.let {
                                playlistsVM.removeSongsFromPlaylist(
                                    songs = selectedItems.toImmutableList(),
                                    playlist = it
                                )
                            }
                            selector.clear()
                        }
                    )
                    IconTextButton(
                        text = "添加到歌单",
                        color = Color(0xFF3EA22C),
                        onClick = { navToAddToPlaylist(selectedItems) }
                    )
                }
            }
        } else {
            SmartBar.setExtraBar(item = null)
        }
    }

    SmartContainer.LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(state),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "PLAYLIST_HEADER", contentType = LPlaylist::class) {
            NavigatorHeader(
                title = playlist?.name ?: "未知歌单",
                subTitle = "共 ${playlistDetailVM.songs.size} 首歌曲"
            )
        }
        itemsIndexed(
            items = playlistDetailVM.songs,
            key = { _, item -> item.id },
            contentType = { _, _ -> LSong::class }
        ) { index, item ->
            ReorderableItem(
                defaultDraggingModifier = Modifier.animateItemPlacement(),
                state = state,
                key = item.id
            ) { isDragging ->
                SongCard(
                    song = { item },
                    lyricRepository = playingVM.lyricRepository,
                    onClick = {
                        if (selector.isSelecting.value) {
                            selector.onSelected(item)
                        } else {
                            mainVM.playSongWithPlaylist(playlistDetailVM.songs, item)
                        }
                    },
                    dragModifier = Modifier.detectReorder(state),
                    onLongClick = { navToSongAction(item.id) },
                    onEnterSelect = { selector.onSelected(item) },
                    isSelected = { isDragging || selectedItems.any { it.id == item.id } }
                )
            }
        }
    }

//    Column(
//        modifier = Modifier.fillMaxSize()
//    ) {
////        NavigatorHeaderWithButtons(
////            title = playlist.playlistTitle,
////            subTitle = playlist.playlistInfo
////        ) {
////            LazyListSortToggleButton(sortByState = sortByState) {
////                sortByState = next(sortByState)
////            }
////            SortToggleButton(sortDesc = sortDesc) {
////                sortDesc = !sortDesc
////            }
////        }
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
//            contentPadding = PaddingValues(
//                bottom = contentPaddingForFooter
//            )
//        ) {
//            itemsIndexed(items = songs) { index, item ->
//                SongCard(
//                    song = { item },
//                    onClick = { onSongSelected(item) },
//                    onLongClick = { navToSongAction(item.id) }
//                )
////                SongCard(
////                    modifier = Modifier.animateItemPlacement(),
////                    index = index,
////                    getSong = { item },
////                    onItemClick = onSongSelected,
////                    onItemLongClick = { navToSongAction(it.id) }
////                )
//            }
//        }
//    }
}

@Composable
fun EmptyPlaylistDetailScreen() {
    Text(text = "无法获取歌单信息", modifier = Modifier.padding(20.dp))
}