package com.lalilu.lmusic.compose.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.lalilu.R
import com.lalilu.lmusic.compose.component.navigate.NavigateBar
import com.lalilu.lmusic.compose.component.navigate.NavigateDetailBar
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost


val LibraryNavigateBar: @Composable () -> Unit = { NavigateBar() }
val LibraryDetailNavigateBar: @Composable () -> Unit = { NavigateDetailBar() }


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

object ScreenActions {
    @Composable
    fun navToPlaylist(): (String) -> Unit {
        return navigate(route = ScreenData.PlaylistsDetail.name)
    }

    @Composable
    fun navToArtist(): (String) -> Unit {
        return navigate(route = ScreenData.ArtistsDetail.name)
    }

    @Composable
    fun navToAddToPlaylist(): (String) -> Unit {
        return navigate(route = ScreenData.Playlists.name, useNullableArg = true)
    }

    @Composable
    fun navToNetworkMatch(): (String) -> Unit {
        return navigate(route = ScreenData.SongsMatchNetworkData.name)
    }

    @Composable
    fun navToAlbum(): (String) -> Unit {
        return navigate(route = ScreenData.AlbumsDetail.name) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToSong(hapticType: HapticFeedbackType? = null): (String) -> Unit {
        return navigate(route = ScreenData.SongsDetail.name, hapticType = hapticType) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToSongFromLibrary(hapticType: HapticFeedbackType? = null): (String) -> Unit {
        return navigate(route = ScreenData.SongsDetail.name, hapticType = hapticType) {
            popUpTo(ScreenData.Library.name) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    @Composable
    fun navigate(
        route: String,
        useNullableArg: Boolean = false,
        hapticType: HapticFeedbackType? = null,
        builder: NavOptionsBuilder.(NavController) -> Unit = {}
    ): (String) -> Unit {
        val haptic = LocalHapticFeedback.current
        val controller = LocalNavigatorHost.current
        return remember {
            {
                hapticType?.let { type -> haptic.performHapticFeedback(type) }
                controller.navigate(
                    route = "$route${if (useNullableArg) "?" else "/"}$it",
                    builder = { builder(controller) })
            }
        }
    }

    @Composable
    fun navigate(
        route: ScreenData,
        hapticType: HapticFeedbackType? = null,
        builder: NavOptionsBuilder.(NavController) -> Unit = {}
    ): () -> Unit {
        val haptic = LocalHapticFeedback.current
        val controller = LocalNavigatorHost.current
        return remember {
            {
                hapticType?.let { type -> haptic.performHapticFeedback(type) }
                controller.navigate(route.name, builder = { builder(controller) })
            }
        }
    }
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
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Favourite(
        icon = R.drawable.ic_heart_3_line,
        title = R.string.destination_label_favourite,
        subTitle = R.string.destination_subtitle_favourite,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Playlists(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlists,
        subTitle = R.string.destination_subtitle_playlists,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Search(
        icon = R.drawable.ic_search_2_line,
        title = R.string.destination_label_search,
        subTitle = R.string.destination_subtitle_search,
        showNavigateButton = true,
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Settings(
        icon = R.drawable.ic_settings_4_line,
        title = R.string.destination_label_settings,
        subTitle = R.string.destination_subtitle_settings,
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