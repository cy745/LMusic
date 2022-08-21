package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.screen.component.card.ArtistCard
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost

@Composable
fun ArtistScreen() {
    val artists = Library.getArtists()
    val navController = LocalNavigatorHost.current
    val contentPaddingForFooter by SmartBar.contentPaddingForSmartBarDp
    var sortByState by rememberDataSaverState("KEY_SORT_BY_ArtistScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_ArtistScreen", true)

    val onArtistSelected = remember {
        { artistId: String ->
            navController.navigate("${MainScreenData.ArtistsDetail.name}/$artistId")
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
            artists.forEachIndexed { index, item ->
                item {
                    ArtistCard(
                        index = index,
                        artistTitle = item.name,
                        onClick = { onArtistSelected(item.name) }
                    )
                }
            }
        }
    }
}