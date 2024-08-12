package com.lalilu.lmusic.compose

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.base.BottomSheetLayout
import com.lalilu.component.base.BottomSheetLayout2
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.DynamicTipsHost
import com.lalilu.component.navigation.HostNavigator
import com.lalilu.component.navigation.NavigationSmartBar
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.screen.playing.PlayingLayout
import com.lalilu.lmusic.compose.screen.playing.PlayingLayoutExpended
import com.lalilu.lmusic.compose.screen.playing.PlayingSmartCard

object LayoutWrapper {

    @Composable
    fun BoxScope.Content() {
        val windowClass = LocalWindowSize.current

        val navHostContent = remember {
            movableContentOf<(Navigator) -> Unit> { content ->
                HostNavigator(HomeScreen) { navigator ->
                    content(navigator)

                    CustomTransition(
                        modifier = Modifier.fillMaxSize(),
                        navigator = navigator,
                    )
                }
            }
        }

        val navigationSmartBar = remember {
            movableContentOf<Modifier> { modifier ->
                NavigationSmartBar(modifier = modifier)
            }
        }

        if (windowClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            LayoutForPad(
                navHostContent = navHostContent,
                navigationSmartBar = navigationSmartBar
            )
        } else {
            LayoutForMobile(
                navHostContent = navHostContent,
                navigationSmartBar = navigationSmartBar
            )
        }

        DialogWrapper.Content()

        with(DynamicTipsHost) { Content() }
    }
}

@Composable
fun LayoutForPad(
    modifier: Modifier = Modifier,
    navigatorBarHeight: Dp = 56.dp,
    navHostContent: @Composable ((Navigator) -> Unit) -> Unit,
    navigationSmartBar: @Composable (Modifier) -> Unit
) {
    val navigator = remember { mutableStateOf<Navigator?>(null) }

    BottomSheetLayout2(
        modifier = modifier,
        sheetPeekHeight = navigatorBarHeight,
        sheetContent = { enhanceSheetState ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val progress = enhanceSheetState.progress(
                                BottomSheetValue.Collapsed,
                                BottomSheetValue.Expanded
                            )

                            alpha = (progress * 4f).coerceIn(0f, 1f)
                        }
                ) {
                    PlayingLayoutExpended()
                }

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(navigatorBarHeight)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            val progress = enhanceSheetState.progress(
                                BottomSheetValue.Collapsed,
                                BottomSheetValue.Expanded
                            )

                            translationY = constraints.maxHeight * progress
                            alpha = (1f - progress)
                        }
                ) {
                    PlayingSmartCard(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(360.dp)
                    )

                    CompositionLocalProvider(LocalNavigator provides navigator.value) {
                        navigationSmartBar(
                            Modifier
                                // 拦截滑动事件
                                .pointerInput(Unit) { detectDragGestures { _, _ -> } }
                                .fillMaxHeight()
                                .weight(1f)
                        )
                    }
                }
            }
        },
        content = {
            navHostContent { navigator.value = it }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LayoutForMobile(
    modifier: Modifier = Modifier,
    navHostContent: @Composable ((Navigator) -> Unit) -> Unit,
    navigationSmartBar: @Composable (Modifier) -> Unit
) {
    BottomSheetLayout(
        modifier = modifier.fillMaxSize(),
        scrimColor = Color.Black.copy(alpha = 0.5f),
        skipHalfExpanded = false,
        sheetBackgroundColor = MaterialTheme.colors.background,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.1f, 0.16f, 0f, 1f)
        ),
        sheetContent = {
            val navigator = remember { mutableStateOf<Navigator?>(null) }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                navHostContent { navigator.value = it }

                CompositionLocalProvider(value = LocalNavigator provides navigator.value) {
                    navigationSmartBar(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        },
        content = { PlayingLayout() }
    )
}