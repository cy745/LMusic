package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistVM: PlaylistsViewModel,
    mainViewModel: MainViewModel
) {
    val windowSize = LocalWindowSize.current
    val playlist = remember { mutableStateOf<LPlaylist?>(null) }
    val navToSongAction = ScreenActions.navToSong(hapticType = HapticFeedbackType.LongPress)

    val onSongSelected: (LSong) -> Unit = { song ->
        playlist.value?.songs?.let {
            mainViewModel.playSongWithPlaylist(it, song)
        }
    }

    LaunchedEffect(playlistId) {
        playlist.value = playlistVM.playlists.find { it._id == playlistId }
    }

    SmartContainer.LazyVerticalGrid(
        columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(
            items = playlist.value?.songs ?: emptyList(),
            key = { _, item -> item.id },
            contentType = { _, _ -> LSong::class }
        ) { index, item ->
            SongCard(
                song = { item },
                onClick = { onSongSelected(item) },
                onLongClick = { navToSongAction(item.id) },
                onEnterSelect = { onSongSelected(item) }
            )
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