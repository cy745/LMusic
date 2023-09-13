package com.lalilu.lmusic.utils.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

fun <T> AnimatedContentTransitionScope<T>.slideTransition(
    duration: Int = 500,
    movement: Int = 200,
    transitionPercent: Float = 0.6f,
    enterEasing: Easing = EaseOut,
    exitEasing: Easing = EaseIn,
    direction: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Left,
): ContentTransform {
    return fadeIn(
        animationSpec = TweenSpec(
            durationMillis = (duration * (1f - transitionPercent)).toInt(),
            delay = (duration * transitionPercent).toInt(),
            easing = enterEasing
        ),
    ) + slideIntoContainer(
        towards = direction,
        animationSpec = TweenSpec(durationMillis = duration, easing = enterEasing),
        initialOffset = { movement }
    ) togetherWith fadeOut(
        animationSpec = TweenSpec(
            durationMillis = (duration * transitionPercent).toInt(),
            easing = exitEasing
        ),
    ) + slideOutOfContainer(
        towards = direction,
        animationSpec = TweenSpec(durationMillis = duration, easing = exitEasing),
        targetOffset = { -movement }
    )
}