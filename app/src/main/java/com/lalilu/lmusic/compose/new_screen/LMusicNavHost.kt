package com.lalilu.lmusic.compose.new_screen

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph(start = true)
@NavGraph
annotation class HomeNavGraph(
    val start: Boolean = false
)

@RootNavGraph
@NavGraph
annotation class FavouriteNavGraph(
    val start: Boolean = false
)

@RootNavGraph
@NavGraph
annotation class SearchNavGraph(
    val start: Boolean = false
)

@RootNavGraph
@NavGraph
annotation class PlaylistNavGraph(
    val start: Boolean = false
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun LMusicNavHost(
    modifier: Modifier,
    navController: NavHostController = LocalNavigatorHost.current
) {
    LaunchedEffect(Unit) {
        SmartBar.setMainBar(content = NavBar.content)
    }

    val animateEngine = rememberAnimatedNavHostEngine(
        rootDefaultAnimations = RootNavGraphDefaultAnimations(
            exitTransition = { ExitTransition.None },
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically { 100 } }
        )
    )
    DestinationsNavHost(
        modifier = modifier,
        navGraph = NavGraphs.root,
        navController = navController,
        engine = animateEngine
    ) {

    }
}
