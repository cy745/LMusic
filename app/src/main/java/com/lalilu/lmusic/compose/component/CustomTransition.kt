package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun CustomTransition(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    keyPrefix: String = "",
    getScreenFrom: (Navigator) -> Screen = { navigator.lastItem },
    content: @Composable (AnimatedVisibilityScope.(Screen) -> Unit) = { it.Content() }
) {
    AnimatedContent(
        modifier = modifier,
        contentKey = { it.key },
        targetState = getScreenFrom(navigator),
        transitionSpec = {
            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + slideInVertically { 100 } togetherWith
                    fadeOut(tween(0))
        },
        label = "CustomAnimateTransition"
    ) { screen ->
        navigator.saveableState("${keyPrefix}_transition", screen) {
            content(screen)
        }
    }
}