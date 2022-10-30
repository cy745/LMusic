package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistDetailViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    mainViewModel: MainViewModel,
    vm: PlaylistDetailViewModel
) {
    val navToSongAction = ScreenActions.navToSong(hapticType = HapticFeedbackType.LongPress)
    val playlist by vm.getPlaylistFlow(playlistId).collectAsState(initial = null)

    LaunchedEffect(playlistId) {
        vm.getPlaylistDetailById(playlistId, this)
    }

    val state = rememberReorderableLazyListState(
        onMove = vm::onMoveItem,
        canDragOver = vm::canDragOver,
        onDragEnd = vm::onDragEnd
    )

    val onSongSelected: (LSong) -> Unit = remember {
        { song ->
            mainViewModel.playSongWithPlaylist(vm.songs, song)
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
                subTitle = "共 ${vm.songs.size} 首歌曲"
            )
        }
        itemsIndexed(
            items = vm.songs,
            key = { _, item -> item.id },
            contentType = { _, _ -> LSong::class }
        ) { index, item ->
            ReorderableItem(
                defaultDraggingModifier = Modifier.animateItemPlacement(),
                state = state,
                key = item.id
            ) { isDragging ->
                SongCard(
                    dragModifier = Modifier.detectReorder(state),
                    song = { item },
                    onClick = { onSongSelected(item) },
                    onLongClick = { navToSongAction(item.id) },
                    onEnterSelect = { },
                    isSelected = { isDragging }
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