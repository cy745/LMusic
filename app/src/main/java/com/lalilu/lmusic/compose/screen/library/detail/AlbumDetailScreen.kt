package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.AlbumCoverCard
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.viewmodel.LocalPlayingVM
import com.lalilu.lmusic.viewmodel.PlayingViewModel

@OptIn(ExperimentalAnimationApi::class)
object AlbumDetailScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.AlbumsDetail.name}?albumId={albumId}",
            arguments = listOf(navArgument("albumId") {})
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")

            Library.getAlbumOrNull(albumId)
                ?.let { AlbumDetailScreen(album = it) }
                ?: EmptyAlbumDetailScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.AlbumsDetail.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.AlbumsDetail.name}?albumId=$argv"
    }
}

@Composable
private fun AlbumDetailScreen(
    album: LAlbum,
    playingVM: PlayingViewModel = LocalPlayingVM.current
) {
    val navToSongAction = SongDetailScreen.navToByArgv(
        hapticType = HapticFeedbackType.LongPress
    ) {
        popUpTo(ScreenData.Library.name)
    }

    val songs = album.songs
    val title = album.name
    val subTitle = album.artistName ?: ""
    val sortedItems = remember { songs.toMutableStateList() }

    val onSongSelected: (LSong) -> Unit = { song ->
        playingVM.playSongWithPlaylist(songs, song)
    }

    SmartContainer.LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AlbumCoverCard(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = 2.dp,
                imageData = { album },
                onClick = { }
            )
        }

        item {
            NavigatorHeader(title = title, subTitle = subTitle)
        }

        itemsIndexed(sortedItems) { index, item ->
            SongCard(
                song = { item },
                lyricRepository = playingVM.lyricRepository,
                onClick = { onSongSelected(item) },
                onLongClick = { navToSongAction(item.id) }
            )
        }
    }
}

@Composable
fun EmptyAlbumDetailScreen() {
    Text(text = "无法获取该专辑信息", modifier = Modifier.padding(20.dp))
}