package com.lalilu.component.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorContent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import cafe.adriel.voyager.navigator.compositionUniqueId
import com.lalilu.component.base.EnhanceBottomSheetState
import com.lalilu.component.base.EnhanceModalSheetState
import com.lalilu.component.base.LocalEnhanceSheetState

@OptIn(InternalVoyagerApi::class)
@Composable
fun Screen.NestedNavigator(
    startScreen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
    onBackPressed: OnBackPressed = null,
    key: String = compositionUniqueId(),
    content: NavigatorContent = { CurrentScreen() }
) {
    val parentNavigator = LocalNavigator.current
    val enhanceSheetState = LocalEnhanceSheetState.current

    CompositionLocalProvider(
        LocalNavigatorParent provides parentNavigator,
        LocalNavigatorKey provides key,
        LocalNavigatorBaseScreen provides this
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

            if (onBackPressed == null) {
                BackHandler(
                    enabled = when (enhanceSheetState) {
                        is EnhanceBottomSheetState -> !enhanceSheetState.isVisible && navigator.canPop
                        is EnhanceModalSheetState -> enhanceSheetState.isVisible && navigator.canPop
                        else -> false
                    }
                ) {
                    enhanceSheetState.apply {
                        when (this) {
                            is EnhanceBottomSheetState -> navigator.pop()
                            is EnhanceModalSheetState -> if (!navigator.pop()) hide()
                            else -> {}
                        }
                    }
                }
            }

            content(navigator)
        }
    }
}