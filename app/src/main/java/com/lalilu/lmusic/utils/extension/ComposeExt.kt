package com.lalilu.lmusic.utils.extension

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeProgress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.lmusic.screen.MainScreenData

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
 * @param from  所设起点导航位置
 * @param to    目标导航位置
 *
 * 指定导航起点位置和目标位置
 */
fun NavController.navigate(
    to: String,
    from: String = MainScreenData.Library.name,
    clearAllBefore: Boolean = false,
    singleTop: Boolean = true
) {
    if (clearAllBefore) clearBackStack()
    navigate(to) {
        launchSingleTop = singleTop
        popUpTo(from) { inclusive = from == to }
    }
}

@Composable
fun BackHandlerWithNavigator(navController: NavController, onBack: () -> Unit) {
    // 当无上一级可导航时，启用此返回事件拦截器
    val enable = remember(navController.currentBackStackEntryAsState().value) {
        !navController.canPopUp()
    }

    BackHandler(
        enabled = enable,
        onBack = onBack
    )
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