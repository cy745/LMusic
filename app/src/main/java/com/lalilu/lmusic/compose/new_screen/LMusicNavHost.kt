package com.lalilu.lmusic.compose.new_screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
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
fun LMusicNavHost(
    navController: NavHostController = LocalNavigatorHost.current
) {
    DestinationsNavHost(navGraph = NavGraphs.root, navController = navController) {

    }
}
