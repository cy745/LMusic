package com.lalilu.component.base

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.navigation.EnhanceNavigator
import com.lalilu.component.override.ModalBottomSheetDefaults
import com.lalilu.component.override.ModalBottomSheetLayout
import com.lalilu.component.override.ModalBottomSheetState
import com.lalilu.component.override.ModalBottomSheetValue
import com.lalilu.component.override.rememberModalBottomSheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias BottomSheetNavigatorContent = @Composable (bottomSheetNavigator: BottomSheetNavigator) -> Unit

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
    sheetContent: BottomSheetNavigatorContent = { CurrentScreen() },
    content: BottomSheetNavigatorContent
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = skipHalfExpanded,
        enableBottomSheetMode = enableBottomSheetMode,
        animationSpec = animationSpec
    )

    Navigator(screen = defaultScreen, onBackPressed = null) { navigator ->
        val bottomSheetNavigator = remember {
            BottomSheetNavigator(
                navigator = navigator,
                sheetState = sheetState,
                coroutineScope = coroutineScope
            )
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
                        bottomSheetNavigator.back()
                    }
                    sheetContent(bottomSheetNavigator)
                },
                content = { content(bottomSheetNavigator) }
            )
        }
    }
}

class BottomSheetNavigator internal constructor(
    private val navigator: Navigator,
    private val sheetState: ModalBottomSheetState,
    private val coroutineScope: CoroutineScope
) : Stack<Screen> by navigator, EnhanceNavigator {

    val isVisible: Boolean by derivedStateOf {
        if (!sheetState.enabled) {
            return@derivedStateOf items.size > 1
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