package com.lalilu.lmusic.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.utils.extension.measure

object SmartBar {
    private val mainBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val extraBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    val contentPaddingForSmartBar = mutableStateOf(0)
    val contentPaddingForSmartBarDp = mutableStateOf(0.dp)

    @Composable
    @OptIn(ExperimentalAnimationApi::class)
    fun BoxScope.SmartBarContent() {
        val density = LocalDensity.current
        Column(
            modifier = Modifier
                .clickable(enabled = false) { }
                .align(Alignment.BottomCenter)
                .background(color = MaterialTheme.colors.background.copy(alpha = 0.95f))
                .fillMaxWidth()
                .measure { _, height ->
                    contentPaddingForSmartBar.value = height
                    density.run { contentPaddingForSmartBarDp.value = height.toDp() + 20.dp }
                }
        ) {
            AnimatedVisibility(visible = mainBar.value != null) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = "${contentPaddingForSmartBar.value}")
            }
            AnimatedContent(targetState = extraBar.value) { it?.invoke() }
            AnimatedContent(targetState = mainBar.value) { it?.invoke() }
            AnimatedVisibility(visible = mainBar.value != null) {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }

    fun setBarItem(toggle: Boolean = false, item: @Composable () -> Unit) {
        if (toggle && mainBar.value === item) {
            mainBar.value = null
            return
        }
        extraBar.value = null
        mainBar.value = item
    }

    fun addBarItem(toggle: Boolean = false, item: @Composable () -> Unit) {
        if (toggle && extraBar.value === item) {
            extraBar.value = null
            return
        }
        extraBar.value = item
    }

}