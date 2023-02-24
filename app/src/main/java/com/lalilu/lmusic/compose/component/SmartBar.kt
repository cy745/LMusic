package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.compose.new_screen.NavBar
import com.lalilu.lmusic.utils.extension.measure
import com.lalilu.lmusic.utils.recomposeHighlighter

object SmartBar {
    private val mainBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val extraBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)

    val smartBarHeightDpState = mutableStateOf(0.dp)

    @Composable
    @OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
    fun BoxScope.SmartBarContent(modifier: Modifier = Modifier) {
        val density = LocalDensity.current
        val hasContent by remember(mainBar, extraBar) {
            derivedStateOf { mainBar.value != null || extraBar.value != null }
        }

        Surface(
            modifier = modifier
                .recomposeHighlighter()
                .align(Alignment.BottomCenter)
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
                    Spacer(modifier = Modifier.height(5.dp))
                }
                AnimatedContent(targetState = extraBar.value) {
                    it?.invoke()
                }
                AnimatedVisibility(visible = extraBar.value != null) {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                AnimatedContent(targetState = WindowInsets.isImeVisible to mainBar.value) { pair ->
                    pair.second.takeIf { !pair.first }?.invoke()
                }
                AnimatedVisibility(visible = mainBar.value != null && !WindowInsets.isImeVisible) {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                AnimatedVisibility(visible = hasContent) {
                    Spacer(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()
                    )
                }
            }
        }
    }

    fun setMainBar(
        content: (@Composable () -> Unit)?
    ): SmartBar {
        mainBar.value = content
        return this
    }

    fun setExtraBar(
        content: (@Composable () -> Unit)?
    ): SmartBar {
        extraBar.value = content
        return this
    }

    @Composable
    fun RegisterExtraBarContent(
        enable: () -> Boolean = { true },
        recoverTo: (@Composable () -> Unit)? = null,
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(enable()) {
            if (enable()) setExtraBar(content) else setExtraBar(recoverTo)
        }
        DisposableEffect(Unit) {
            onDispose {
                setExtraBar(recoverTo)
            }
        }
    }

    @Composable
    fun RegisterMainBarContent(
        enable: () -> Boolean = { true },
        recoverTo: @Composable () -> Unit = NavBar.content,
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(enable()) {
            if (enable()) setMainBar(content) else setMainBar(recoverTo)
        }
        DisposableEffect(Unit) {
            onDispose {
                setMainBar(recoverTo)
            }
        }
    }
}