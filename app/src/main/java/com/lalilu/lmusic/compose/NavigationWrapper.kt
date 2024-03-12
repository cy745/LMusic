package com.lalilu.lmusic.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.compositionUniqueId
import com.lalilu.component.base.BottomSheetNavigatorLayout
import com.lalilu.component.navigation.EnhanceNavigator
import com.lalilu.component.navigation.SheetController
import com.lalilu.component.navigation.createDefaultEnhanceNavigator
import com.lalilu.lmusic.compose.component.navigate.NavigationSheetContent
import com.lalilu.lmusic.compose.new_screen.HomeScreen


@OptIn(ExperimentalMaterialApi::class)
object NavigationWrapper {
    var navigator: EnhanceNavigator? by mutableStateOf(null)
        private set
    var sheetController: SheetController? by mutableStateOf(null)
        private set

    @OptIn(InternalVoyagerApi::class)
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        forPad: () -> Boolean = { false }
    ) {
        // 共用Navigator避免切换时导致导航栈丢失
        Navigator(
            HomeScreen,
            onBackPressed = null,
            key = compositionUniqueId()
        ) { navigator ->
            if (forPad()) {
                val isVisible by remember(navigator) { derivedStateOf { navigator.items.size > 1 } }
                val emptyNavigator = remember(navigator) {
                    createDefaultEnhanceNavigator(navigator).also {
                        this@NavigationWrapper.sheetController = null
                        this@NavigationWrapper.navigator = it
                    }
                }

                BackHandler(enabled = isVisible) {
                    emptyNavigator.back()
                }

                NavigationSheetContent(
                    modifier = modifier,
                    navigator = emptyNavigator,
                    transitionKeyPrefix = "forPad"
                )
            } else {
                val animateSpec = remember {
                    tween<Float>(
                        durationMillis = 150,
                        easing = CubicBezierEasing(0.1f, 0.16f, 0f, 1f)
                    )
                }
                BottomSheetNavigatorLayout(
                    modifier = modifier.fillMaxSize(),
                    navigator = navigator,
                    defaultIsVisible = false,
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    sheetBackgroundColor = MaterialTheme.colors.background,
                    animationSpec = animateSpec,
                    sheetContent = { sheetNavigator ->
                        this@NavigationWrapper.navigator = sheetNavigator
                        this@NavigationWrapper.sheetController = sheetNavigator
                        NavigationSheetContent(
                            modifier = modifier,
                            transitionKeyPrefix = "bottomSheet",
                            navigator = sheetNavigator,
                            sheetController = sheetNavigator
                        )
                    }
                ) { }
            }
        }
    }
}