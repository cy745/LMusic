package com.lalilu.lmusic.compose

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.lalilu.lmusic.compose.new_screen.NavGraphs
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
object NavigationWrapper {

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        navController: NavHostController = LocalNavigatorHost.current,
    ) {
        PagerWrapper.OnPagerChangeHandler { isCurrentPage ->
            navController.enableOnBackPressed(isCurrentPage)
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
        )
    }
}