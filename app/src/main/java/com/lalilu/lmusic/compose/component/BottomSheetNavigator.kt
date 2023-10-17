package com.lalilu.lmusic.compose.component

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
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.compositionUniqueId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias BottomSheetNavigatorContent = @Composable (bottomSheetNavigator: BottomSheetNavigator) -> Unit

val LocalBottomSheetNavigator: ProvidableCompositionLocal<BottomSheetNavigator> =
    staticCompositionLocalOf { error("BottomSheetNavigator not initialized") }

@SuppressLint("UnnecessaryComposedModifier")
@OptIn(InternalVoyagerApi::class)
@ExperimentalMaterialApi
@Composable
fun BottomSheetNavigator(
    modifier: Modifier = Modifier,
    hideOnBackPress: Boolean = true,
    ignoreFlingNestedScroll: Boolean = false,
    defaultScreen: Screen = HiddenBottomSheetScreen,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    sheetGesturesEnabled: Boolean = true,
    skipHalfExpanded: Boolean = true,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    key: String = compositionUniqueId(),
    sheetContent: BottomSheetNavigatorContent = { CurrentScreen() },
    content: BottomSheetNavigatorContent
) {
    var hideBottomSheet: (() -> Unit)? = null
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { state ->
            if (state == ModalBottomSheetValue.Hidden) {
                hideBottomSheet?.invoke()
            }
            true
        },
        skipHalfExpanded = skipHalfExpanded,
        animationSpec = animationSpec
    )
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.Fling) {
                    if (sheetState.progress == 1f || sheetState.progress == 0f) {
                        // 消费剩余的所有Fling运动
                        return available
                    }
                }

                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    Navigator(defaultScreen, onBackPressed = null, key = key) { navigator ->
        val bottomSheetNavigator = remember(navigator, sheetState, coroutineScope) {
            BottomSheetNavigator(navigator, sheetState, coroutineScope)
        }

        hideBottomSheet = bottomSheetNavigator::hide

        CompositionLocalProvider(LocalBottomSheetNavigator provides bottomSheetNavigator) {
            ModalBottomSheetLayout(
                modifier = modifier.composed {
                    if (ignoreFlingNestedScroll) this.nestedScroll(nestedScrollConnection) else this
                },
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
                content = {
                    content(bottomSheetNavigator)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetNavigator internal constructor(
    private val navigator: Navigator,
    private val sheetState: ModalBottomSheetState,
    private val coroutineScope: CoroutineScope
) : Stack<Screen> by navigator {

    val isVisible: Boolean by derivedStateOf {
        if (sheetState.currentValue == sheetState.targetValue && sheetState.progress == 1f) {
            return@derivedStateOf sheetState.currentValue == ModalBottomSheetValue.Expanded
        }

        when (sheetState.currentValue) {
            ModalBottomSheetValue.Hidden -> sheetState.progress >= 0.95f
            ModalBottomSheetValue.Expanded -> sheetState.progress <= 0.05f

            else -> false
        }
    }

    fun pushTab(screen: Screen) {
        if (items.size <= 1) {
            push(screen)
            return
        }

        val firstItem = items.firstOrNull()
        popUntil { it == firstItem }

        if (screen != firstItem) {
            replace(screen)
        }
    }

    fun showSingle(screen: Screen) {
        val lastItem = lastItemOrNull

        if (lastItem == null || lastItem::class.java != screen::class.java) {
            push(screen)
        } else {
            replace(screen)
        }
    }

    fun show(screen: Screen? = null) {
        coroutineScope.launch {
            if (screen != null && screen !== navigator.lastItemOrNull) {
                replaceAll(screen)
            }
            sheetState.show()
        }
    }

    fun hide() {
        coroutineScope.launch {
            if (isVisible) {
                sheetState.hide()
            }
        }
    }

    fun getNavigator(): Navigator {
        return navigator
    }

    @Composable
    fun saveableState(
        key: String,
        content: @Composable () -> Unit
    ) {
        navigator.saveableState(key, content = content)
    }
}

private object HiddenBottomSheetScreen : Screen {

    @Composable
    override fun Content() {
        Spacer(modifier = Modifier.height(1.dp))
    }
}


@ExperimentalMaterialApi
@Composable
internal fun BottomSheetNavigatorBackHandler(
    navigator: BottomSheetNavigator,
    hideOnBackPress: Boolean
) {
    BackHandler(enabled = navigator.isVisible) {
        // 若当前只剩一个页面，则不清空元素了
        if (navigator.items.size == 1) {
            navigator.hide()
            return@BackHandler
        }

        if (navigator.pop().not() && hideOnBackPress) {
            navigator.hide()
        }
    }
}
