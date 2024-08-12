package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.navigation.CustomScreenTransition

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun CustomTransition(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    content: @Composable (AnimatedVisibilityScope.(Screen) -> Unit) = {
        navigator.saveableState("transition", it) { it.Content() }
    }
) {
    CustomScreenTransition(
        navigator = navigator,
        modifier = modifier,
        disposeScreenAfterTransitionEnd = true,
        content = content,
        transition = {
            val (initialOffset, targetOffset) = when (navigator.lastEvent) {
                StackEvent.Pop -> ({ size: Int -> -100 }) to ({ size: Int -> 100 })
                else -> ({ size: Int -> 100 }) to ({ size: Int -> -100 })
            }

            slideInVertically(animationSpec, initialOffset) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) togetherWith
                    slideOutVertically(animationSpec, targetOffset) + fadeOut(tween(50))
        }
    )
}