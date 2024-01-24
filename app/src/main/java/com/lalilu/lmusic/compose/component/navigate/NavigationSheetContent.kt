package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.component.base.CustomScreen
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.component.navigation.SheetNavigator
import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.lmusic.compose.TabWrapper
import com.lalilu.lmusic.compose.component.CustomTransition

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

@Composable
fun NavigationSheetContent(
    modifier: Modifier,
    transitionKeyPrefix: String,
    sheetNavigator: SheetNavigator,
    getScreenFrom: (Navigator) -> Screen = { sheetNavigator.getNavigator().lastItem },
) {
    val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    val customScreenInfo by remember { derivedStateOf { (currentScreen as? CustomScreen)?.getScreenInfo() } }

    ImmerseStatusBar(
        enable = { customScreenInfo?.immerseStatusBar != false },
        isExpended = { sheetNavigator.isVisible }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CustomTransition(
            modifier = Modifier.fillMaxSize(),
            keyPrefix = transitionKeyPrefix,
            navigator = sheetNavigator.getNavigator(),
            getScreenFrom = getScreenFrom,
        ) {
            currentScreen = it
            CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
                it.Content()
            }
        }
        NavigationSmartBar(
            modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            measureHeightState = currentPaddingValue,
            currentScreen = { NavigationWrapper.navigator?.lastItemOrNull }
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