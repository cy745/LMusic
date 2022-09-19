package com.lalilu.lmusic.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.isPad
import com.lalilu.lmusic.utils.extension.measure
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
object SmartBar {
    private val mainBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val extraBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)

    private val smartBarHeightDp = MutableStateFlow(0.dp)
    private val statusBarHeightDp = MutableStateFlow(0.dp)
    private val statusBarHeight = statusBarHeightDp.debounce(50L)

//    private val contentPadding = smartBarHeightDp
//        .combine(statusBarHeightDp) { bottom, top ->
//            PaddingValues(
//                top = top,
//                bottom = if (bottom > 100.dp) bottom else 100.dp
//            )
//        }.debounce(50L)
//        .asLiveData()

    @Composable
    fun rememberContentPadding(horizontal: Dp = 0.dp): PaddingValues {
//        val contentPadding by contentPadding.observeAsState(PaddingValues())
        val statusBarHeight by statusBarHeight.collectAsState(0.dp)

        return remember(statusBarHeight) {
            PaddingValues(
                top = statusBarHeight,
                bottom = 200.dp,
                start = horizontal,
                end = horizontal
            )
        }
    }

    @Composable
    @OptIn(ExperimentalAnimationApi::class)
    fun BoxScope.SmartBarContent(translationY: Float, alpha: Float) {
        val density = LocalDensity.current
        val windowSize = LocalWindowSize.current
        val hasContent = mainBar.value != null || extraBar.value != null
        val isPad = windowSize.isPad()
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        LaunchedEffect(statusBarHeight) {
            statusBarHeightDp.emit(statusBarHeight)
        }

        Surface(
            modifier = Modifier
                .graphicsLayer {
                    this.translationY = if (isPad) 0f else translationY
                    this.alpha = if (isPad) 1f else alpha
                }
                .align(Alignment.BottomCenter)
                .imePadding()
                .fillMaxWidth(),
            color = MaterialTheme.colors.background.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .measure { _, height ->
                        density.run { smartBarHeightDp.value = height.toDp() + 20.dp }
                    }
            ) {
                AnimatedVisibility(visible = hasContent) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                AnimatedContent(targetState = extraBar.value) {
                    it?.invoke()
                }
                AnimatedVisibility(visible = extraBar.value != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                AnimatedContent(targetState = mainBar.value) {
                    it?.invoke()
                }
                AnimatedVisibility(visible = mainBar.value != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                AnimatedVisibility(visible = hasContent) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    fun setMainBar(
        toggle: Boolean = false,
        item: (@Composable () -> Unit)?
    ): SmartBar {
        if (toggle && mainBar.value === item) {
            mainBar.value = null
            return this
        }
        mainBar.value = item
        return this
    }

    fun setExtraBar(
        toggle: Boolean = false,
        item: (@Composable () -> Unit)?
    ): SmartBar {
        if (toggle && extraBar.value === item) {
            extraBar.value = null
            return this
        }
        extraBar.value = item
        return this
    }
}