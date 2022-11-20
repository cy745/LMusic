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
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmedia.repository.FavoriteRepository
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
        ScreenData.Library.register(this) {
            LibraryScreen()
        }
        ScreenData.Songs.register(this) {
            SongsScreen()
        }
        ScreenData.Favourite.register(this) {
            PlaylistDetailScreen(playlistId = FavoriteRepository.FAVORITE_PLAYLIST_ID)
        }
        ScreenData.Artists.register(this) {
            ArtistScreen()
        }
        ScreenData.Albums.register(this) {
            AlbumsScreen()
        }
        ScreenData.Search.register(this) {
            SearchScreen()
        }
        ScreenData.Settings.register(this) {
            SettingsScreen()
        }
        ScreenData.Playlists.register(this) {
            val isAddingSongs = it.arguments?.getBoolean("isAdding") ?: false
            PlaylistsScreen(isAddingSongs)
        }
        ScreenData.PlaylistsDetail.register(this) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()
            playlistId?.let {
                PlaylistDetailScreen(playlistId = it)
            } ?: EmptyPlaylistDetailScreen()
        }
        ScreenData.SongsDetail.register(this) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)?.let {
                SongDetailScreen(song = it)
            } ?: EmptySongDetailScreen()
        }
        ScreenData.ArtistsDetail.register(this) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")
            Library.getArtistOrNull(artistName)
                ?.let { ArtistDetailScreen(artist = it) }
                ?: EmptyArtistDetailScreen()
        }
        ScreenData.AlbumsDetail.register(this) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")
            Library.getAlbumOrNull(albumId)
                ?.let { AlbumDetailScreen(album = it) }
                ?: EmptyAlbumDetailScreen()
        }
        ScreenData.SongsMatchNetworkData.register(this) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)
                ?.let { MatchNetworkDataScreen(song = it) }
                ?: EmptySearchForLyricScreen()
        }
    }
}
