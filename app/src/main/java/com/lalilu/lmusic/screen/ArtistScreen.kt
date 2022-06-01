package com.lalilu.lmusic.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.card.ArtistCard
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.viewmodel.ArtistViewModel

@Composable
fun ArtistScreen(
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp,
    artistViewModel: ArtistViewModel = hiltViewModel()
) {
    val artists by artistViewModel.artists.collectAsState(initial = emptyList())
    var sortByState by rememberDataSaverState("KEY_SORT_BY_ArtistScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_ArtistScreen", true)
    val sortedItems = remember(sortByState, sortDesc, artists) {
        sort(sortByState, sortDesc, artists.toMutableStateList(),
            getTextField = { it.artist.artistName },
            getTimeField = { it.mapIds.getOrNull(0)?.originArtistId?.toLong() ?: -1L }
        )
    }

    val onArtistSelected = remember {
        { artistId: String ->
            navigateTo("${MainScreenData.ArtistsDetail.name}/$artistId")
        }
    }

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.Artists) {
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
                    artistTitle = item.artist.artistName,
                    onClick = { onArtistSelected(item.artist.artistName) }
                )
            }
        }
    }
}