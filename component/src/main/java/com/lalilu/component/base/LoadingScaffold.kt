package com.lalilu.component.base

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lalilu.component.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Composable
inline fun <reified T> LoadingScaffold(
    modifier: Modifier = Modifier,
    targetState: LoadingState<T>,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "Loading PlaceHolder AnimatedContent",
    noinline contentKey: (LoadingType) -> Any = { it },
    noinline transitionSpec: AnimatedContentTransitionScope<LoadingType>.() -> ContentTransform = {
        fadeIn(animationSpec = tween(220)) togetherWith
                ExitTransition.None
    },
    crossinline onLoadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    crossinline onLoadErrorContent: @Composable () -> Unit = { DefaultErrorContent() },
    crossinline onLoadedContent: @Composable (T) -> Unit = {}
) {
    AnimatedContent(
        modifier = modifier,
        targetState = targetState.loadingType,
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        contentKey = contentKey,
        label = label
    ) {
        when {
            it == LoadingType.Loading -> onLoadingContent()
            it == LoadingType.Error || targetState.data == null -> onLoadErrorContent()
            else -> onLoadedContent(targetState.data as T)
        }
    }
}

@Composable
fun DelayVisibleContent(
    modifier: Modifier = Modifier,
    delayMillis: Long = 200,
    enterTransition: EnterTransition = fadeIn(animationSpec = tween(220)),
    exitTransition: ExitTransition = ExitTransition.None,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis)

        if (isActive) {
            visible.value = true
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible.value,
        enter = enterTransition,
        exit = exitTransition,
        content = content
    )
}

@Composable
fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DelayVisibleContent {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_1701922707980))
//            val properties = rememberLottieDynamicProperties(
//                rememberLottieDynamicProperty(
//                    property = LottieProperty.STROKE_COLOR,
//                    value = dayNightTextColor().toArgb(),
//                    keyPath = arrayOf("**", "Group 1", "Stroke 1")
//                )
//            )

            LottieAnimation(
                composition,
                modifier = Modifier.size(150.dp, 150.dp),
                iterations = LottieConstants.IterateForever,
                clipSpec = LottieClipSpec.Progress(0.5f, 1f),
                speed = 0.5f,
//                dynamicProperties = properties
            )
        }
    }
}

@Composable
fun DefaultErrorContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Error", fontSize = 36.sp)
    }
}

sealed class LoadingType {
    data object Error : LoadingType()
    data object Loading : LoadingType()
    data object Ready : LoadingType()
}

class LoadingState<T> {
    var data: T? by mutableStateOf(null)
    var loadingType: LoadingType by mutableStateOf(LoadingType.Loading)
}

@Composable
inline fun <reified T> Flow<T?>.collectAsLoadingState(
    context: CoroutineContext = EmptyCoroutineContext
): LoadingState<T> {
    val result = remember { LoadingState<T>() }

    LaunchedEffect(Unit) {
        withContext(context) {
            this@collectAsLoadingState.collectLatest {
                result.apply {
                    loadingType = it?.let { LoadingType.Ready } ?: LoadingType.Error
                    data = it
                }
            }
        }
    }

    return result
}