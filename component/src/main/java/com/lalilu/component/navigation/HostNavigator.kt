package com.lalilu.component.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.lalilu.component.base.EnhanceBottomSheetState
import com.lalilu.component.base.EnhanceModalSheetState
import com.lalilu.component.base.LocalEnhanceSheetState

@Composable
fun HostNavigator(
    startScreen: Screen,
    content: @Composable (Navigator) -> Unit = { CurrentScreen() }
) {
    val enhanceSheetState = LocalEnhanceSheetState.current

    Navigator(
        screen = startScreen,
        onBackPressed = null,
        disposeBehavior = NavigatorDisposeBehavior(
            disposeSteps = false,
            disposeNestedNavigators = false
        )
    ) { navigator ->
        LaunchedEffect(navigator, enhanceSheetState) {
            AppRouter.bind(navigator) {
                // 尝试跳转的时候若底部sheet不可见，则显示
                if (enhanceSheetState is EnhanceModalSheetState && !enhanceSheetState.isVisible) {
                    enhanceSheetState.show()
                }
            }
        }

        BackHandler(
            enabled = when (enhanceSheetState) {
                is EnhanceBottomSheetState -> !enhanceSheetState.isVisible && navigator.canPop
                is EnhanceModalSheetState -> enhanceSheetState.isVisible
                else -> false
            }
        ) {
            val actualNavigator = navigator.nestedNavigatorInLastScreen()
                ?.takeIf { it.canPop }
                ?: navigator

            when (enhanceSheetState) {
                is EnhanceBottomSheetState -> actualNavigator.pop()
                is EnhanceModalSheetState -> if (!actualNavigator.pop()) enhanceSheetState.hide()
                else -> {}
            }
        }

        content(navigator)
    }
}