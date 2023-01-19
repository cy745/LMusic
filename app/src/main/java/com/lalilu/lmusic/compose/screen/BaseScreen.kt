package com.lalilu.lmusic.compose.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost

abstract class BaseScreen {
    abstract fun register(builder: NavGraphBuilder)

    abstract fun getNavToRoute(): String
    open fun getNavToByArgvRoute(argv: String): String = getNavToRoute()

    @Composable
    fun navTo(
        hapticType: HapticFeedbackType? = null,
        builder: NavOptionsBuilder.() -> Unit = {}
    ): () -> Unit {
        val navigator = LocalNavigatorHost.current
        val haptic = LocalHapticFeedback.current
        return remember {
            {
                hapticType?.let(haptic::performHapticFeedback)
                navigator.navigate(getNavToRoute()) {
                    launchSingleTop = true
                    builder()
                }
            }
        }
    }

    @Composable
    fun navToByArgv(
        hapticType: HapticFeedbackType? = null,
        builder: NavOptionsBuilder.() -> Unit = {}
    ): (String) -> Unit {
        val navigator = LocalNavigatorHost.current
        val haptic = LocalHapticFeedback.current
        return remember {
            {
                hapticType?.let(haptic::performHapticFeedback)
                navigator.navigate(
                    route = getNavToByArgvRoute(it)
                ) {
                    launchSingleTop = true
                    builder()
                }
            }
        }
    }
}