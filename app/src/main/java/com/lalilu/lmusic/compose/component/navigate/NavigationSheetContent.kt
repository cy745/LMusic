package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.new_screen.SearchScreen
import com.lalilu.lplaylist.screen.PlaylistScreen


@Composable
fun NavigationSheetContent(
    modifier: Modifier,
    transitionKeyPrefix: String,
    navigator: BottomSheetNavigator,
    getScreenFrom: (Navigator) -> Screen = { it.lastItem },
) {
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
                currentScreen = { currentScreen.value }
            )
        }
    }
}