package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.entity.items
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel

@Composable
fun SongsScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val songs = Library.getSongs()
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current
    val navController = LocalNavigatorHost.current
    val contentPaddingForFooter by SmartBar.contentPaddingForSmartBarDp
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
            navController.navigate("${MainScreenData.SongsDetail.name}/$mediaId")
        }
    }

    Column {
//        NavigatorHeaderWithButtons(route = MainScreenData.Songs) {
//            LazyListSortToggleButton(sortByState = sortByState) {
//                sortByState = next(sortByState)
//            }
//            SortToggleButton(sortDesc = sortDesc) {
//                sortDesc = !sortDesc
//            }
//        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
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