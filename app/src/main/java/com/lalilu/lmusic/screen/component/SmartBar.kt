package com.lalilu.lmusic.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.isPad
import com.lalilu.lmusic.utils.extension.measure

object SmartBar {
    private val mainBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val extraBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    val contentPaddingForSmartBar = mutableStateOf(0)
    val contentPaddingForSmartBarDp = mutableStateOf(0.dp)

    @Composable
    fun rememberContentPadding(horizontal: Dp = 0.dp): PaddingValues {
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        return remember(horizontal, statusBarHeight, contentPaddingForSmartBarDp.value) {
            PaddingValues(
                top = statusBarHeight,
                bottom = contentPaddingForSmartBarDp.value,
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
                        contentPaddingForSmartBar.value = height
                        density.run { contentPaddingForSmartBarDp.value = height.toDp() + 20.dp }
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