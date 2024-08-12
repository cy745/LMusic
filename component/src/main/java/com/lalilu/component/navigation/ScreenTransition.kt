package com.lalilu.component.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent

@ExperimentalVoyagerApi
@OptIn(InternalVoyagerApi::class)
@Composable
fun CustomScreenTransition(
    navigator: Navigator,
    transition: AnimatedContentTransitionScope<Screen>.() -> ContentTransform,
    modifier: Modifier = Modifier,
    disposeScreenAfterTransitionEnd: Boolean = false,
    content: ScreenTransitionContent = {
        navigator.saveableState("transition", it) { it.Content() }
    }
) {
    val screenCandidatesToDispose = rememberSaveable(saver = screenCandidatesToDisposeSaver()) {
        mutableStateOf(emptySet())
    }

    val currentScreens = navigator.items

    if (disposeScreenAfterTransitionEnd) {
        DisposableEffect(currentScreens) {
            onDispose {
                val newScreenKeys = navigator.items.map { it.key }
                screenCandidatesToDispose.value += currentScreens.filter { it.key !in newScreenKeys }
            }
        }
    }

    AnimatedContent(
        targetState = navigator.lastItem,
        transitionSpec = {
            val contentTransform = transition()

            val sourceScreenTransition = when (navigator.lastEvent) {
                StackEvent.Pop, StackEvent.Replace -> initialState
                else -> targetState
            } as? ScreenTransition

            val screenEnterTransition = sourceScreenTransition?.enter(navigator.lastEvent)
                ?: contentTransform.targetContentEnter

            val screenExitTransition = sourceScreenTransition?.exit(navigator.lastEvent)
                ?: contentTransform.initialContentExit

            screenEnterTransition togetherWith screenExitTransition
        },
        modifier = modifier
    ) { screen ->
        if (this.transition.targetState == this.transition.currentState && disposeScreenAfterTransitionEnd) {
            LaunchedEffect(Unit) {
                val newScreens = navigator.items.map { it.key }
                val screensToDispose =
                    screenCandidatesToDispose.value.filterNot { it.key in newScreens }
                if (screensToDispose.isNotEmpty()) {
                    screensToDispose.forEach { navigator.dispose(it) }
                    navigator.clearEvent()
                }
                screenCandidatesToDispose.value = emptySet()
            }
        }

        content(screen)
    }
}

private fun screenCandidatesToDisposeSaver(): Saver<MutableState<Set<Screen>>, List<Screen>> {
    return Saver(
        save = { it.value.toList() },
        restore = { mutableStateOf(it.toSet()) }
    )
}
