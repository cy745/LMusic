package com.lalilu.lmusic.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.*

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AlbumsScreen(
    albums: List<MediaItem>,
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp
) {
    var textVisible by rememberDataSaverState("KEY_TEXT_VISIBLE_AlbumsScreen", false)
    var sortByState by rememberDataSaverState("KEY_SORT_BY_AlbumsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_AlbumsScreen", false)
    val sortedItems = remember { albums.toMutableStateList() }

    val sort = {
        sort(sortByState, sortDesc, sortedItems,
            getTextField = { it.mediaMetadata.albumTitle.toString() },
            getTimeField = { it.mediaId.toLong() }
        )
    }
    val onAlbumSelected = remember {
        { albumId: String ->
            navigateTo("${MainScreenData.AlbumDetail.name}/$albumId")
        }
    }

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.Albums) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
                sort()
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
                sort()
            }
            TextVisibleToggleButton(textVisible = textVisible) {
                textVisible = !textVisible
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 10.dp,
                bottom = contentPaddingForFooter
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sortedItems) { item ->
                AlbumCard(
                    modifier = Modifier.animateItemPlacement(),
                    mediaItem = item,
                    drawText = textVisible,
                    onAlbumSelected = onAlbumSelected
                )
            }
        }
    }
}
