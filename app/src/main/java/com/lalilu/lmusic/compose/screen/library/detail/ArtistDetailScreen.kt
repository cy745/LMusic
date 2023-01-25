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
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.SongsScreen
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.LocalSongsVM
import com.lalilu.lmusic.viewmodel.SongsViewModel

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ArtistDetailScreen(
    artist: LArtist,
    songsVM: SongsViewModel = LocalSongsVM.current
) {
    val sortFor = remember { "ArtistDetail" }

    LaunchedEffect(artist) {
        songsVM.updateBySongs(
            songs = artist.songs,
            sortFor = sortFor
        )
    }

    SongsScreen(
        showAll = false,
        sortFor = sortFor
    ) { songs, showSortBar ->
        item {
            NavigatorHeader(
                title = artist.name,
                subTitle = "共 ${songs.size} 首歌曲"
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
fun EmptyArtistDetailScreen() {
    Text(text = "无法获取该歌手信息", modifier = Modifier.padding(20.dp))
}
