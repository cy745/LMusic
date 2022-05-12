package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lalilu.R
import com.lalilu.lmusic.datasource.*
import com.lalilu.lmusic.screen.detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavHostController.clearBackStack() {
    if (popBackStack()) clearBackStack()
}

@ExperimentalAnimationApi
@Composable
@ExperimentalMaterialApi
fun ComposeNavigator(
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController,
    scaffoldState: ModalBottomSheetState,
    mediaSource: MMediaSource,
    contentPaddingForFooter: Dp = 0.dp,
) {
    val expendScaffold: () -> Unit = {
        scope.launch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    }

    NavHost(
        navController = navController,
        startDestination = MainScreenData.Library.name,
        modifier = modifier
    ) {
        composable(
            route = MainScreenData.Library.name
        ) {
            LibraryScreen(
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.AllSongs.name
        ) {
            AllSongsScreen(
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Artist.name
        ) {
            val artists = mediaSource.getChildren(ARTIST_ID) ?: emptyList()
            ArtistScreen(
                artists = artists,
                navigateTo = navController::navigate,
                contentPaddingForFooter = contentPaddingForFooter
            )
        }

        composable(
            route = MainScreenData.Albums.name
        ) {
            val albums = mediaSource.getChildren(ALBUM_ID) ?: emptyList()
            AlbumsScreen(
                albums = albums,
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
            route = "${MainScreenData.PlaylistDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong()

            playlistId?.let {
                PlaylistDetailScreen(
                    playlistId = it,
                    navigateTo = navController::navigate,
                    contentPaddingForFooter = contentPaddingForFooter
                )
            }
        }
        composable(
            route = "${MainScreenData.SongDetail.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            mediaId?.let { id ->
                mediaSource.getItemById(ITEM_PREFIX + id)?.also {
                    SongDetailScreen(
                        mediaItem = it,
                        navigateTo = navController::navigate
                    )
                }
            } ?: EmptySongDetailScreen()
        }

        composable(
            route = "${MainScreenData.ArtistDetail.name}/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId")

            artistId?.let { id ->
                println(mediaSource.getItemById(ARTIST_PREFIX + id))
                mediaSource.getItemById(ARTIST_PREFIX + id)?.let { artist ->
                    mediaSource.getChildren(ARTIST_PREFIX + id)?.also { songs ->
                        ArtistDetailScreen(
                            artist = artist,
                            songs = songs,
                            navigateTo = navController::navigate,
                            contentPaddingForFooter = contentPaddingForFooter
                        )
                    }
                }
            } ?: EmptyArtistDetailScreen()
        }

        composable(
            route = "${MainScreenData.AlbumDetail.name}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")

            albumId?.let { id ->
                mediaSource.getItemById(ALBUM_PREFIX + id)?.let { album ->
                    mediaSource.getChildren(ALBUM_PREFIX + id)?.also { songs ->
                        AlbumDetailScreen(
                            album = album,
                            songs = songs,
                            navigateTo = navController::navigate,
                            contentPaddingForFooter = contentPaddingForFooter
                        )
                    }
                }
            } ?: EmptyAlbumDetailScreen()
        }
        composable(
            route = MainScreenData.AddToPlaylist.name,
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
            route = "${MainScreenData.AddToPlaylist.name}/{mediaId}",
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
            route = "${MainScreenData.SearchForLyric.name}/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            mediaId?.let { id ->
                mediaSource.getItemById(ITEM_PREFIX + id)?.also {
                    SearchForLyricScreen(
                        mediaItem = it,
                        navigateUp = navController::navigateUp,
                        expendScaffold = expendScaffold,
                        contentPaddingForFooter = contentPaddingForFooter
                    )
                }
            } ?: EmptySearchForLyricScreen()
        }
        composable(
            route = MainScreenData.Settings.name
        ) {
            SettingsScreen(
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
        subTitle = R.string.destination_subtitle_library
    ),
    AllSongs(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_all_song,
        subTitle = R.string.destination_subtitle_all_song,
        showNavigateButton = true
    ),
    Artist(
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
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        showNavigateButton = true
    ),
    PlaylistDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail
    ),
    ArtistDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist
    ),
    AlbumDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail
    ),
    SongDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail
    ),
    AddToPlaylist(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_add_song_to_playlist,
        subTitle = R.string.destination_label_add_song_to_playlist
    ),
    SearchForLyric(
        icon = R.drawable.ic_lrc_fill,
        title = R.string.destination_label_search_for_lyric,
        subTitle = R.string.destination_label_search_for_lyric
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
        showNavigateButton = true
    );

    companion object {
        fun fromRoute(route: String?): MainScreenData? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }
    }
}