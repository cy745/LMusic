package com.lalilu.lmusic.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmusic.datasource.entity.MPlaylist
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
    var playlist = remember { MPlaylist(playlistId) }
    val sortedItems = remember { emptyList<MediaItem>().toMutableStateList() }
    val title = remember(playlist) { playlist.playlistTitle }
    val subTitle = remember(playlist) { playlist.playlistInfo }
    var sortByState by rememberDataSaverState("KEY_SORT_BY_PlaylistDetailScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_PlaylistDetailScreen", true)

    LaunchedEffect(playlistId) {
        viewModel.getPlaylistById(playlistId)?.let {
            playlist = it
        }
        viewModel.getSongsByPlaylistId(playlistId).let {
            sortedItems.clear()
            sortedItems.addAll(it)
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

    val sort = {
        sort(sortByState, sortDesc, sortedItems,
            getTextField = { it.mediaMetadata.albumTitle.toString() },
            getTimeField = { it.mediaId.toLong() }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigatorHeaderWithButtons(title = title, subTitle = subTitle) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
                sort()
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
                sort()
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