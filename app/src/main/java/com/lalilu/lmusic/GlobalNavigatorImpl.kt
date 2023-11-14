package com.lalilu.lmusic

import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.component.base.DialogScreen
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lmusic.compose.DialogWrapper
import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.lmusic.compose.new_screen.SongDetailScreen
import com.lalilu.lmusic.compose.new_screen.SongsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object GlobalNavigatorImpl : GlobalNavigator, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    /**
     * 跳转至某元素的详情页
     */
    override fun goToDetailOf(
        mediaId: String,
        navigator: BottomSheetNavigator?,
    ) {
        val nav = navigator ?: NavigationWrapper.navigator ?: return
        nav.showSingle(SongDetailScreen(mediaId = mediaId))
    }

    override fun showSongs(
        mediaIds: List<String>,
        title: String?,
        navigator: BottomSheetNavigator?,
    ) {
        val nav = navigator ?: NavigationWrapper.navigator ?: return
        nav.showSingle(
            SongsScreen(
                title = title,
                mediaIds = mediaIds
            )
        )
    }

    override fun navigateTo(screen: Screen, navigator: BottomSheetNavigator?) {
        val nav = navigator
            ?: (if (screen is DialogScreen) DialogWrapper.navigator else NavigationWrapper.navigator)
            ?: return
        nav.showSingle(
            screen = screen,
            replaceAllWhenInvisible = screen is DialogScreen
        )
    }
}