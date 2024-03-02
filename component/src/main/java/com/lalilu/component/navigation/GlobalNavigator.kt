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

    /**
     * 跳转至某页面
     *
     * [screen]     目标页面
     * [singleTop]  是否替换栈顶的相同类型的页面
     * [navigator]  执行操作的导航器
     */
    fun navigateTo(
        screen: Screen,
        singleTop: Boolean = true,
        navigator: SheetNavigator? = null
    )

    fun goBack(
        navigator: SheetNavigator? = null
    )
}