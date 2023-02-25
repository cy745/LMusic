package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.AlbumCard
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun AlbumsScreen(
    mediaVM: LMediaViewModel = get(),
    navigator: DestinationsNavigator
) {
    val albums by mediaVM.albums.collectAsState(initial = emptyList())

    SmartContainer.LazyStaggeredVerticalGrid(
        contentPaddingForHorizontal = 10.dp,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        columns = { if (it == WindowWidthSizeClass.Expanded) 3 else 2 }
    ) {
        items(items = albums, key = { it.id }, contentType = { LAlbum::class }) {
            AlbumCard(
//                modifier = Modifier.animateItemPlacement(),
                album = { it },
                showTitle = { true },
                onClick = { navigator.navigate(AlbumDetailScreenDestination(it.id)) }
            )
        }
    }
}