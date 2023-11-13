package com.lalilu.lmusic.compose.component.navigate

import android.view.MotionEvent
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.lmusic.utils.extension.measureHeight

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NavigationSmartBar(
    modifier: Modifier = Modifier,
    measureHeightState: MutableState<PaddingValues>,
    navigator: BottomSheetNavigator,
    content: @Composable (Modifier) -> Unit
) {
    val density = LocalDensity.current
    val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current

    val currentScreen by remember { derivedStateOf { navigator.lastItemOrNull as? DynamicScreen } }
    val mainContent by remember { derivedStateOf { currentScreen?.mainContentStack?.lastOrNull() } }
    val extraContent by remember { derivedStateOf { currentScreen?.extraContentStack?.lastOrNull() } }

    val isShowMask by remember { derivedStateOf { mainContent?.showMask ?: false } }
    val isShowBackground by remember { derivedStateOf { mainContent?.showBackground ?: true } }

    val maskColorUp = animateColorAsState(
        targetValue = if (isShowMask) Color.Black.copy(alpha = 0.4f) else Color.Transparent,
        label = ""
    )
    val maskColorBottom = animateColorAsState(
        targetValue = if (isShowMask) Color.Black.copy(alpha = 0.7f) else Color.Transparent,
        label = ""
    )
    val backgroundColor = animateColorAsState(
        targetValue = if (isShowBackground) MaterialTheme.colors.background.copy(alpha = 0.95f) else Color.Transparent,
        label = ""
    )

    // Mask遮罩层，点击后即消失
    Spacer(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        maskColorUp.value,
                        maskColorBottom.value
                    )
                )
            )
            .fillMaxSize()
            .pointerInteropFilter {  // 监听触摸时，若为ACTION_UP或ACTION_CANCEL则触发返回事件
                if (isShowMask && (it.action == MotionEvent.ACTION_UP || it.action == MotionEvent.ACTION_CANCEL)) {
                    backPressDispatcher?.onBackPressedDispatcher?.onBackPressed()
                }
                isShowMask
            }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = backgroundColor.value)
            .measureHeight { _, height ->
                measureHeightState.value = PaddingValues(bottom = density.run { height.toDp() })
            },
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedContent(
            targetState = extraContent,
            label = "",
            content = { it?.content?.invoke() }
        )

        AnimatedContent(
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) togetherWith slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
            },
//            contentAlignment = Alignment.BottomCenter,
            targetState = WindowInsets.isImeVisible to mainContent,
            label = ""
        ) { pair ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                when {
                    pair.first -> Unit
                    pair.second != null -> pair.second!!.content.invoke()
                    else -> content(Modifier.fillMaxWidth())
                }
            }
        }
    }
}