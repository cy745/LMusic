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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.screen.library.*
import com.lalilu.lmusic.compose.screen.library.detail.*
import com.lalilu.lmusic.screen.library.SettingsScreen
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.NetworkDataViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel

@ExperimentalAnimationApi
@Composable
@ExperimentalMaterialApi
fun LMusicNavGraph(
    navHostController: NavHostController = LocalNavigatorHost.current,
    mainViewModel: MainViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playlistsViewModel: PlaylistsViewModel = hiltViewModel(),
    networkDataViewModel: NetworkDataViewModel = hiltViewModel()
) {
    val windowSize = LocalWindowSize.current
    val configuration = LocalConfiguration.current
    val currentRoute = navHostController.currentBackStackEntryAsState().value

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
        composable(
            route = ScreenData.Library.name
        ) {
            LibraryScreen(libraryViewModel)
        }

        composable(
            route = ScreenData.Songs.name
        ) {
            SongsScreen(mainViewModel, libraryViewModel)
        }

        composable(
            route = ScreenData.Artists.name
        ) {
            ArtistScreen(libraryViewModel)
        }

        composable(
            route = ScreenData.Albums.name
        ) {
            AlbumsScreen(libraryViewModel)
        }

        composable(
            route = ScreenData.Playlists.name
        ) {
            PlaylistsScreen(mainViewModel, playlistsViewModel, libraryViewModel)
        }

        composable(
            route = "${ScreenData.PlaylistsDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()
            playlistId?.let {
                PlaylistDetailScreen(
                    playlistId = it,
                    playlistVM = playlistsViewModel,
                    mainViewModel = mainViewModel
                )
            } ?: EmptyPlaylistDetailScreen()
        }

        composable(
            route = "${ScreenData.SongsDetail.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)?.let {
                SongDetailScreen(
                    song = it,
                    mainViewModel = mainViewModel,
                    networkDataViewModel = networkDataViewModel,
                    playlistsVM = playlistsViewModel
                )
            } ?: EmptySongDetailScreen()
        }

        composable(
            route = "${ScreenData.ArtistsDetail.name}/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")
            Library.getArtistOrNull(artistName)
                ?.let { ArtistDetailScreen(artist = it) }
                ?: EmptyArtistDetailScreen()
        }

        composable(
            route = "${ScreenData.AlbumsDetail.name}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")
            Library.getAlbumOrNull(albumId)
                ?.let { AlbumDetailScreen(album = it) }
                ?: EmptyAlbumDetailScreen()
        }

        composable(
            route = "${ScreenData.SongsMatchNetworkData.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)
                ?.let { MatchNetworkDataScreen(song = it) }
                ?: EmptySearchForLyricScreen()
        }

        composable(
            route = ScreenData.Settings.name
        ) {
            SettingsScreen()
        }
    }
}
