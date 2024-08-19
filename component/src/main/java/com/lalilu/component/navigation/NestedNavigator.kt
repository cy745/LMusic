package com.lalilu.component.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorContent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import cafe.adriel.voyager.navigator.compositionUniqueId

@OptIn(InternalVoyagerApi::class)
@Composable
fun Screen.NestedNavigator(
    startScreen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
    onBackPressed: OnBackPressed = null,
    key: String = compositionUniqueId(),
    content: NavigatorContent = { CurrentScreen() }
) {
    Navigator(
        screen = startScreen,
        disposeBehavior = disposeBehavior,
        onBackPressed = onBackPressed,
        key = key,
    ) { navigator ->
        RegisterNavigator(
            screen = this,
            navigator = navigator
        )
        content(navigator)
    }
}