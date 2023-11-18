package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.component.base.BottomSheetNavigatorLayout
import com.lalilu.component.base.CustomScreen
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.component.base.SideSheetNavigatorLayout
import com.lalilu.component.base.TabScreen
import com.lalilu.component.navigation.SheetNavigator
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.component.navigate.NavigationBar
import com.lalilu.lmusic.compose.component.navigate.NavigationSmartBar
import com.lalilu.lmusic.compose.new_screen.HomeScreen


@OptIn(ExperimentalMaterialApi::class)
object NavigationWrapper {
    var navigator: SheetNavigator? by mutableStateOf(null)
        private set

    // 使用remember避免在该变量内部的state引用触发重组,使其转换为普通的变量
    private val isSheetVisible: Boolean
        @Composable
        get() = remember { navigator?.isVisible ?: false }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        navigator: Navigator,
        defaultScreen: Screen,
        forPad: () -> Boolean = { false }
    ) {
        val animationSpec = remember {
            SpringSpec<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 1000f
            )
        }

        // 使用Composable封装函数避免过度参与重组,变换处理完导航栈后交予后续逻辑用于判断
        val resultScreen = stackTransform(
            forPad = forPad,
            items = navigator.items,
            defaultScreen = defaultScreen,
            onResult = { navigator.replaceAll(it) }
        )

        if (forPad()) {
            SideSheetNavigatorLayout(
                modifier = modifier.fillMaxSize(),
                navigator = navigator,
                defaultIsVisible = isSheetVisible && resultScreen.lastOrNull() != defaultScreen,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                sheetBackgroundColor = MaterialTheme.colors.background,
                animationSpec = animationSpec,
                sheetContent = { sheetNavigator ->
                    NavigationWrapper.navigator = sheetNavigator
                    SheetContent(
                        modifier = modifier,
                        transitionKeyPrefix = "SideSheet",
                        sheetNavigator = sheetNavigator
                    )
                },
                content = { TabWrapper.Content() }
            )
        } else {
            BottomSheetNavigatorLayout(
                modifier = modifier.fillMaxSize(),
                navigator = navigator,
                defaultScreen = HomeScreen,
                defaultIsVisible = isSheetVisible,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                sheetBackgroundColor = MaterialTheme.colors.background,
                animationSpec = animationSpec,
                sheetContent = { sheetNavigator ->
                    NavigationWrapper.navigator = sheetNavigator
                    SheetContent(
                        modifier = modifier,
                        transitionKeyPrefix = "bottomSheet",
                        sheetNavigator = sheetNavigator
                    )
                },
                content = { }
            )
        }
    }

    /**
     * 当导航的显示模式切换时，转换处理导航栈，使其适应对应的显示模式
     *
     * 当切换至平板状态时:
     *  1.需要移除导航栈内所有TabScreen
     *  2.转移导航栈中的最后一个TabScreen交予TabWrapper显示
     *  3.确保导航栈不为空
     *
     * 当切换至BottomSheet状态时:
     *  1.必须保证导航栈的最低一个是HomeScreen
     *  2.需要把TabWrapper内显示的TabScreen添加至导航栈的底部
     *
     *  该函数的特性是,依赖重组产生的页面切换的事件,且需要在该重组事件的前面完成,且无法通过副作用函数解决
     */
    @Composable
    private fun stackTransform(
        forPad: () -> Boolean,
        items: List<Screen>,
        defaultScreen: Screen,
        onResult: (List<Screen>) -> Unit = {}
    ): List<Screen> {
        if (currentComposer.skipping) return items

        return if (forPad()) {
            // 将导航栈中最后一个TabScreen类型的Screen显示在TabWrapper上
            items.lastOrNull { TabWrapper.tabScreen.contains(it) }
                ?.let { it as? TabScreen }
                ?.let { TabWrapper.postScreen(it) }

            // 移除所有的TabScreen
            val result = items.filter { it !is TabScreen }
                .toMutableList()

            // 需要确保result不为空,则当其为空时补充一个defaultScreen
            if (result.isEmpty()) {
                result.add(defaultScreen)
            }

            result.also(onResult)
        } else {
            // 移除所有的TabScreen 和 defaultScreen
            val result = items.filter { it !is TabScreen && it != defaultScreen }
                .toMutableList()

            // 若TabWrapper内正显示的页面非HomeScreen,则将其加入底部
            TabWrapper.navigator?.current
                ?.takeIf { it != HomeScreen }
                ?.let { result.add(0, it) }

            // 确保导航栈的最底部为HomeScreen
            result.add(0, HomeScreen)

            result.also(onResult)
        }
    }

    @Composable
    private fun SheetContent(
        modifier: Modifier,
        transitionKeyPrefix: String,
        sheetNavigator: SheetNavigator
    ) {
        val currentPaddingValue = remember { mutableStateOf(PaddingValues(0.dp)) }
        var currentScreen by remember { mutableStateOf<Screen?>(null) }
        val customScreenInfo by remember { derivedStateOf { (currentScreen as? CustomScreen)?.getScreenInfo() } }

        ImmerseStatusBar(
            enable = { customScreenInfo?.immerseStatusBar == true },
            isExpended = { sheetNavigator.isVisible }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            CustomTransition(
                modifier = Modifier.fillMaxSize(),
                keyPrefix = transitionKeyPrefix,
                navigator = sheetNavigator.getNavigator()
            ) {
                currentScreen = it
                CompositionLocalProvider(LocalPaddingValue provides currentPaddingValue) {
                    it.Content()
                }
            }
            NavigationSmartBar(
                modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                measureHeightState = currentPaddingValue,
                currentScreen = { navigator?.lastItemOrNull }
            ) { modifier ->
                NavigationBar(
                    modifier = modifier.align(Alignment.BottomCenter),
                    tabScreens = { TabWrapper.tabScreen },
                    currentScreen = { currentScreen },
                    navigator = sheetNavigator
                )
            }
        }
    }
}

@Composable
fun ImmerseStatusBar(
    enable: () -> Boolean = { true },
    isExpended: () -> Boolean = { false },
) {
    val result by remember { derivedStateOf { isExpended() && enable() } }
    val systemUiController = rememberSystemUiController()
    val isDarkModeNow = isSystemInDarkTheme()

    LaunchedEffect(result, isDarkModeNow) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = result && !isDarkModeNow
        )
    }
}