package com.lalilu.lmusic.compose

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.component.base.BottomSheetNavigatorLayout
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.lmusic.compose.component.navigate.NavigationSheetContent
import com.lalilu.lmusic.compose.new_screen.HomeScreen


@OptIn(ExperimentalMaterialApi::class)
object NavigationWrapper {
    var navigator: BottomSheetNavigator? by mutableStateOf(null)
        private set

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val windowSizeClass = LocalWindowSize.current
        // 共用Navigator避免切换时导致导航栈丢失
        val animateSpec = remember {
            tween<Float>(
                durationMillis = 150,
                easing = CubicBezierEasing(0.1f, 0.16f, 0f, 1f)
            )
        }
        BottomSheetNavigatorLayout(
            modifier = modifier.fillMaxSize(),
            defaultScreen = HomeScreen,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetBackgroundColor = MaterialTheme.colors.background,
            enableBottomSheetMode = { windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded },
            animationSpec = animateSpec,
            sheetContent = { sheetNavigator ->
                this@NavigationWrapper.navigator = sheetNavigator

                NavigationSheetContent(
                    modifier = modifier,
                    transitionKeyPrefix = "bottomSheet",
                    navigator = sheetNavigator
                )
            }
        ) { }
    }
}