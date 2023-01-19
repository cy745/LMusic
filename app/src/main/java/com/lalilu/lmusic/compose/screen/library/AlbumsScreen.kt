package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import com.funny.data_saver.core.rememberDataSaverState
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.AlbumCard
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.AlbumDetailScreen
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.LocalLibraryVM

@OptIn(ExperimentalAnimationApi::class)
object AlbumsScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(ScreenData.Albums.name) {
            AlbumsScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Albums.name
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun AlbumsScreen(
    libraryVM: LibraryViewModel = LocalLibraryVM.current
) {
    val albums by libraryVM.albums
    val windowSize = LocalWindowSize.current
    val navToAlbumAction = AlbumDetailScreen.navToByArgv()
    var textVisible by rememberDataSaverState("KEY_TEXT_VISIBLE_AlbumsScreen", true)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_AlbumsScreen", true)

    SmartContainer.LazyStaggeredVerticalGrid(
        contentPaddingForHorizontal = 10.dp,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        columns = if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 3 else 2,
    ) {
        items(items = albums, key = { it.id }, contentType = { LAlbum::class }) {
            AlbumCard(
//                modifier = Modifier.animateItemPlacement(),
                album = { it },
                showTitle = { textVisible },
                onClick = { navToAlbumAction(it.id) }
            )
        }
    }
}
