package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.component.navigation.EnhanceNavigator
import com.lalilu.component.navigation.LocalSheetController
import com.lalilu.component.navigation.SheetController
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.new_screen.SearchScreen
import com.lalilu.lplaylist.screen.PlaylistScreen

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
    navigator: EnhanceNavigator,
    sheetController: SheetController? = LocalSheetController.current,
    getScreenFrom: (Navigator) -> Screen = { it.lastItem },
) {
    ImmerseStatusBar(
        isExpended = { sheetController?.isVisible ?: false }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val currentScreen = remember { mutableStateOf<Screen?>(null) }
        val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }

        CustomTransition(
            modifier = Modifier.fillMaxSize(),
            keyPrefix = transitionKeyPrefix,
            navigator = navigator.getNavigator(),
            getScreenFrom = getScreenFrom,
        ) {
            if (!currentComposer.skipping) {
                currentScreen.value = it
            }

            CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
                it.Content()
            }
        }
        NavigationSmartBar(
            modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            measureHeightState = currentPaddingValue,
            currentScreen = { currentScreen.value }
        ) { modifier ->
            NavigationBar(
                modifier = modifier.align(Alignment.BottomCenter),
                tabScreens = {
                    listOf(
                        HomeScreen,
                        PlaylistScreen,
                        SearchScreen
                    )
                },
                currentScreen = { currentScreen.value },
                navigator = navigator,
                sheetController = sheetController
            )
        }
    }
}