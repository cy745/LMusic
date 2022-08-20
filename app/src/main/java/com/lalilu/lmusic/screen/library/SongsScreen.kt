package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.entity.items
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel

@Composable
fun SongsScreen(
    currentWindowSize: WindowSize,
    navigateTo: (destination: String) -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel(),
    contentPaddingForFooter: Dp = 0.dp
) {
    val songs = Library.getSongs()
    val haptic = LocalHapticFeedback.current
    var sortByState by rememberDataSaverState("KEY_SORT_BY_AllSongsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_AllSongsScreen", true)

    val onSongSelected: (Int) -> Unit = remember(songs) {
        { index ->
            mainViewModel.playSongWithPlaylist(
                items = songs.items(),
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

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.Songs) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (currentWindowSize == WindowSize.Expanded) 2 else 1),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = contentPaddingForFooter)
        ) {
            songs.forEachIndexed { index, item ->
                item {
                    @OptIn(ExperimentalFoundationApi::class)
                    SongCard(
                        modifier = Modifier.animateItemPlacement(),
                        index = index,
                        song = item,
                        onSongSelected = onSongSelected,
                        onSongShowDetail = onSongShowDetail
                    )
                }
            }
        }
    }
}