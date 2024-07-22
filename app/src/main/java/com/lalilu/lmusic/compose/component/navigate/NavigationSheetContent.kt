package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.new_screen.SearchScreen
import com.lalilu.lplaylist.screen.PlaylistScreen


@Composable
fun NavigationSheetContent(
    modifier: Modifier,
    navigator: BottomSheetNavigator,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }

        CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
            CustomTransition(
                modifier = Modifier.fillMaxSize(),
                navigator = navigator.getNavigator(),
            )
        }

        NavigationSmartBar(
            modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            measureHeightState = currentPaddingValue,
            currentScreen = { navigator.lastItemOrNull }
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
                currentScreen = { navigator.lastItemOrNull }
            )
        }
    }
}