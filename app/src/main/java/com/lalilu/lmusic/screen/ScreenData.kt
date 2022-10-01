package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.lalilu.R
import com.lalilu.lmusic.screen.component.NavigateBar
import com.lalilu.lmusic.screen.component.NavigateDetailBar

private val LibraryNavigateBar: @Composable () -> Unit = { NavigateBar() }
private val LibraryDetailNavigateBar: @Composable () -> Unit = { NavigateDetailBar() }

enum class ScreenData(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val showNavigateButton: Boolean = false,
    val fadeEdgeForStatusBar: Boolean = true,
    val mainBarContent: @Composable (() -> Unit)? = null,
    val extraBarContent: @Composable (() -> Unit)? = null
) {
    Library(
        icon = R.drawable.ic_loader_line,
        title = R.string.destination_label_library,
        subTitle = R.string.destination_subtitle_library,
        showNavigateButton = true,
        mainBarContent = LibraryNavigateBar
    ),
    Songs(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_all_song,
        subTitle = R.string.destination_subtitle_all_song,
        showNavigateButton = true,
        mainBarContent = LibraryNavigateBar
    ),
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        showNavigateButton = false,
        mainBarContent = LibraryNavigateBar
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        showNavigateButton = true,
        mainBarContent = LibraryNavigateBar
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        showNavigateButton = true,
        mainBarContent = LibraryNavigateBar
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
        showNavigateButton = true,
        mainBarContent = LibraryNavigateBar
    ),
    PlaylistsDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail,
        mainBarContent = LibraryDetailNavigateBar
    ),
    ArtistsDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist_detail,
        subTitle = R.string.destination_label_artist_detail,
        mainBarContent = LibraryDetailNavigateBar
    ),
    AlbumsDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail,
        mainBarContent = LibraryDetailNavigateBar
    ),
    SongsDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail,
        mainBarContent = LibraryDetailNavigateBar,
        fadeEdgeForStatusBar = false
    ),
    SongsAddToPlaylist(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_add_song_to_playlist,
        subTitle = R.string.destination_label_add_song_to_playlist,
        mainBarContent = LibraryDetailNavigateBar
    ),
    SongsMatchNetworkData(
        icon = R.drawable.ic_music_line,
        title = R.string.destination_label_match_network_data,
        subTitle = R.string.destination_label_match_network_data,
        mainBarContent = LibraryDetailNavigateBar
    );


    companion object {
        fun fromRoute(route: String?): ScreenData? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }
    }
}