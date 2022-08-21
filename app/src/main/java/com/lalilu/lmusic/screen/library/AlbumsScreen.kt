package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.screen.component.button.TextVisibleToggleButton
import com.lalilu.lmusic.screen.component.card.AlbumCard
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AlbumsScreen() {
    val albums = Library.getAlbums()
    val windowSize = LocalWindowSize.current
    val navController = LocalNavigatorHost.current
    val contentPaddingForFooter by SmartBar.contentPaddingForSmartBarDp
    var textVisible by rememberDataSaverState("KEY_TEXT_VISIBLE_AlbumsScreen", true)
    var sortByState by rememberDataSaverState("KEY_SORT_BY_AlbumsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_AlbumsScreen", true)
//    val sortedItems = remember(sortByState, sortDesc, albums) {
//        sort(sortByState, sortDesc, albums.toMutableStateList(),
//            getTextField = { it.mediaMetadata.albumTitle.toString() },
//            getTimeField = { it.mediaId.toLong() }
//        )
//    }

    val onAlbumSelected = remember {
        { albumId: String ->
            navController.navigate("${MainScreenData.AlbumsDetail.name}/$albumId")
        }
    }

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.Albums) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
            }
            TextVisibleToggleButton(textVisible = textVisible) {
                textVisible = !textVisible
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Fixed(
                if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 3 else 2
            ),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 10.dp,
                bottom = contentPaddingForFooter
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            albums.forEach {
                item {
                    AlbumCard(
                        modifier = Modifier.animateItemPlacement(),
                        album = it,
                        drawText = textVisible,
                        onAlbumSelected = onAlbumSelected
                    )
                }
            }
        }
    }
}
