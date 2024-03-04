package com.lalilu.component.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.base.TabScreen

interface SheetController {
    val isVisible: Boolean
    fun hide()
    fun show()
}

val LocalSheetController: ProvidableCompositionLocal<SheetController?> =
    staticCompositionLocalOf { null }

@Composable
fun BackHandler(
    navigator: SheetController? = LocalSheetController.current,
    onBack: () -> Unit
) {
    BackHandler(enabled = navigator?.isVisible ?: false, onBack = onBack)
}

interface EnhanceNavigator : Stack<Screen> {

    /**
     * 跳转前的操作
     *
     * @param targetScreen 目标跳转页面
     * @return 返回值决定是否继续下一步操作
     */
    fun preJump(targetScreen: Screen): Boolean = true

    /**
     * 实际进行跳转的操作
     *
     * @param targetScreen 目标跳转页面
     * @return 返回值决定是否继续下一步操作
     */
    fun doJump(targetScreen: Screen): Boolean {
        when {
            // Tab类型页面
            targetScreen is TabScreen -> {
                val firstItem = items.firstOrNull()?.let { listOf(it) } ?: emptyList()
                replaceAll(firstItem)


                if (targetScreen != firstItem) {
                    push(targetScreen)
                }
            }

            // 不同类型的页面，添加至导航栈
            lastItemOrNull
                ?.let { it::class.java != targetScreen::class.java }
                ?: true -> push(targetScreen)

            // 同类型页面，替换
            else -> replace(targetScreen)
        }
        return true
    }

    /**
     * 执行跳转后的操作，可在此进行撤销的逻辑
     *
     * @param fromScreen 跳转起始时的页面
     * @param toScreen   跳转目标页面
     * @return 返回值决定是否撤销跳转的操作，返回true表示不撤销，返回false表示撤销
     */
    fun postJump(fromScreen: Screen?, toScreen: Screen): Boolean = true

    /**
     * 进行恢复跳转操作前页面的操作
     *
     * @param fromScreen 跳转起始时的页面
     */
    fun resetTo(fromScreen: Screen?) {}

    /**
     * 获取当前显示的页面
     *
     * @return 当前用户可见的页面
     */
    fun getCurrentScreen(): Screen?

    /**
     * 执行跳转操作
     *
     * @param targetScreen 目标跳转页面
     */
    fun jump(targetScreen: Screen) {
        val currentScreen = getCurrentScreen()

        if (!preJump(targetScreen)) return

        if (!doJump(targetScreen)) return

        if (postJump(currentScreen, targetScreen)) return

        resetTo(currentScreen)
    }

    /**
     * 执行返回操作前的操作
     *
     * @return 返回值决定是否继续下一步操作
     */
    fun preBack(currentScreen: Screen?): Boolean = true

    /**
     * 执行返回操作
     *
     *  @return 返回值决定是否继续下一步操作
     */
    fun doBack(currentScreen: Screen?): Boolean {
        return pop().not()
    }


    /**
     * 执行返回操作后的操作
     */
    fun postBack(fromScreen: Screen?) {}

    /**
     * 执行返回操作
     */
    fun back() {
        val currentScreen = getCurrentScreen()
        if (!preBack(currentScreen)) return
        if (!doBack(currentScreen)) return
        postBack(currentScreen)
    }

    fun getNavigator(): Navigator
}

