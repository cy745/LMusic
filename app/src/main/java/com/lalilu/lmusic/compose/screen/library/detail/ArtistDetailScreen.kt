package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.viewmodel.LocalPlayingVM
import com.lalilu.lmusic.viewmodel.PlayingViewModel

@OptIn(ExperimentalAnimationApi::class)
object ArtistDetailScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.ArtistsDetail.name}?artistName={artistName}",
            arguments = listOf(navArgument("artistName") {})
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")

            Library.getArtistOrNull(artistName)
                ?.let { ArtistDetailScreen(artist = it) }
                ?: EmptyArtistDetailScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.ArtistsDetail.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.ArtistsDetail.name}?artistName=$argv"
    }
}

@Composable
private fun ArtistDetailScreen(
    artist: LArtist,
    playingVM: PlayingViewModel = LocalPlayingVM.current
) {
    val songs = artist.songs
    val navToSongAction = SongDetailScreen.navToByArgv(hapticType = HapticFeedbackType.LongPress)

    val onSongSelected: (LSong) -> Unit = { song ->
        playingVM.playSongWithPlaylist(songs, song)
    }

    SmartContainer.LazyVerticalGrid(
        columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 },
    ) {
//        item {
//            NavigatorHeaderWithButtons(
//                title = artist.name,
//                subTitle = "${artist.getSongCount()} 首歌曲"
//            ) {
//                LazyListSortToggleButton(sortByState = sortByState) {
//                    sortByState = next(sortByState)
//                }
//                SortToggleButton(sortDesc = sortDesc) {
//                    sortDesc = !sortDesc
//                }
//            }
//        }

        itemsIndexed(items = songs) { index, item ->
            SongCard(
                song = { item },
                lyricRepository = playingVM.lyricRepository,
                onClick = { onSongSelected(item) },
                onLongClick = { navToSongAction(item.id) }
            )
//            SongCard(
//                modifier = Modifier.animateItemPlacement(),
//                index = index,
//                getSong = { item },
//                onItemClick = onSongSelected,
//                onItemLongClick = { navToSongAction(it.id) }
//            )
        }
    }
}

@Composable
fun EmptyArtistDetailScreen() {

}
