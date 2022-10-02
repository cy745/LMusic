package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.lalilu.R
import com.lalilu.lmusic.screen.component.NavigateBar
import com.lalilu.lmusic.screen.component.NavigateDetailBar


private val LibraryNavigateBar: @Composable () -> Unit = { NavigateBar() }
private val LibraryDetailNavigateBar: @Composable () -> Unit = { NavigateDetailBar() }


/**
 * 组件策略，用于指定应该如何处理组件
 */
sealed class ComponentStrategy {

    /**
     * 不处理策略
     */
    object DoNothing : ComponentStrategy()

    /**
     * 清除策略
     */
    object Clear : ComponentStrategy()

    /**
     * 替换策略，将内部的content替换
     */
    data class Replace(val content: @Composable () -> Unit) : ComponentStrategy()
}


enum class ScreenData(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val showNavigateButton: Boolean = false,
    val fadeEdgeForStatusBar: Boolean = true,
    val mainBar: ComponentStrategy = ComponentStrategy.Clear,
    val extraBar: ComponentStrategy = ComponentStrategy.Clear,
    val mainBarForPad: ComponentStrategy = ComponentStrategy.Clear,
    val extraBarForPad: ComponentStrategy = ComponentStrategy.Clear,
    val isChecked: MutableState<Boolean>? = if (showNavigateButton) mutableStateOf(false) else null
) {
    Library(
        icon = R.drawable.ic_loader_line,
        title = R.string.destination_label_library,
        subTitle = R.string.destination_subtitle_library,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Songs(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_all_song,
        subTitle = R.string.destination_subtitle_all_song,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    PlaylistsDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    ArtistsDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist_detail,
        subTitle = R.string.destination_label_artist_detail,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    AlbumsDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    SongsDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail,
        extraBar = ComponentStrategy.DoNothing,
        extraBarForPad = ComponentStrategy.DoNothing,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        fadeEdgeForStatusBar = false
    ),
    SongsAddToPlaylist(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_add_song_to_playlist,
        subTitle = R.string.destination_label_add_song_to_playlist,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    SongsMatchNetworkData(
        icon = R.drawable.ic_music_line,
        title = R.string.destination_label_match_network_data,
        subTitle = R.string.destination_label_match_network_data,
        extraBar = ComponentStrategy.DoNothing,
        extraBarForPad = ComponentStrategy.DoNothing,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    );


    companion object {
        fun fromRoute(route: String?): ScreenData? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }
    }
}