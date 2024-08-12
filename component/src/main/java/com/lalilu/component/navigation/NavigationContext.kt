package com.lalilu.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.base.screen.ScreenType

val LocalNavigatorParent = compositionLocalOf<Navigator?> { null }
val LocalNavigatorBaseScreen = compositionLocalOf<Screen> { error("No base screen found") }
val LocalNavigatorKey = compositionLocalOf { "" }

private val screenNavigatorMap = mutableStateMapOf<Screen, Navigator>()

@Composable
fun Navigator.currentScreen(): State<Screen?> {
    return remember(this) {
        derivedStateOf {
            var screen = lastItemOrNull

            // 若该页面存在指向嵌套页面的路由
            while (screenNavigatorMap[screen] != null) {
                val temp = screenNavigatorMap[screen]
                    ?.lastItemOrNull

                if (temp is ScreenType.Empty) break
                else screen = temp
            }

            screen
        }
    }
}

@Composable
fun Navigator.previousScreen(): State<Screen?> {
    return remember(this) {
        derivedStateOf {
            val screens = items
                .flatMap { screenNavigatorMap[it]?.items ?: listOf(it) }

            screens.getOrNull(screens.size - 2)
        }
    }
}

@Composable
fun RegisterNavigator(screen: Screen, navigator: Navigator) {
    LaunchedEffect(screen, navigator) {
        screenNavigatorMap[screen] = navigator
    }
}