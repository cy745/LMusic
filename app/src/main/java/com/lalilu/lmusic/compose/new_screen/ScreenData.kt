package com.lalilu.lmusic.compose.new_screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lalilu.R
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.Destination
import com.lalilu.lmusic.compose.new_screen.destinations.DictionariesScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.DictionaryDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.FavouriteScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.HomeScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.NetDataScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SearchScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SettingsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongsScreenDestination

enum class ScreenData(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val destination: Destination
) {
    Home(
        icon = R.drawable.ic_loader_line,
        title = R.string.destination_label_library,
        subTitle = R.string.destination_subtitle_library,
        destination = HomeScreenDestination
    ),
    Songs(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_all_song,
        subTitle = R.string.destination_subtitle_all_song,
        destination = SongsScreenDestination
    ),
    Favourite(
        icon = R.drawable.ic_heart_3_line,
        title = R.string.destination_label_favourite,
        subTitle = R.string.destination_subtitle_favourite,
        destination = FavouriteScreenDestination
    ),
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        destination = PlaylistsScreenDestination
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        destination = AlbumsScreenDestination
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        destination = ArtistsScreenDestination
    ),
    Dictionaries(
        icon = R.drawable.ic_file_copy_2_line,
        title = R.string.destination_label_dictionaries,
        subTitle = R.string.destination_subtitle_dictionaries,
        destination = DictionariesScreenDestination
    ),
    Search(
        icon = R.drawable.ic_search_2_line,
        title = R.string.destination_label_search,
        subTitle = R.string.destination_subtitle_search,
        destination = SearchScreenDestination
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
        destination = SettingsScreenDestination
    ),
    PlaylistsDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail,
        destination = PlaylistDetailScreenDestination
    ),
    ArtistsDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist_detail,
        subTitle = R.string.destination_label_artist_detail,
        destination = ArtistDetailScreenDestination
    ),
    AlbumsDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail,
        destination = AlbumDetailScreenDestination
    ),
    DictionaryDetail(
        icon = R.drawable.ic_file_copy_2_line,
        title = R.string.destination_label_dictionary_detail,
        subTitle = R.string.destination_subtitle_dictionary_detail,
        destination = DictionaryDetailScreenDestination
    ),
    SongsDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail,
        destination = SongDetailScreenDestination
    ),
    NetData(
        icon = R.drawable.ic_music_line,
        title = R.string.destination_label_match_network_data,
        subTitle = R.string.destination_label_match_network_data,
        destination = NetDataScreenDestination
    );

    companion object {
        private val routeMap by lazy {
            values().associateBy { it.destination.baseRoute }
        }

        fun getOrNull(baseRoute: String): ScreenData? {
            return routeMap[baseRoute]
        }
    }
}