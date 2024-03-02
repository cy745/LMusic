package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.lalilu.component.base.HiddenBottomSheetScreen
import com.lalilu.component.base.SideSheetNavigatorLayout
import com.lalilu.component.base.TabScreen
import com.lalilu.component.navigation.SheetNavigator
import com.lalilu.lmusic.compose.component.navigate.NavigationSheetContent
import com.lalilu.lmusic.compose.new_screen.HomeScreen


@OptIn(ExperimentalMaterialApi::class)
object NavigationWrapper {
    var navigator: SheetNavigator? by mutableStateOf(null)
        private set

    // 使用remember避免在该变量内部的state引用触发重组,使其转换为普通的变量
    private val isSheetVisible: Boolean
        @Composable
        get() = remember { navigator?.isVisible ?: false }

    @OptIn(InternalVoyagerApi::class)
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        forPad: () -> Boolean = { false }
    ) {
        val emptyScreen = remember {
            HiddenBottomSheetScreen
        }
        val animationSpec = remember {
            SpringSpec<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 1000f
            )
        }

        // 共用Navigator避免切换时导致导航栈丢失
        Navigator(
            HomeScreen,
            onBackPressed = null,
            key = compositionUniqueId()
        ) { navigator ->
            if (forPad()) {
                SideSheetNavigatorLayout(
                    modifier = modifier.fillMaxSize(),
                    navigator = navigator,
                    defaultIsVisible = isSheetVisible && navigator.lastItemOrNull !is TabScreen,
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    sheetBackgroundColor = MaterialTheme.colors.background,
                    animationSpec = animationSpec,
                    sheetContent = { sheetNavigator ->
                        NavigationWrapper.navigator = sheetNavigator
                        NavigationSheetContent(
                            modifier = modifier,
                            transitionKeyPrefix = "SideSheet",
                            sheetNavigator = sheetNavigator,
                            getScreenFrom = {
                                sheetNavigator.lastItemOrNull?.takeIf { it !is TabScreen }
                                    ?: emptyScreen
                            }
                        )
                    },
                    content = { sheetNavigator ->
                        NavigationSheetContent(
                            modifier = modifier,
                            transitionKeyPrefix = "Tab",
                            sheetNavigator = sheetNavigator,
                            getScreenFrom = {
                                sheetNavigator.items.lastOrNull { it is TabScreen }
                                    ?: emptyScreen
                            }
                        )
                    }
                )
            } else {
                BottomSheetNavigatorLayout(
                    modifier = modifier.fillMaxSize(),
                    navigator = navigator,
                    defaultIsVisible = isSheetVisible,
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    sheetBackgroundColor = MaterialTheme.colors.background,
                    animationSpec = animationSpec,
                    sheetContent = { sheetNavigator ->
                        NavigationWrapper.navigator = sheetNavigator
                        NavigationSheetContent(
                            modifier = modifier,
                            transitionKeyPrefix = "bottomSheet",
                            sheetNavigator = sheetNavigator
                        )
                    },
                    content = { }
                )
            }
        }
    }
}