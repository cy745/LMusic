package com.lalilu.lmusic.compose.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost


object ScreenActions {

    @Composable
    fun navToSongById(
        hapticType: HapticFeedbackType? = null,
        popUpToRoute: ScreenData? = null
    ): (String) -> Unit {
        return navigateWithPathArg(
            route = ScreenData.SongsDetail,
            hapticType = hapticType
        ) {
            popUpToRoute?.let { popUpTo(it.buildRoute()) }
            launchSingleTop = true
        }
    }

    @Composable
    fun navToAlbumById(hapticType: HapticFeedbackType? = null): (String) -> Unit {
        return navigateWithPathArg(
            route = ScreenData.AlbumsDetail,
            hapticType = hapticType
        ) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToArtistById(hapticType: HapticFeedbackType? = null): (String) -> Unit {
        return navigateWithPathArg(
            route = ScreenData.ArtistsDetail,
            hapticType = hapticType
        ) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToPlaylistById(hapticType: HapticFeedbackType? = null): (String) -> Unit {
        return navigateWithPathArg(
            route = ScreenData.PlaylistsDetail,
            hapticType = hapticType
        ) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToAddToPlaylist(hapticType: HapticFeedbackType? = null): (Map<String, Any?>) -> Unit {
        return navigateWithNullableArgs(
            route = ScreenData.Playlists,
            hapticType = hapticType
        ) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToNetData(
        hapticType: HapticFeedbackType? = null
    ): (String) -> Unit {
        return navigateWithPathArg(
            route = ScreenData.SongsMatchNetworkData,
            hapticType = hapticType
        ) {
            launchSingleTop = true
        }
    }

    @Composable
    fun navToSongs(
        hapticType: HapticFeedbackType? = null
    ): () -> Unit {
        return navigate(
            route = ScreenData.Songs,
            hapticType = hapticType
        ) {
            popUpTo(ScreenData.Library.buildRoute())
            launchSingleTop = true
        }
    }

    @Composable
    fun navToArtists(
        hapticType: HapticFeedbackType? = null
    ): () -> Unit {
        return navigate(
            route = ScreenData.Artists,
            hapticType = hapticType
        ) {
            popUpTo(ScreenData.Library.buildRoute())
            launchSingleTop = true
        }
    }

    @Composable
    fun navToAlbums(
        hapticType: HapticFeedbackType? = null
    ): () -> Unit {
        return navigate(
            route = ScreenData.Albums,
            hapticType = hapticType
        ) {
            popUpTo(ScreenData.Library.buildRoute())
            launchSingleTop = true
        }
    }

    @Composable
    fun navToSettings(
        hapticType: HapticFeedbackType? = null
    ): () -> Unit {
        return navigate(
            route = ScreenData.Settings,
            hapticType = hapticType
        ) {
            popUpTo(ScreenData.Library.buildRoute())
            launchSingleTop = true
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
        val currentRoute by controller.currentBackStackEntryAsState()
        return remember {
            {
                val targetRoute = route.buildRoute()

                if (route.sourceRoute != currentRoute?.destination?.route) {
                    hapticType?.let { type -> haptic.performHapticFeedback(type) }
                    controller.navigate(route = targetRoute, builder = { builder(controller) })
                }
            }
        }
    }


    @Composable
    fun navigateWithPathArg(
        route: ScreenData,
        hapticType: HapticFeedbackType? = null,
        builder: NavOptionsBuilder.(NavController) -> Unit = {}
    ): (String?) -> Unit {
        val haptic = LocalHapticFeedback.current
        val controller = LocalNavigatorHost.current
        return remember {
            {
                val targetRoute = route.buildRoute(mapOf(route.pathArg.first to it))

                hapticType?.let { type -> haptic.performHapticFeedback(type) }
                controller.navigate(route = targetRoute, builder = { builder(controller) })
            }
        }
    }

    @Composable
    fun navigateWithNullableArgs(
        route: ScreenData,
        hapticType: HapticFeedbackType? = null,
        builder: NavOptionsBuilder.(NavController) -> Unit = {}
    ): (Map<String, Any?>) -> Unit {
        val haptic = LocalHapticFeedback.current
        val controller = LocalNavigatorHost.current
        return remember {
            {
                val targetRoute = route.buildRoute(it)

                hapticType?.let { type -> haptic.performHapticFeedback(type) }
                controller.navigate(route = targetRoute, builder = { builder(controller) })
            }
        }
    }
}