package com.lalilu.lmusic.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SongCard
import com.lalilu.lmusic.screen.component.SortToggleButton
import com.lalilu.lmusic.viewmodel.AllSongViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllSongsScreen(
    navigateTo: (destination: String) -> Unit = {},
    allSongViewModel: AllSongViewModel = hiltViewModel(),
    contentPaddingForFooter: Dp = 0.dp
) {
    val haptic = LocalHapticFeedback.current
    val allSongs: List<MediaItem> by allSongViewModel.allSongsLiveData.collectAsState(emptyList())
    var sortByState by rememberDataSaverState("KEY_SORT_BY_AllSongsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_AllSongsScreen", true)
    val sortedItems = remember(sortByState, sortDesc, allSongs) {
        sort(sortByState, sortDesc, allSongs.toMutableStateList(),
            getTextField = { it.mediaMetadata.title.toString() },
            getTimeField = { it.mediaId.toLong() }
        )
    }
    val onSongSelected: (Int) -> Unit = remember(sortedItems) {
        { index ->
            allSongViewModel.playSongWithAllSongPlaylist(
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

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.AllSongs) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = contentPaddingForFooter)
        ) {
            itemsIndexed(sortedItems) { index, item ->
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