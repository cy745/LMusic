package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.library.*
import com.lalilu.lmusic.screen.library.detail.*
import com.lalilu.lmusic.utils.LocalNavigatorHost
import com.lalilu.lmusic.utils.rememberWindowSizeClass


/**
 * 递归清除返回栈
 */
fun NavController.clearBackStack() {
    if (popBackStack()) clearBackStack()
}

/**
 * @param from  所设起点导航位置
 * @param to    目标导航位置
 *
 * 指定导航起点位置和目标位置
 */
fun NavController.navigate(
    to: String,
    from: String = MainScreenData.Library.name,
    clearAllBefore: Boolean = false,
    singleTop: Boolean = true
) {
    if (clearAllBefore) clearBackStack()
    navigate(to) {
        launchSingleTop = singleTop
        popUpTo(from) { inclusive = from == to }
    }
}

@ExperimentalAnimationApi
@Composable
@ExperimentalMaterialApi
fun LMusicNavGraph(
    modifier: Modifier = Modifier,
    contentPaddingForFooter: Dp = 0.dp,
    onExpendModal: () -> Unit = {},
) {
    val navController = LocalNavigatorHost.current
    val currentWindowSizeClass = rememberWindowSizeClass()
    val currentWindowSize = currentWindowSizeClass.windowSize

    AnimatedNavHost(
        navController = navController,
        startDestination = MainScreenData.Library.name,
        modifier = modifier,
        exitTransition = { ExitTransition.None },
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 700,
                )
            ) + slideInVertically { 100 }
        }
    ) {
        composable(
            route = MainScreenData.Library.name
        ) {
            LibraryScreen(
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Songs.name
        ) {
            SongsScreen(
                currentWindowSize = currentWindowSize,
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Artists.name
        ) {
            ArtistScreen(
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Albums.name
        ) {
            AlbumsScreen(
                currentWindowSize = currentWindowSize,
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }
        composable(
            route = MainScreenData.Playlists.name
        ) {
            PlaylistsScreen(
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }
        composable(
            route = "${MainScreenData.PlaylistsDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()

            playlistId?.let {
                PlaylistDetailScreen(
                    playlistId = it,
                    currentWindowSize = currentWindowSize,
                    navigateTo = navController::navigate,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            }
        }
        composable(
            route = "${MainScreenData.SongsDetail.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")
            Library.getSongOrNull(mediaId)?.let {
                SongDetailScreen(
                    song = it,
                    currentWindowSize = currentWindowSize,
                    navigateTo = navController::navigate
                )
            } ?: EmptySongDetailScreen()
        }

        composable(
            route = "${MainScreenData.ArtistsDetail.name}/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName")

            artistName?.let { name ->
                ArtistDetailScreen(
                    artistName = name,
                    currentWindowSize = currentWindowSize,
                    navigateTo = navController::navigate,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            } ?: EmptyArtistDetailScreen()
        }

        composable(
            route = "${MainScreenData.AlbumsDetail.name}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")
            Library.getAlbumOrNull(albumId)?.let {
                AlbumDetailScreen(
                    album = it,
                    currentWindowSize = currentWindowSize,
                    navigateTo = navController::navigate,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            } ?: EmptyAlbumDetailScreen()
        }
        composable(
            route = MainScreenData.SongsAddToPlaylist.name,
            arguments = listOf(navArgument("mediaIds") { type = NavType.StringArrayType })
        ) { backStackEntry ->
            val mediaIds = backStackEntry.arguments?.getStringArrayList("mediaIds")

            mediaIds?.takeIf { it.isNotEmpty() }?.let {
                PlaylistsScreen(
                    navigateUp = navController::navigateUp,
                    contentPaddingForFooter = contentPaddingForFooter,
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
                    navigateUp = navController::navigateUp,
                    contentPaddingForFooter = contentPaddingForFooter,
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
            Library.getSongOrNull(mediaId)?.let {
                MatchNetworkDataScreen(
                    song = it,
                    navigateUp = navController::navigateUp,
                    expendScaffold = onExpendModal,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            } ?: EmptySearchForLyricScreen()
        }
        composable(
            route = MainScreenData.Settings.name
        ) {
            SettingsScreen(
                currentWindowSize = currentWindowSize,
                contentPaddingForFooter = contentPaddingForFooter
            )
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