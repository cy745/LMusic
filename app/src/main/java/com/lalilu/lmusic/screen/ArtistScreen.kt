package com.lalilu.lmusic.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmusic.datasource.extensions.getArtistId
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.ArtistCard
import com.lalilu.lmusic.screen.component.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SortToggleButton

@Composable
fun ArtistScreen(
    artists: List<MediaItem>,
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp
) {
    var sortByState by rememberDataSaverState("KEY_SORT_BY_ArtistScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_ArtistScreen", false)
    val sortedItems = remember(sortByState, sortDesc) {
        sort(sortByState, sortDesc, artists.toMutableStateList(),
            getTextField = { it.mediaMetadata.artist.toString() },
            getTimeField = { it.mediaMetadata.getArtistId() }
        )
    }

    val onArtistSelected = remember {
        { artistId: String ->
            navigateTo("${MainScreenData.ArtistDetail.name}/$artistId")
        }
    }

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.Artist) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                bottom = contentPaddingForFooter
            )
        ) {
            itemsIndexed(sortedItems) { index, item ->
                ArtistCard(
                    index = index,
                    mediaItem = item,
                    onSelected = onArtistSelected
                )
            }
        }
    }
}