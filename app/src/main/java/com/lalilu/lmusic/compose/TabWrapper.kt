package com.lalilu.lmusic.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.component.base.TabScreen
import com.lalilu.lmusic.compose.component.navigate.NavigateTabBar
import com.lalilu.lmusic.compose.component.navigate.NavigationSmartBar
import com.lalilu.lmusic.compose.new_screen.HomeScreen
import com.lalilu.lmusic.compose.new_screen.SearchScreen
import com.lalilu.lplaylist.screen.PlaylistScreen

object TabWrapper {

    var navigator: TabNavigator? = null
        private set

    val tabScreen: List<TabScreen> = listOf(
        HomeScreen,
        PlaylistScreen,
        SearchScreen
    )

    @Composable
    fun Content() {
        val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }

        TabNavigator(tab = HomeScreen) { tabNavigator ->
            navigator = tabNavigator

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    targetState = tabNavigator.current,
                    transitionSpec = {
                        fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + slideInVertically { 100 } togetherWith
                                fadeOut(tween(0))
                    },
                    label = ""
                ) { screen ->
                    tabNavigator.saveableState("transition", screen) {
                        CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
                            screen.Content()
                        }
                    }
                }

                NavigationSmartBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    measureHeightState = currentPaddingValue,
                    screen = { tabNavigator.current }
                ) { modifier ->
                    NavigateTabBar(
                        modifier = modifier
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colors.background.copy(alpha = 0.95f)),
                        tabScreens = { tabScreen },
                        currentScreen = { tabNavigator.current },
                        onSelectTab = { tabNavigator.current = it }
                    )
                }
            }
        }
    }
}