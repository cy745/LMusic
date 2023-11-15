package com.lalilu.component.base

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.compositionUniqueId
import com.lalilu.component.navigation.LocalSheetNavigator
import com.lalilu.component.navigation.SheetNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(InternalVoyagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun SideSheetNavigatorLayout(
    modifier: Modifier = Modifier,
    hideOnBackPress: Boolean = true,
    defaultScreen: Screen = HiddenSideSheetScreen,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    key: String = compositionUniqueId(),
    sheetContent: @Composable (SheetNavigator) -> Unit = { CurrentScreen() },
    content: @Composable (SheetNavigator) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalSideSheetState()

    Navigator(defaultScreen, onBackPressed = null, key = key) { navigator ->
        val sheetNavigator = remember(navigator, sheetState, coroutineScope) {
            SideSheetNavigator(
                navigator = navigator,
                sheetState = sheetState,
                coroutineScope = coroutineScope
            )
        }

        CompositionLocalProvider(
            LocalSheetNavigator provides sheetNavigator
        ) {
            ModalSideSheetLayout(
                modifier = modifier,
                alignment = Alignment.CenterStart,
                scrimColor = scrimColor,
                sheetShape = sheetShape,
                sheetState = sheetState,
                animationSpec = animationSpec,
                sheetElevation = sheetElevation,
                sheetBackgroundColor = sheetBackgroundColor,
                sheetContentColor = sheetContentColor,
                sheetContent = {
                    SideSheetNavigatorBackHandler(sheetNavigator, hideOnBackPress)
                    sheetContent(sheetNavigator)
                },
                content = { content(sheetNavigator) }
            )
        }
    }
}

private object HiddenSideSheetScreen : Screen {

    @Composable
    override fun Content() {
        Spacer(modifier = Modifier.height(1.dp))
    }
}

class SideSheetNavigator(
    private val navigator: Navigator,
    private val sheetState: ModalSideSheetState,
    private val coroutineScope: CoroutineScope
) : Stack<Screen> by navigator, SheetNavigator {
    override val isVisible: Boolean by derivedStateOf { sheetState.isVisible }

    override fun pushTab(screen: Screen) {
    }

    override fun showSingle(screen: Screen, replaceAllWhenInvisible: Boolean) {
        val lastItem = lastItemOrNull

        // 若当前为隐藏状态
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
                sheetState.isVisible = true
            }
        }
    }

    override fun show(screen: Screen?) {
        coroutineScope.launch {
            if (screen != null && screen !== navigator.lastItemOrNull) {
                replaceAll(screen)
            }
            sheetState.isVisible = true
        }
    }

    override fun hide() {
        coroutineScope.launch {
            sheetState.isVisible = false
        }
    }

    override fun getNavigator(): Navigator {
        return navigator
    }
}

@ExperimentalMaterialApi
@Composable
fun SideSheetNavigatorBackHandler(
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
