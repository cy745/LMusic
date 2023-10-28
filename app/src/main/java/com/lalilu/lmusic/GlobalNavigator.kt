package com.lalilu.lmusic

import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.lmusic.compose.component.BottomSheetNavigator
import com.lalilu.lmusic.compose.new_screen.SongDetailScreen
import com.lalilu.lmusic.compose.new_screen.SongsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object GlobalNavigator : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    /**
     * 跳转至某元素的详情页
     */
    fun goToDetailOf(
        navigator: BottomSheetNavigator? = NavigationWrapper.navigator,
        mediaId: String
    ) {
        navigator ?: return
        navigator.showSingle(SongDetailScreen(mediaId = mediaId))
    }

    fun showSongs(
        mediaIds: List<String>,
        title: String? = null,
        navigator: BottomSheetNavigator? = NavigationWrapper.navigator,
    ) {
        navigator ?: return
        navigator.showSingle(
            SongsScreen(
                title = title,
                mediaIds = mediaIds
            )
        )
    }
}