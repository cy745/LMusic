package com.lalilu.lmusic.compose.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.*
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmusic.compose.component.navigate.NavigateBar
import com.lalilu.lmusic.compose.component.navigate.NavigateDetailBar


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

enum class ScreenData(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val showNavigateButton: Boolean = false,
    val fadeEdgeForStatusBar: Boolean = true,
    val pathArg: Pair<String, String?> = "" to "",
    val nullableArgs: Map<String, Any?> = emptyMap(),
    val mainBar: ComponentStrategy = ComponentStrategy.Clear,
    val extraBar: ComponentStrategy = ComponentStrategy.Clear,
    val mainBarForPad: ComponentStrategy = ComponentStrategy.Clear,
    val extraBarForPad: ComponentStrategy = ComponentStrategy.Clear
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
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
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
        nullableArgs = mapOf("isAdding" to false),
        mainBar = ComponentStrategy.Replace(LibraryNavigateBar)
    ),
    Albums(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_albums,
        subTitle = R.string.destination_subtitle_albums,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    Artists(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist,
        subTitle = R.string.destination_subtitle_artist,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
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
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    PlaylistsDetail(
        icon = R.drawable.ic_play_list_line,
        title = R.string.destination_label_playlist_detail,
        subTitle = R.string.destination_subtitle_playlist_detail,
        pathArg = "playlistId" to "",
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    ArtistsDetail(
        icon = R.drawable.ic_user_line,
        title = R.string.destination_label_artist_detail,
        subTitle = R.string.destination_label_artist_detail,
        pathArg = "artistName" to null,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    AlbumsDetail(
        icon = R.drawable.ic_album_fill,
        title = R.string.destination_label_album_detail,
        subTitle = R.string.destination_subtitle_album_detail,
        pathArg = "albumId" to null,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    ),
    SongsDetail(
        icon = R.drawable.ic_music_2_line,
        title = R.string.destination_label_song_detail,
        subTitle = R.string.destination_subtitle_song_detail,
        pathArg = "mediaId" to null,
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
        pathArg = "mediaId" to null,
        extraBar = ComponentStrategy.DoNothing,
        extraBarForPad = ComponentStrategy.DoNothing,
        mainBar = ComponentStrategy.Replace(LibraryDetailNavigateBar),
        mainBarForPad = ComponentStrategy.Replace(LibraryDetailNavigateBar)
    );

    val sourceRoute by lazy {
        var route = name
        pathArg.takeIf { it.first.isNotEmpty() }?.let { route += "/{${it.first}}" }
        nullableArgs.forEach {
            if (it.key.isNotEmpty()) route += "?${it.key}={${it.key}}"
        }
        return@lazy route
    }

    private val sourceArgument by lazy {
        val arguments = arrayListOf<NamedNavArgument>()
        pathArg.takeIf { it.first.isNotEmpty() }?.let {
            arguments.add(navArgument(it.first) {
                type = NavType.StringType
                defaultValue = it.second
                nullable = true
            })
        }
        nullableArgs.forEach {
            if (it.key.isNotEmpty()) {
                arguments.add(navArgument(it.key) {
                    nullable = true
                    defaultValue = it.value
                    type = when (it.value) {
                        is Boolean -> {
                            nullable = false
                            NavType.BoolType
                        }

                        is Int -> NavType.IntType
                        is Long -> NavType.LongType
                        is Double, is Float -> NavType.FloatType
                        is String -> NavType.StringType
                        else -> NavType.StringType
                    }
                })
            }
        }
        return@lazy arguments
    }

    @OptIn(ExperimentalAnimationApi::class)
    fun register(
        builder: NavGraphBuilder,
        content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
    ) {
        println("[Register]: $sourceRoute")
        builder.composable(route = sourceRoute, arguments = sourceArgument, content = content)
    }

    fun buildRoute(paramMap: Map<String, Any?> = emptyMap()): String {
        var targetRoute = name
        this.pathArg.takeIf { it.first.isNotEmpty() }?.let {
            targetRoute += "/${paramMap.getOrElse(it.first) { it.second }}"
        }
        this.nullableArgs.forEach {
            if (it.key.isNotEmpty()) {
                targetRoute += "?${it.key}=${paramMap.getOrElse(it.key) { it.value }}"
            }
        }
        return targetRoute
    }

    companion object {
        fun fromRoute(route: String?): ScreenData? {
            route ?: return null
            return values().find { it.sourceRoute == route }
        }
    }
}