package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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

const val Name = 0b1
const val Count = 0b01

fun Int.contain(v: Int): Boolean = this and v == v
fun Int.place(v: Int): Int = this or v
fun Int.remove(v: Int): Int = this and v.inv()

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun ArtistsScreen(
    title: String = "所有艺术家",
    artistIdsText: String? = null,
    lMusicSp: LMusicSp = get(),
    artistsVM: ArtistsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val artists = artistsVM.artists
    var artistSortKey by lMusicSp.artistSortValue

    LaunchedEffect(artistIdsText) {
        artistsVM.show(artistList = artistIdsText.getIds())
    }

    SmartContainer.LazyColumn {
        item {
            NavigatorHeader(
                title = title,
                subTitle = "共 ${artists.size} 条记录"
            ) {
                Switch(
                    checked = artistSortKey.contain(Name),
                    onCheckedChange = {
                        artistSortKey =
                            if (it) artistSortKey.place(Name) else artistSortKey.remove(Name)
                    }
                )
            }
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
