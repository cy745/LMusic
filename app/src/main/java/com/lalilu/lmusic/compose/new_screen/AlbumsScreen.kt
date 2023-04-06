package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.AlbumCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.AlbumsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun AlbumsScreen(
    title: String = "全部专辑",
    albumIdsText: String? = null,
    albumsVM: AlbumsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val albums by albumsVM.albums
    val showSortPanel = remember { mutableStateOf(false) }

    LaunchedEffect(albumIdsText) {
        albumsVM.updateByIds(
            ids = albumIdsText.getIds(),
            sortFor = Sortable.SORT_FOR_ALBUMS
        )
    }

    SortPanelWrapper(
        sortFor = Sortable.SORT_FOR_ALBUMS,
        showPanelState = showSortPanel,
        supportSortPresets = {
            listOf(
                SortPreset.SortByTitle,
                SortPreset.SortByDuration,
                SortPreset.SortByItemCount
            )
        },
        supportGroupRules = { albumsVM.sorter.supportGroupRules },
        supportSortRules = { albumsVM.sorter.supportSortRules },
        supportOrderRules = { albumsVM.sorter.supportOrderRules }
    ) {
        SmartContainer.LazyStaggeredVerticalGrid(
            contentPaddingForHorizontal = 10.dp,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            columns = { if (it == WindowWidthSizeClass.Expanded) 3 else 2 }
        ) {
            item(key = "Header", contentType = "Header") {
                Surface(shape = RoundedCornerShape(5.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        NavigatorHeader(
                            title = title,
                            subTitle = "共 ${albums.size} 张专辑"
                        )
                        Button(onClick = { showSortPanel.value = !showSortPanel.value }) {
                            Text(text = "排序")
                        }
                    }
                }
            }
            items(items = albums, key = { it.id }, contentType = { LAlbum::class }) {
                AlbumCard(
                    album = { it },
                    showTitle = { true },
                    onClick = { navigator.navigate(AlbumDetailScreenDestination(it.id)) }
                )
            }
        }
    }
}