package com.lalilu.lmusic

import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.navigation.SheetNavigator
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
        navigator: SheetNavigator?,
    ) {
        val nav = navigator ?: NavigationWrapper.navigator ?: return
        nav.showSingle(SongDetailScreen(mediaId = mediaId))
    }

    override fun showSongs(
        mediaIds: List<String>,
        title: String?,
        navigator: SheetNavigator?,
    ) {
        val nav = navigator ?: NavigationWrapper.navigator ?: return
        nav.showSingle(
            SongsScreen(
                title = title,
                mediaIds = mediaIds
            )
        )
    }

    override fun navigateTo(
        screen: Screen,
        navigator: SheetNavigator?
    ) {
        val nav = navigator ?: NavigationWrapper.navigator ?: return
        nav.showSingle(screen = screen)
    }

    override fun goBack(navigator: SheetNavigator?) {
        val nav = navigator ?: NavigationWrapper.navigator ?: return
        nav.pop()
    }
}