package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.EmptyScreen
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.navigation.NestedNavigator
import com.lalilu.lmusic.compose.component.CustomTransition
import com.zhangke.krouter.KRouter
import com.zhangke.krouter.annotation.Destination
import com.zhangke.krouter.annotation.Param

@Destination("/pages/songs")
data class SongsScreen(
    @Param(required = true, name = KRouter.PRESET_ROUTER)
    private val router: String = "/pages/songs",
    private val title: String? = null,
    private val mediaIds: List<String> = emptyList()
) : Screen, ScreenInfoFactory, ScreenType.List {

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = R.string.screen_title_songs,
            icon = R.drawable.ic_music_2_line
        )
    }

    @Composable
    override fun Content() {
        val windowSizeClass = LocalWindowSize.current
        val isPad = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

        // TODO 待检查从下一级路由返回时该嵌套路由被重置的问题
        NestedNavigator(
            key = router,
            startScreen = SubsSongsScreen(title, mediaIds),
        ) { navigator ->
            LaunchedEffect(navigator) {
                AppRouter.bindFor(router).collect { intent ->
                    when (intent) {
                        NavIntent.Pop -> navigator.pop()
                        is NavIntent.Push -> navigator.push(intent.screen)
                        is NavIntent.Replace -> navigator.replace(intent.screen)
                        is NavIntent.Jump -> {
                            // 此处完善自定义跳转逻辑
                            navigator.push(intent.screen)
                        }
                    }
                }
            }

            val listContent = remember(navigator) {
                movableContentOf {
                    navigator.items
                        .firstOrNull { it is ScreenType.List }
                        ?.let {
                            navigator.saveableState(key = "list", screen = it) {
                                it.Content()
                            }
                        }
                }
            }

            val detailContent = remember {
                movableContentOf<Boolean> { isPad ->
                    CustomTransition(
                        navigator = navigator,
                        content = {
                            when {
                                isPad && it is ScreenType.List -> EmptyScreen.Content()
                                !isPad && it is ScreenType.List -> listContent()
                                else -> navigator.saveableState(
                                    screen = it,
                                    key = "transition",
                                    content = { it.Content() }
                                )
                            }
                        }
                    )
                }
            }

            if (isPad) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(360.dp),
                        content = { listContent() }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        content = { detailContent(true) }
                    )
                }
            } else {
                detailContent(false)
            }
        }
    }
}