package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.compose.component.AlbumCard
import com.lalilu.lmusic.screen.ScreenActions
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.component.SmartContainer
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.LibraryViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AlbumsScreen(
    libraryViewModel: LibraryViewModel
) {
    val albums by libraryViewModel.albums.observeAsState(emptyList())
    val windowSize = LocalWindowSize.current
    val navToAlbumAction = ScreenActions.navToAlbum()
    var textVisible by rememberDataSaverState("KEY_TEXT_VISIBLE_AlbumsScreen", true)
    var sortByState by rememberDataSaverState("KEY_SORT_BY_AlbumsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_AlbumsScreen", true)

    SmartContainer.LazyVerticalGrid(
        columns = GridCells.Fixed(
            if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 3 else 2
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPaddingForHorizontal = 10.dp
    ) {
        items(items = albums.toList(), key = { it.id }, contentType = { LAlbum::class }) {
            AlbumCard(
                modifier = Modifier.animateItemPlacement(),
                album = { it },
                showTitle = { textVisible },
                onClick = { navToAlbumAction(it.id) }
            )
        }
    }
}
