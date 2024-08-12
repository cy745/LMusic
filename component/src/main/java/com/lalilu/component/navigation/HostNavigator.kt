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
import com.lalilu.component.base.TabScreen

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
        LaunchedEffect(navigator) {
            AppRouter.bindFor().collect { intent ->
                when (intent) {
                    NavIntent.Pop -> navigator.pop()
                    is NavIntent.Push -> navigator.push(intent.screen)
                    is NavIntent.Replace -> navigator.replace(intent.screen)
                    is NavIntent.Jump -> {
                        val screen = intent.screen
                        when {
                            // Tab类型页面
                            screen is TabScreen -> {
                                // 移除栈顶的页面，直到栈顶的页面是startScreen
                                navigator.popUntil { it == startScreen }

                                // 如果栈顶的页面与目标页面不同则替换
                                if (navigator.lastItemOrNull != screen) {
                                    navigator.push(screen)
                                }
                            }

                            // 不同类型的页面，添加至导航栈
                            navigator.lastItemOrNull
                                ?.let { !it::class.isInstance(screen) }
                                ?: true -> navigator.push(screen)

                            // 同类型页面，替换
                            else -> navigator.replace(screen)
                        }

                        // 尝试跳转的时候若底部sheet不可见，则显示
                        if (enhanceSheetState is EnhanceModalSheetState && !enhanceSheetState.isVisible) {
                            enhanceSheetState.show()
                        }
                    }
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
            enhanceSheetState.apply {
                when (this) {
                    is EnhanceBottomSheetState -> navigator.pop()
                    is EnhanceModalSheetState -> if (!navigator.pop()) hide()
                    else -> {}
                }
            }
        }

        content(navigator)
    }
}