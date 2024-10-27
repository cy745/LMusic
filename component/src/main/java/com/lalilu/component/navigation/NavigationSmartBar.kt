package com.lalilu.component.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lalilu.component.base.ScreenBarComponent
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfoFactory


sealed interface NavigationBarType {
    data object TabBar : NavigationBarType
    data object CommonBar : NavigationBarType
    data class NormalBar(val barComponent: ScreenBarComponent) : NavigationBarType
}

@Composable
fun NavigationSmartBar(
    modifier: Modifier = Modifier,
) {
    val currentScreen = LocalNavigator.current
        ?.lastItemOrNull

    val previousScreen = LocalNavigator.current
        ?.previousScreen()
        ?.value

    val previousTitle = (previousScreen as? ScreenInfoFactory)
        ?.provideScreenInfo()
        ?.title?.invoke()
        ?: "返回"

    val mainContent = (currentScreen as? ScreenBarFactory)?.content()
    val tabScreenRoutes = remember {
        listOf("/pages/home", "/pages/playlist", "/pages/search")
    }

    val tabScreens = remember(tabScreenRoutes) {
        tabScreenRoutes.mapNotNull { AppRouter.route(it).get() as? TabScreen }
    }

    val navigationBar: NavigationBarType = remember(mainContent, currentScreen) {
        when {
            mainContent != null -> NavigationBarType.NormalBar(mainContent)
            currentScreen is TabScreen -> NavigationBarType.TabBar
            else -> NavigationBarType.CommonBar
        }
    }

    AnimatedContent(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) togetherWith slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
        },
        contentAlignment = Alignment.BottomCenter,
        targetState = navigationBar,
        label = ""
    ) { item ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) { detectTapGestures() }
                .background(MaterialTheme.colors.background.copy(0.95f))
                .navigationBarsPadding()
                .height(56.dp)
        ) {
            when (item) {
                is NavigationBarType.NormalBar -> {
                    item.barComponent.content()
                }

                is NavigationBarType.TabBar -> {
                    NavigateTabBar(
                        modifier = Modifier.fillMaxHeight(),
                        currentScreen = { currentScreen },
                        tabScreens = { tabScreens },
                        onSelectTab = { AppRouter.intent(NavIntent.Jump(it)) }
                    )
                }

                is NavigationBarType.CommonBar -> {
                    NavigateCommonBar(
                        modifier = Modifier.fillMaxHeight(),
                        previousTitle = previousTitle,
                        currentScreen = currentScreen
                    )
                }
            }
        }
    }
}
