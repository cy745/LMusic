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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.compose.component.card.AlbumCoverCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.SongsScreen
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.SongsViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalAnimationApi::class)
object AlbumDetailScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.AlbumsDetail.name}?albumId={albumId}",
            arguments = listOf(navArgument("albumId") {})
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")

            LMedia.getAlbumOrNull(albumId)
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
    songsVM: SongsViewModel = get(),
) {
    val sortFor = remember { "AlbumDetail" }

    LaunchedEffect(album) {
        songsVM.updateBySongs(
            songs = album.songs,
            sortFor = sortFor
        )
    }

    SongsScreen(
        showAll = false,
        sortFor = sortFor
    ) { songs, showSortBar ->
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