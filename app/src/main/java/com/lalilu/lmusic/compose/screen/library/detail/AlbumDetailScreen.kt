package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.compose.component.card.AlbumCoverCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.SongsScreen
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.LocalSongsVM
import com.lalilu.lmusic.viewmodel.SongsViewModel

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AlbumDetailScreen(
    album: LAlbum,
    songsVM: SongsViewModel = LocalSongsVM.current,
) {
    LaunchedEffect(album) {
        songsVM.updateBySongs(album.songs)
    }

    SongsScreen(showAll = false) { songs, showSortBar ->
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
            NavigatorHeader(
                title = album.name,
                subTitle = album.artistName?.trim()
                    ?.let { "$it\n共 ${songs.size} 首歌曲" }
                    ?: "共 ${songs.size} 首歌曲"
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = dayNightTextColor(0.05f),
                    onClick = showSortBar
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.subtitle2,
                        color = dayNightTextColor(0.7f),
                        text = "排序"
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAlbumDetailScreen() {
    Text(text = "无法获取该专辑信息", modifier = Modifier.padding(20.dp))
}