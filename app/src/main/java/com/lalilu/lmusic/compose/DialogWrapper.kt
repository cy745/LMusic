package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.compositionUniqueId
import com.lalilu.component.base.BottomSheetNavigatorLayout
import com.lalilu.component.base.BottomSheetNavigatorBackHandler
import com.lalilu.component.base.HiddenBottomSheetScreen
import com.lalilu.component.navigation.SheetNavigator
import com.lalilu.lmusic.compose.component.CustomTransition

@OptIn(ExperimentalMaterialApi::class)
object DialogWrapper {
    var navigator: SheetNavigator? = null
        private set

    @OptIn(InternalVoyagerApi::class)
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        defaultScreen: Screen = HiddenBottomSheetScreen,
        key: String = compositionUniqueId(),
        content: @Composable () -> Unit
    ) {
        Navigator(defaultScreen, onBackPressed = null, key = key) { navigator ->
            BottomSheetNavigatorLayout(
                navigator = navigator,
                modifier = modifier.fillMaxSize(),
                resetOnHide = true,
                visibleWhenShow = true,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                sheetBackgroundColor = MaterialTheme.colors.background,
                animationSpec = SpringSpec(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 1000f
                ),
                sheetContent = { bottomSheetNavigator ->
                    this.navigator = bottomSheetNavigator

                    CustomTransition(
                        modifier = Modifier.wrapContentHeight(),
                        navigator = bottomSheetNavigator.getNavigator()
                    ) { screen ->
                        BottomSheetNavigatorBackHandler(bottomSheetNavigator, true)
                        screen.Content()
                    }
                },
                content = { content() }
            )
        }
    }
}