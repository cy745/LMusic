package com.lalilu.lmusic.utils.extension

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeProgress
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.palette.graphics.Palette
import coil.request.ImageRequest
import com.lalilu.lmusic.utils.PaletteTransformation
import kotlin.math.roundToInt

fun NavController.canPopUp(): Boolean {
    return previousBackStackEntry != null
}

fun NavController.popUpElse(elseDo: () -> Unit) {
    if (canPopUp()) this.navigateUp() else elseDo()
}

/**
 * 递归清除返回栈
 */
fun NavController.clearBackStack() {
    if (popBackStack()) clearBackStack()
}

/**
 * @param to    目标导航位置
 *
 * 指定导航起点位置和目标位置
 */
fun NavController.navigateSingleTop(
    to: String,
) {
    navigate(to) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun rememberStatusBarHeight(): Float {
    val density = LocalDensity.current
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return remember(statusBarHeightDp, density) {
        density.run { statusBarHeightDp.toPx() }
    }
}

@Composable
fun rememberScreenHeight(): Dp {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return remember {
        screenHeightDp
    }
}

@Composable
fun rememberScreenHeightInPx(): Int {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return remember(screenHeightDp, configuration) {
        (screenHeightDp.value * configuration.densityDpi / 160f).roundToInt()
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun SwipeProgress<ModalBottomSheetValue>.watchForOffset(
    betweenFirst: ModalBottomSheetValue,
    betweenSecond: ModalBottomSheetValue,
    elseValue: Float = 1f
): Float = when {
    from == betweenFirst && to == betweenSecond -> 1f - (fraction * 3)
    from == betweenSecond && to == betweenFirst -> fraction * 3
    else -> elseValue
}.coerceIn(0f, 1f)

/**
 * 根据屏幕的长宽类型来判断设备是否平板
 * 依据是：平板没有一条边会是Compact的
 */
@Composable
fun WindowSizeClass.isPad(): Boolean {
    return remember(widthSizeClass, heightSizeClass) {
        widthSizeClass != WindowWidthSizeClass.Compact
                && heightSizeClass != WindowHeightSizeClass.Compact
    }
}

@Composable
fun ImageRequest.Builder.requirePalette(callback: (Palette) -> Unit): ImageRequest.Builder {
    return transformations(remember { PaletteTransformation(callback = callback) })
}