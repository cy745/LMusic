package com.lalilu.component.navigation

import cafe.adriel.voyager.core.screen.Screen

interface GlobalNavigator {

    /**
     * 跳转至某元素的详情页
     */
    fun goToDetailOf(
        mediaId: String,
        navigator: SheetNavigator? = null
    )

    /**
     * 展示一些歌曲
     */
    fun showSongs(
        mediaIds: List<String>,
        title: String? = null,
        navigator: SheetNavigator? = null
    )

    fun navigateTo(
        screen: Screen,
        navigator: SheetNavigator? = null
    )
}