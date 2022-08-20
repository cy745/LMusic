package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.screen.component.button.TextVisibleToggleButton
import com.lalilu.lmusic.screen.component.card.AlbumCard
import com.lalilu.lmusic.utils.WindowSize

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AlbumsScreen(
    currentWindowSize: WindowSize,
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp
) {
    val albums = Library.getAlbums()
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
            navigateTo("${MainScreenData.AlbumsDetail.name}/$albumId")
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
                if (currentWindowSize == WindowSize.Expanded) 3 else 2
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
