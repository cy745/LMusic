package com.lalilu.lmusic.compose

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lalilu.component.base.BottomSheetNavigatorLayout
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lmusic.compose.component.navigate.NavigationSheetContent
import com.lalilu.lmusic.compose.new_screen.HomeScreen


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val windowSizeClass = LocalWindowSize.current

    BottomSheetNavigatorLayout(
        modifier = modifier.fillMaxSize(),
        defaultScreen = HomeScreen,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        skipHalfExpanded = false,
        sheetBackgroundColor = MaterialTheme.colors.background,
        enableBottomSheetMode = { windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded },
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.1f, 0.16f, 0f, 1f)
        ),
        sheetContent = { sheetNavigator ->
            LaunchedEffect(sheetNavigator) {
                AppRouter.intentFlow.collect { intent ->
                    when (intent) {
                        NavIntent.Pop -> sheetNavigator.back()
                        is NavIntent.Push -> sheetNavigator.jump(intent.screen)
                        is NavIntent.Replace -> sheetNavigator.replace(intent.screen)
                    }
                }
            }

            NavigationSheetContent(
                modifier = modifier,
                navigator = sheetNavigator
            )
        },
        content = { content() }
    )
}