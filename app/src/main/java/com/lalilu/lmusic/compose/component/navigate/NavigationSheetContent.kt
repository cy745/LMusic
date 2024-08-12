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
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.component.base.TabScreen
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmusic.compose.component.CustomTransition


@Composable
fun NavigationSheetContent(
    modifier: Modifier,
    navigator: Navigator,
) {
    val tabScreenRoutes = remember {
        listOf("/pages/home", "/pages/playlist", "/pages/search")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }
        val tabScreens = remember(tabScreenRoutes) {
            tabScreenRoutes.mapNotNull { AppRouter.route(it).get() as? TabScreen }
        }

        CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
            CustomTransition(
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
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
                tabScreens = { tabScreens },
                currentScreen = { navigator.lastItemOrNull }
            )
        }
    }
}