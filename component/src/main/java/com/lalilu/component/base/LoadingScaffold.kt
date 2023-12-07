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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Composable
inline fun <reified T> LoadingScaffold(
    modifier: Modifier = Modifier,
    targetState: State<LoadingState<T>>,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "Loading PlaceHolder AnimatedContent",
    noinline contentKey: (LoadingState<T>) -> Any = { it },
    noinline transitionSpec: AnimatedContentTransitionScope<LoadingState<T>>.() -> ContentTransform = {
        fadeIn(animationSpec = tween(220)) togetherWith
                ExitTransition.None
    },
    crossinline onLoadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    crossinline onLoadErrorContent: @Composable () -> Unit = { DefaultErrorContent() },
    crossinline onLoadedContent: @Composable (T) -> Unit = {}
) {
    AnimatedContent(
        modifier = modifier,
        targetState = targetState.value,
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        contentKey = contentKey,
        label = label
    ) {
        when {
            it.isLoading -> onLoadingContent()
            it.isError || it.data == null -> onLoadErrorContent()
            else -> onLoadedContent(it.data as T)
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

@Suppress("UNCHECKED_CAST")
data class LoadingState<T>(
    val data: T? = null,
    val isError: Boolean = false,
    val isLoading: Boolean = false
) {
    companion object {
        val loadingInstanceMap = HashMap<Class<*>, LoadingState<*>>()
        val errorInstanceMap = HashMap<Class<*>, LoadingState<*>>()

        inline fun <reified T> getLoadingInstance(): LoadingState<T> {
            return loadingInstanceMap
                .getOrPut(T::class.java) { LoadingState<T>(isLoading = true) }
                    as LoadingState<T>
        }

        inline fun <reified T> getErrorInstance(): LoadingState<T> {
            return errorInstanceMap
                .getOrPut(T::class.java) { LoadingState<T>(isError = true) }
                    as LoadingState<T>
        }
    }
}

@Composable
inline fun <reified T> Flow<T?>.collectAsLoadingState(
    context: CoroutineContext = EmptyCoroutineContext
): State<LoadingState<T>> = produceState(LoadingState.getLoadingInstance(), this, context) {
    if (context == EmptyCoroutineContext) {
        collect {
            value = if (it == null) LoadingState.getErrorInstance() else LoadingState(data = it)
        }
    } else withContext(context) {
        collect {
            value = if (it == null) LoadingState.getErrorInstance() else LoadingState(data = it)
        }
    }
}