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
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.screen.library.AlbumsScreen
import com.lalilu.lmusic.compose.screen.library.ArtistScreen
import com.lalilu.lmusic.compose.screen.library.EmptySearchForLyricScreen
import com.lalilu.lmusic.compose.screen.library.LibraryScreen
import com.lalilu.lmusic.compose.screen.library.MatchNetworkDataScreen
import com.lalilu.lmusic.compose.screen.library.PlaylistsScreen
import com.lalilu.lmusic.compose.screen.library.SearchScreen
import com.lalilu.lmusic.compose.screen.library.SettingsScreen
import com.lalilu.lmusic.compose.screen.library.SongsScreen
import com.lalilu.lmusic.compose.screen.library.detail.AlbumDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.ArtistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.EmptyAlbumDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.EmptyArtistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.EmptyPlaylistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.EmptySongDetailScreen
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
            LibraryScreen()
        }

        composable(
            route = ScreenData.Songs.name
        ) {
            SongsScreen()
        }

        composable(
            route = ScreenData.Favourite.name
        ) {
            PlaylistDetailScreen(playlistId = 0)
        }

        composable(
            route = ScreenData.Artists.name
        ) {
            ArtistScreen()
        }

        composable(
            route = ScreenData.Albums.name
        ) {
            AlbumsScreen()
        }

        composable(
            route = ScreenData.Search.name
        ) {
            SearchScreen()
        }

        composable(
            route = ScreenData.Settings.name
        ) {
            SettingsScreen()
        }

        composable(
            route = "${ScreenData.Playlists.name}?isAdding={isAdding}",
            arguments = listOf(navArgument("isAdding") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val isAddingSongs = backStackEntry.arguments?.getBoolean("isAdding") ?: false

            PlaylistsScreen(isAddingSongs)
        }

        composable(
            route = "${ScreenData.PlaylistsDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()
            playlistId?.let {
                PlaylistDetailScreen(playlistId = it)
            } ?: EmptyPlaylistDetailScreen()
        }

        composable(
            route = "${ScreenData.SongsDetail.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)?.let {
                SongDetailScreen(song = it)
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
    }
}
