package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.lalilu.lmusic.compose.CustomScreen
import com.lalilu.lmusic.compose.component.BottomSheetNavigator
import com.lalilu.lmusic.utils.extension.measureHeight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavigationSmartBar(
    modifier: Modifier = Modifier,
    measureHeightState: MutableState<PaddingValues>,
    navigator: BottomSheetNavigator,
    content: @Composable (Modifier) -> Unit
) {
    val density = LocalDensity.current
    val currentScreen by remember { derivedStateOf { navigator.lastItemOrNull as? CustomScreen } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.background.copy(alpha = 0.95f))
            .measureHeight { _, height ->
                measureHeightState.value = PaddingValues(bottom = density.run { height.toDp() })
            }
    ) {
        AnimatedContent(
            targetState = currentScreen?.getExtraContent(),
            label = ""
        ) {
            it?.invoke()
        }
        AnimatedContent(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding(),
            targetState = WindowInsets.isImeVisible,
            label = ""
        ) { isImeVisible ->
            if (!isImeVisible) {
                content(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        }
    }
}