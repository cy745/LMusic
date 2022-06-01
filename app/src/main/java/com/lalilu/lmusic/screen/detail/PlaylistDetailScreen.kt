package com.lalilu.lmusic.screen.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PlaylistDetailScreen(
    playlistId: Long,
    currentWindowSize: WindowSize,
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp,
    viewModel: PlaylistsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val haptic = LocalHapticFeedback.current
    var playlist by remember { mutableStateOf(MPlaylist(playlistId)) }
    val playlistItems = remember { emptyList<MediaItem>().toMutableStateList() }

    var sortByState by rememberDataSaverState("KEY_SORT_BY_PlaylistDetailScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_PlaylistDetailScreen", true)
    val sortedItems = remember(sortByState, sortDesc, playlistItems) {
        sort(sortByState, sortDesc, playlistItems,
            getTextField = { it.mediaMetadata.title.toString() },
            getTimeField = { it.mediaId.toLong() }
        )
    }

    LaunchedEffect(playlistId) {
        viewModel.getPlaylistById(playlistId)?.let {
            playlist = it
        }
        viewModel.getSongsByPlaylistId(playlistId).let {
            playlistItems.clear()
            playlistItems.addAll(it)
        }
    }

    val onSongSelected: (Int) -> Unit = remember {
        { index: Int ->
            mainViewModel.playSongWithPlaylist(
                items = sortedItems,
                index = index
            )
        }
    }

    val onSongShowDetail: (String) -> Unit = remember {
        { mediaId ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            navigateTo("${MainScreenData.SongsDetail.name}/$mediaId")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigatorHeaderWithButtons(
            title = playlist.playlistTitle,
            subTitle = playlist.playlistInfo
        ) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (currentWindowSize == WindowSize.Expanded) 2 else 1),
            contentPadding = PaddingValues(
                bottom = contentPaddingForFooter
            )
        ) {
            itemsIndexed(items = sortedItems) { index, item ->
                SongCard(
                    modifier = Modifier.animateItemPlacement(),
                    index = index,
                    mediaItem = item,
                    onSongSelected = onSongSelected,
                    onSongShowDetail = onSongShowDetail
                )
            }
        }
    }
}