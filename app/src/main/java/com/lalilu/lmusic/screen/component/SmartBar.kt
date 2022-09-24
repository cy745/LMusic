package com.lalilu.lmusic.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
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

    private val statusBarHeightDp = MutableStateFlow(0.dp)
    val smartBarHeightDpState = mutableStateOf(0.dp)
    val statusBarHeightDpLiveData = statusBarHeightDp
        .debounce(50L)
        .asLiveData()

    @Composable
    @OptIn(ExperimentalAnimationApi::class)
    fun BoxScope.SmartBarContent(translationY: Float, alpha: Float) {
        val density = LocalDensity.current
        val windowSize = LocalWindowSize.current
        val hasContent = mainBar.value != null || extraBar.value != null
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val isPad = windowSize.isPad()

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
                        smartBarHeightDpState.value = density.run { height.toDp() + 20.dp }
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