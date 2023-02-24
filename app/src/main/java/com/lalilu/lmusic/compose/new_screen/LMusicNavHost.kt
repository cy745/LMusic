package com.lalilu.lmusic.compose.new_screen

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph(start = true)
@NavGraph
annotation class HomeNavGraph(
    val start: Boolean = false
)

@NavGraph
annotation class FavouriteNavGraph(
    val start: Boolean = false
)

@NavGraph
annotation class SearchNavGraph(
    val start: Boolean = false
)

@NavGraph
annotation class PlaylistNavGraph(
    val start: Boolean = false
)

@Composable
fun LMusicNavHost() {
    DestinationsNavHost(navGraph = NavGraphs.root) {

    }
}
