package com.lalilu.lmusic.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object SmartBar {
    private var maxSize = 2
    private val items = mutableStateListOf<@Composable () -> Unit>()
    val contentPaddingForSmartBar = mutableStateOf(0.dp)

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun BoxScope.SmartBarContent() {
        Column(
            modifier = Modifier
                .clickable(enabled = false) { }
                .align(Alignment.BottomCenter)
                .background(color = MaterialTheme.colors.background.copy(alpha = 0.95f))
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .animateContentSize()
        ) {
            AnimatedContent(targetState = items) {
                if (it.size > 0) {
                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(Color.LightGray)
                    )
                }
            }
            items.forEach { item ->
                AnimatedContent(targetState = item) { it.invoke() }
            }
            AnimatedContent(targetState = items) {
                if (it.size > 0) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    fun setBarItem(toggle: Boolean = false, item: @Composable () -> Unit) {
        if (toggle && items.size == 1 && items.contains(item)) {
            items.remove(item)
            return
        }
        items.clear()
        items.add(item)
    }

    fun addBarItem(toggle: Boolean = false, item: @Composable () -> Unit) {
        if (toggle && items.contains(item)) {
            items.remove(item)
            return
        }
        if (items.size == maxSize) {
            items.removeLast()
        }
        items.add(item)
    }

}