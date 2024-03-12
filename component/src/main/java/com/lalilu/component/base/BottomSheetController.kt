package com.lalilu.component.base

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.navigation.EnhanceNavigator
import com.lalilu.component.navigation.LocalSheetController
import com.lalilu.component.navigation.SheetController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias BottomSheetNavigatorContent = @Composable (bottomSheetNavigator: BottomSheetController) -> Unit

@SuppressLint("UnnecessaryComposedModifier")
@ExperimentalMaterialApi
@Composable
fun BottomSheetNavigatorLayout(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    hideOnBackPress: Boolean = true,
    defaultIsVisible: Boolean = false,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    sheetGesturesEnabled: Boolean = true,
    skipHalfExpanded: Boolean = true,
    animationSpec: AnimationSpec<Float> = ModalBottomSheetDefaults.AnimationSpec,
    sheetContent: BottomSheetNavigatorContent = { CurrentScreen() },
    content: BottomSheetNavigatorContent
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden, // initialValue 不可动态修改,重组时与预取效果不符
        skipHalfExpanded = skipHalfExpanded,
        animationSpec = animationSpec
    )

    val bottomSheetNavigator = remember {
        BottomSheetController(
            navigator = navigator,
            sheetState = sheetState,
            coroutineScope = coroutineScope
        )
    }

    CompositionLocalProvider(LocalSheetController provides bottomSheetNavigator) {
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
                BackHandler(enabled = bottomSheetNavigator.isVisible && hideOnBackPress) {
                    bottomSheetNavigator.back()
                }
                sheetContent(bottomSheetNavigator)
            },
            content = { content(bottomSheetNavigator) }
        )
    }
}

class BottomSheetController internal constructor(
    private val navigator: Navigator,
    private val sheetState: ModalBottomSheetState,
    private val coroutineScope: CoroutineScope
) : Stack<Screen> by navigator, SheetController, EnhanceNavigator {

    override val isVisible: Boolean by derivedStateOf {
        val exitProgress = sheetState.progress(
            from = ModalBottomSheetValue.Expanded,
            to = ModalBottomSheetValue.Hidden
        )

        val enterProgress = sheetState.progress(
            from = ModalBottomSheetValue.Hidden,
            to = ModalBottomSheetValue.Expanded
        )

        enterProgress >= 0.05 || exitProgress < 0.95
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

    override fun hide() {
        if (isVisible) {
            coroutineScope.launch { sheetState.hide() }
        }
    }

    override fun show() {
        if (!isVisible) {
            coroutineScope.launch { sheetState.show() }
        }
    }

    override fun getNavigator(): Navigator = navigator
}