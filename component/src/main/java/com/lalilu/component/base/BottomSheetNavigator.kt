package com.lalilu.component.base

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.lalilu.component.navigation.EnhanceNavigator
import com.lalilu.component.override.ModalBottomSheetDefaults
import com.lalilu.component.override.ModalBottomSheetLayout
import com.lalilu.component.override.ModalBottomSheetState
import com.lalilu.component.override.ModalBottomSheetValue
import com.lalilu.component.override.rememberModalBottomSheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val LocalBottomSheetNavigator: ProvidableCompositionLocal<BottomSheetNavigator?> =
    staticCompositionLocalOf { null }

@ExperimentalMaterialApi
@Composable
fun BottomSheetNavigatorLayout(
    modifier: Modifier = Modifier,
    defaultScreen: Screen,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = 0.dp,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    sheetGesturesEnabled: Boolean = true,
    skipHalfExpanded: Boolean = true,
    enableBottomSheetMode: () -> Boolean = { true },
    animationSpec: AnimationSpec<Float> = ModalBottomSheetDefaults.AnimationSpec,
    sheetContent: @Composable (bottomSheetNavigator: BottomSheetNavigator) -> Unit = { CurrentScreen() },
    content: @Composable (sheetState: ModalBottomSheetState) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = skipHalfExpanded,
        enableBottomSheetMode = enableBottomSheetMode,
        animationSpec = animationSpec
    )

    Navigator(
        screen = defaultScreen,
        onBackPressed = null,
        disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false)
    ) { navigator ->
        val bottomSheetNavigator = remember {
            BottomSheetNavigator(
                navigator = navigator,
                sheetState = sheetState,
                coroutineScope = coroutineScope
            )
        }

        val scaleValue = remember(sheetState) {
            derivedStateOf {
                val state = sheetState.anchoredDraggableState
                val min = state.anchors.minAnchor()
                val max = state.anchors.maxAnchor()
                val offset = state.offset

                val fraction = offset.normalize(min, max)
                val scale = 0.8f + 0.2f * fraction
                scale.takeIf { !it.isNaN() } ?: 1f
            }
        }

        CompositionLocalProvider(LocalBottomSheetNavigator provides bottomSheetNavigator) {
            ModalBottomSheetLayout(
                modifier = modifier,
                scrimColor = scrimColor,
                sheetState = sheetState,
                sheetShape = sheetShape,
                sheetElevation = sheetElevation,
                sheetBackgroundColor = sheetBackgroundColor,
                sheetContentColor = sheetContentColor,
                sheetGesturesEnabled = sheetGesturesEnabled,
                sheetContent = {
                    BackHandler(enabled = bottomSheetNavigator.isVisible) {
                        if (sheetState.currentValue == ModalBottomSheetValue.Expanded) {
                            bottomSheetNavigator.back()
                        } else {
                            coroutineScope.launch { sheetState.hide() }
                        }
                    }
                    key("SheetContent") {
                        sheetContent(bottomSheetNavigator)
                    }
                },
                content = {
                    Surface(color = Color.Black) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scaleValue.value
                                scaleY = scaleX
                            }
                            .clip(RoundedCornerShape(32.dp)),
                            content = { content(sheetState) }
                        )
                    }
                }
            )
        }
    }
}

class BottomSheetNavigator internal constructor(
    private val navigator: Navigator,
    private val coroutineScope: CoroutineScope,
    val sheetState: ModalBottomSheetState,
) : Stack<Screen> by navigator, EnhanceNavigator {

    val isVisible: Boolean by derivedStateOf {
        if (!sheetState.enabled) {
            return@derivedStateOf items.size > 1
        }

        if (!sheetState.isSkipHalfExpanded) {
            return@derivedStateOf sheetState.progress(
                from = ModalBottomSheetValue.Hidden,
                to = ModalBottomSheetValue.HalfExpanded
            ) >= 0.95
        }

        sheetState.progress(
            from = ModalBottomSheetValue.Hidden,
            to = ModalBottomSheetValue.Expanded
        ) >= 0.95
    }

    fun hide() {
        if (isVisible) {
            coroutineScope.launch { sheetState.hide() }
        }
    }

    fun show() {
        if (!isVisible) {
            coroutineScope.launch { sheetState.show() }
        }
    }

    override fun preBack(currentScreen: Screen?): Boolean {
        // 若当前只剩一个页面，则不清空元素了
        if (items.size <= 1) {
            hide()
            return false
        }
        return true
    }

    override fun postBack(fromScreen: Screen?) {
        hide()
    }

    override fun postJump(fromScreen: Screen?, toScreen: Screen): Boolean {
        show()
        return true
    }

    override fun getNavigator(): Navigator = navigator
}

private fun Float.normalize(minValue: Float, maxValue: Float): Float {
    val min = minOf(minValue, maxValue)
    val max = maxOf(minValue, maxValue)

    if (min == max) return 0f
    if (this <= min) return 0f
    if (this >= max) return 1f

    return ((this - min) / (max - min))
        .coerceIn(0f, 1f)
}