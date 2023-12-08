package com.lalilu.component.base

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.navigation.LocalSheetNavigator
import com.lalilu.component.navigation.SheetNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias BottomSheetNavigatorContent = @Composable (bottomSheetNavigator: BottomSheetNavigator) -> Unit

@SuppressLint("UnnecessaryComposedModifier")
@ExperimentalMaterialApi
@Composable
fun BottomSheetNavigatorLayout(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    hideOnBackPress: Boolean = true,
    resetOnHide: Boolean = false,
    visibleWhenShow: Boolean = false,
    defaultIsVisible: Boolean = false,
    defaultScreen: Screen = HiddenBottomSheetScreen,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    sheetGesturesEnabled: Boolean = true,
    skipHalfExpanded: Boolean = true,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    sheetContent: BottomSheetNavigatorContent = { CurrentScreen() },
    content: BottomSheetNavigatorContent
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden, // initialValue 不可动态修改,重组时与预取效果不符
        skipHalfExpanded = skipHalfExpanded,
        animationSpec = animationSpec
    )

    // 只能重组时取值后判断状态进行更新,且需要避免该参数变化触发不必要的重组
    LaunchedEffect(Unit) {
        when {
            defaultIsVisible && sheetState.currentValue == ModalBottomSheetValue.Hidden -> sheetState.show()
            !defaultIsVisible && sheetState.currentValue == ModalBottomSheetValue.Expanded -> sheetState.hide()
        }
    }

    val bottomSheetNavigator = remember {
        BottomSheetNavigator(
            visibleWhenShow = visibleWhenShow,
            resetOnHide = resetOnHide,
            navigator = navigator,
            sheetState = sheetState,
            defaultScreen = defaultScreen,
            coroutineScope = coroutineScope
        )
    }

    CompositionLocalProvider(LocalSheetNavigator provides bottomSheetNavigator) {
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
                BottomSheetNavigatorBackHandler(bottomSheetNavigator, hideOnBackPress)
                sheetContent(bottomSheetNavigator)
            },
            content = { content(bottomSheetNavigator) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetNavigator internal constructor(
    private val visibleWhenShow: Boolean = false,
    private val resetOnHide: Boolean = false,
    private val navigator: Navigator,
    private val defaultScreen: Screen,
    private val sheetState: ModalBottomSheetState,
    private val coroutineScope: CoroutineScope
) : Stack<Screen> by navigator, SheetNavigator {

    override val isVisible: Boolean by derivedStateOf {
        if (sheetState.currentValue == sheetState.targetValue && sheetState.progress == 1f) {
            return@derivedStateOf sheetState.currentValue == ModalBottomSheetValue.Expanded
        }

        if (visibleWhenShow) {
            return@derivedStateOf when (sheetState.currentValue) {
                ModalBottomSheetValue.Hidden -> sheetState.progress >= 0.05f
                ModalBottomSheetValue.Expanded -> sheetState.progress <= 0.95f

                else -> false
            }
        }

        when (sheetState.currentValue) {
            ModalBottomSheetValue.Hidden -> sheetState.progress >= 0.95f
            ModalBottomSheetValue.Expanded -> sheetState.progress <= 0.05f

            else -> false
        }
    }

    override fun pushTab(screen: Screen) {
        if (items.size <= 1) {
            push(screen)
            return
        }

        val firstItem = items.firstOrNull()
        popUntil { it == firstItem }

        if (screen != firstItem) {
            push(screen)
        }
    }

    override fun showSingle(screen: Screen) {
        val lastItem = lastItemOrNull

        if (!isVisible) {
            replaceAll(screen)
        } else {
            if (lastItem == null || lastItem::class.java != screen::class.java) {
                push(screen)
            } else {
                replace(screen)
            }
        }

        if (!isVisible) {
            coroutineScope.launch {
                sheetState.show()
            }
        }
    }

    override fun showMultiple(screen: Screen) {
        if (!isVisible) {
            replaceAll(screen)
        } else {
            push(screen)
        }

        if (!isVisible) {
            coroutineScope.launch {
                sheetState.show()
            }
        }
    }

    override fun show(screen: Screen?) {
        coroutineScope.launch {
            if (screen != null && screen !== lastItemOrNull) {
                replaceAll(screen)
            }
            sheetState.show()
        }
    }

    override fun hide() {
        coroutineScope.launch {
            if (isVisible) {
                sheetState.hide()
            } else if (resetOnHide && sheetState.targetValue == ModalBottomSheetValue.Hidden) {
                // Swipe down - sheetState is already hidden here so `isVisible` is false
                replaceAll(defaultScreen)
            }
        }
    }

    override fun getNavigator(): Navigator {
        return navigator
    }
}

object HiddenBottomSheetScreen : Screen {

    @Composable
    override fun Content() {
        Spacer(modifier = Modifier.height(1.dp))
    }
}


@ExperimentalMaterialApi
@Composable
fun BottomSheetNavigatorBackHandler(
    navigator: SheetNavigator,
    hideOnBackPress: Boolean
) {
    BackHandler(enabled = navigator.isVisible) {
        // 若当前只剩一个页面，则不清空元素了
        if (navigator.items.size <= 1) {
            navigator.hide()
            return@BackHandler
        }

        if (navigator.pop().not() && hideOnBackPress) {
            navigator.hide()
        }
    }
}

