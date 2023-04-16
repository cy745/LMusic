package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.ArtistCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.ArtistsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun ArtistsScreen(
    title: String = "所有艺术家",
    sortFor: String = Sortable.SORT_FOR_ARTISTS,
    artistIdsText: String? = null,
    lMusicSp: LMusicSp = get(),
    artistsVM: ArtistsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val artists = artistsVM.artists

    LaunchedEffect(artistIdsText) {
        artistsVM.show(artistList = artistIdsText.getIds())
    }

    SmartContainer.LazyColumn {
        item {
            NavigatorHeader(
                title = title,
                subTitle = "共 ${artists.size} 条记录"
            )
        }

        itemsIndexed(items = artists) { index, item ->
            ArtistCard(
                modifier = Modifier.animateItemPlacement(),
                index = index,
                artistName = item.name,
                songCount = item.requireItemsCount(),
                onClick = {
                    navigator.navigate(ArtistDetailScreenDestination(item.name))
                }
            )
        }
    }
}
