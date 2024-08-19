package com.lalilu.component.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.base.screen.ScreenType

class ListDetailContainer(
    private val listScreen: Screen
) : Screen, ScreenType.ListHost {
    override val key: ScreenKey = "${super.key}:${listScreen.key}"

    @Composable
    override fun Content() {
        NestedNavigator(
            startScreen = listScreen,
        ) { navigator ->
            val windowSizeClass = LocalWindowSize.current
            val isPad = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

            val listContent = remember(navigator) {
                movableContentOf<Screen?> { screen ->
                    (screen ?: navigator.items.firstOrNull { it is ScreenType.List })?.let {
                        navigator.saveableState(key = "list", screen = it) {
                            it.Content()
                        }
                    }
                }
            }

            val detailContent = remember(navigator) {
                movableContentOf<Boolean> { isPad ->
                    CustomTransition(
                        navigator = navigator,
                        content = {
                            when {
                                isPad && it is ScreenType.List -> EmptyScreen.Content()
                                !isPad && it is ScreenType.List -> listContent(it)
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
                        content = { listContent(null) }
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