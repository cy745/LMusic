package com.lalilu.lmusic.compose.screen

import android.content.res.Configuration
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.screen.library.AlbumsScreen
import com.lalilu.lmusic.compose.screen.library.ArtistScreen
import com.lalilu.lmusic.compose.screen.library.LibraryScreen
import com.lalilu.lmusic.compose.screen.library.MatchNetworkDataScreen
import com.lalilu.lmusic.compose.screen.library.PlaylistsScreen
import com.lalilu.lmusic.compose.screen.library.SearchScreen
import com.lalilu.lmusic.compose.screen.library.SettingsScreen
import com.lalilu.lmusic.compose.screen.library.SongsScreen
import com.lalilu.lmusic.compose.screen.library.detail.AlbumDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.ArtistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.FavouriteScreen
import com.lalilu.lmusic.compose.screen.library.detail.PlaylistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.SongDetailScreen
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad

@ExperimentalAnimationApi
@Composable
@ExperimentalMaterialApi
fun LMusicNavGraph(
    navHostController: NavHostController = LocalNavigatorHost.current
) {
    val windowSize = LocalWindowSize.current
    val configuration = LocalConfiguration.current
    val currentRoute by navHostController.currentBackStackEntryAsState()

    val isPad by windowSize.rememberIsPad()
    val isLandscape = remember(configuration.orientation) {
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    // 根据当前路径更新SmartBar的内容
    LaunchedEffect(currentRoute, isLandscape) {
        val current = ScreenData.fromRoute(currentRoute?.destination?.route)
            ?: return@LaunchedEffect

        if (isPad && isLandscape) {
            when (current.mainBarForPad) {
                ComponentStrategy.DoNothing -> Unit
                ComponentStrategy.Clear -> SmartBar.setMainBar(item = null)
                is ComponentStrategy.Replace -> SmartBar.setMainBar(item = current.mainBarForPad.content)
            }
            when (current.extraBarForPad) {
                ComponentStrategy.DoNothing -> Unit
                ComponentStrategy.Clear -> SmartBar.setExtraBar(item = null)
                is ComponentStrategy.Replace -> SmartBar.setExtraBar(item = current.extraBarForPad.content)
            }
        } else {
            when (current.mainBar) {
                ComponentStrategy.DoNothing -> Unit
                ComponentStrategy.Clear -> SmartBar.setMainBar(item = null)
                is ComponentStrategy.Replace -> SmartBar.setMainBar(item = current.mainBar.content)
            }
            when (current.extraBar) {
                ComponentStrategy.DoNothing -> Unit
                ComponentStrategy.Clear -> SmartBar.setExtraBar(item = null)
                is ComponentStrategy.Replace -> SmartBar.setExtraBar(item = current.extraBar.content)
            }
        }
        SmartModalBottomSheet.fadeEdge(current.fadeEdgeForStatusBar)
    }

    AnimatedNavHost(
        navController = navHostController,
        startDestination = ScreenData.Library.name,
        modifier = Modifier.fillMaxSize(),
        exitTransition = { ExitTransition.None },
        enterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically { 100 }
        }
    ) {
        SongsScreen.register(builder = this)
        ArtistScreen.register(builder = this)
        AlbumsScreen.register(builder = this)
        SearchScreen.register(builder = this)
        LibraryScreen.register(builder = this)
        SettingsScreen.register(builder = this)
        PlaylistsScreen.register(builder = this)
        FavouriteScreen.register(builder = this)
        SongDetailScreen.register(builder = this)
        AlbumDetailScreen.register(builder = this)
        ArtistDetailScreen.register(builder = this)
        PlaylistDetailScreen.register(builder = this)
        MatchNetworkDataScreen.register(builder = this)
    }
}
