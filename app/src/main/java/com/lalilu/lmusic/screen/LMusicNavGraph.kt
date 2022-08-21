package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.library.*
import com.lalilu.lmusic.screen.library.detail.*
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost

@ExperimentalAnimationApi
@Composable
@ExperimentalMaterialApi
fun LMusicNavGraph() {
    AnimatedNavHost(
        navController = LocalNavigatorHost.current,
        startDestination = MainScreenData.Library.name,
        modifier = Modifier.fillMaxSize(),
        exitTransition = { ExitTransition.None },
        enterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 300)) +
                    slideInVertically { 100 }
        }
    ) {
        composable(
            route = MainScreenData.Library.name
        ) {
            LibraryScreen()
        }

        composable(
            route = MainScreenData.Songs.name
        ) {
            SongsScreen()
        }

        composable(
            route = MainScreenData.Artists.name
        ) {
            ArtistScreen()
        }

        composable(
            route = MainScreenData.Albums.name
        ) {
            AlbumsScreen()
        }
        composable(
            route = MainScreenData.Playlists.name
        ) {
            PlaylistsScreen()
        }
        composable(
            route = "${MainScreenData.PlaylistsDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            // todo playlist 逻辑未完善
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()

            playlistId?.let {
                PlaylistDetailScreen(playlistId = it)
            }
        }
        composable(
            route = "${MainScreenData.SongsDetail.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)
                ?.let { SongDetailScreen(song = it) }
                ?: EmptySongDetailScreen()
        }

        composable(
            route = "${MainScreenData.ArtistsDetail.name}/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")
            Library.getArtistOrNull(artistName)
                ?.let { ArtistDetailScreen(artist = it) }
                ?: EmptyArtistDetailScreen()
        }

        composable(
            route = "${MainScreenData.AlbumsDetail.name}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")
            Library.getAlbumOrNull(albumId)
                ?.let { AlbumDetailScreen(album = it) }
                ?: EmptyAlbumDetailScreen()
        }
        composable(
            route = MainScreenData.SongsAddToPlaylist.name,
            arguments = listOf(navArgument("mediaIds") { type = NavType.StringArrayType })
        ) { backStackEntry ->
            val mediaIds = backStackEntry.arguments?.getStringArrayList("mediaIds")

            mediaIds?.takeIf { it.isNotEmpty() }?.let {
                PlaylistsScreen(
                    isAddingSongToPlaylist = true,
                    mediaIds = it
                )
            }
        }
        composable(
            route = "${MainScreenData.SongsAddToPlaylist.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            mediaId?.let {
                PlaylistsScreen(
                    isAddingSongToPlaylist = true,
                    mediaIds = listOf(it)
                )
            }
        }
        composable(
            route = "${MainScreenData.SongsMatchNetworkData.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)
                ?.let { MatchNetworkDataScreen(song = it) }
                ?: EmptySearchForLyricScreen()
        }
        composable(
            route = MainScreenData.Settings.name
        ) {
            SettingsScreen()
        }
    }
}

enum class MainScreenData(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val showNavigateButton: Boolean = false
) {
    Library(
        icon = R.drawable.ic_loader_line,
        title = R.string.destination_label_library,
        subTitle = R.string.destination_subtitle_library,
        showNavigateButton = true
    ),
    Songs(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_all_song,
        subTitle = R.string.destination_subtitle_all_song,
        showNavigateButton = true
    ),
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        showNavigateButton = true
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        showNavigateButton = true
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        showNavigateButton = true
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
        showNavigateButton = true
    ),
    PlaylistsDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail
    ),
    ArtistsDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist
    ),
    AlbumsDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail
    ),
    SongsDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail
    ),
    SongsAddToPlaylist(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_add_song_to_playlist,
        subTitle = R.string.destination_label_add_song_to_playlist
    ),
    SongsMatchNetworkData(
        icon = R.drawable.ic_music_line,
        title = R.string.destination_label_match_network_data,
        subTitle = R.string.destination_label_match_network_data
    );

    companion object {
        fun fromRoute(route: String?): MainScreenData? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }
    }
}