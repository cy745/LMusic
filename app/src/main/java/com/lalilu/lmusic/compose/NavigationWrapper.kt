package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.component.base.BottomSheetNavigatorLayout
import com.lalilu.component.base.CustomScreen
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.component.navigation.SheetNavigator
import com.lalilu.component.base.SideSheetNavigatorLayout
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.component.navigate.NavigationBar
import com.lalilu.lmusic.compose.component.navigate.NavigationSmartBar
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.new_screen.SearchScreen
import com.lalilu.lplaylist.screen.PlaylistScreen


@OptIn(ExperimentalMaterialApi::class)
object NavigationWrapper {
    var navigator: SheetNavigator? by mutableStateOf(null)
        private set

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        forPad: Boolean = false,
        baseContent: @Composable () -> Unit
    ) {
        if (forPad) {
            SideSheetNavigatorLayout(
                modifier = modifier.fillMaxSize(),
                scrimColor = Color.Black.copy(alpha = 0.5f),
                sheetBackgroundColor = MaterialTheme.colors.background,
                animationSpec = SpringSpec(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 1000f
                ),
                sheetContent = { sheetNavigator ->
                    navigator = sheetNavigator
                    SheetContent(modifier, sheetNavigator)
                }
            ) {
                baseContent()
            }
        } else {
            BottomSheetNavigatorLayout(
                defaultScreen = HomeScreen,
                ignoreFlingNestedScroll = true,
                modifier = modifier.fillMaxSize(),
                scrimColor = Color.Black.copy(alpha = 0.5f),
                sheetBackgroundColor = MaterialTheme.colors.background,
                animationSpec = SpringSpec(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 1000f
                ),
                sheetContent = { sheetNavigator ->
                    navigator = sheetNavigator
                    SheetContent(modifier, sheetNavigator)
                },
                content = { baseContent() }
            )
        }
    }

    @Composable
    private fun SheetContent(
        modifier: Modifier,
        sheetNavigator: SheetNavigator
    ) {
        val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }
        val currentScreen by remember { derivedStateOf { sheetNavigator.lastItemOrNull as? CustomScreen } }
        val customScreenInfo by remember { derivedStateOf { currentScreen?.getScreenInfo() } }

        ImmerseStatusBar(
            enable = { customScreenInfo?.immerseStatusBar == true },
            isExpended = { sheetNavigator.isVisible }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            CustomTransition(
                modifier = Modifier.fillMaxSize(),
                navigator = sheetNavigator.getNavigator()
            ) {
                CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
                    it.Content()
                }
            }
            NavigationSmartBar(
                modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                measureHeightState = currentPaddingValue,
                currentScreen = { currentScreen }
            ) { modifier ->
                NavigationBar(
                    modifier = modifier.align(Alignment.BottomCenter),
                    tabScreens = { TabWrapper.tabScreen },
                    currentScreen = { currentScreen },
                    navigator = sheetNavigator
                )
            }
        }
    }
}

@Composable
fun ImmerseStatusBar(
    enable: () -> Boolean = { true },
    isExpended: () -> Boolean = { false },
) {
    val result by remember { derivedStateOf { isExpended() && enable() } }
    val systemUiController = rememberSystemUiController()
    val isDarkModeNow = isSystemInDarkTheme()

    LaunchedEffect(result, isDarkModeNow) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = result && !isDarkModeNow
        )
    }
}