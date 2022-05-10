package com.lalilu.lmusic.screen.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.lalilu.lmusic.screen.component.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SongCard
import com.lalilu.lmusic.screen.component.SortToggleButton
import com.lalilu.lmusic.screen.viewmodel.MediaBrowserViewModel
import com.lalilu.lmusic.screen.viewmodel.PlaylistsViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PlaylistDetailScreen(
    playlistId: Long,
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp,
    viewModel: PlaylistsViewModel = hiltViewModel(),
    mediaBrowserViewModel: MediaBrowserViewModel = hiltViewModel()
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
            mediaBrowserViewModel.playSongWithPlaylist(
                items = sortedItems,
                index = index
            )
        }
    }

    val onSongShowDetail: (String) -> Unit = remember {
        { mediaId ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            navigateTo("${MainScreenData.SongDetail.name}/$mediaId")
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
        LazyColumn(
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