package com.lalilu.lmusic.utils.extension

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lalilu.common.SystemUiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
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
    // 若目标路径与当前路径相同时不进行导航
    // TODO 现无法判断带有参数的路径，需要实现从路径中提取参数并比对的逻辑
    if (this.currentBackStackEntry?.destination?.route == to) return

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
    val screenHeightDp = configuration.screenHeightDp.dp + WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding() + WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()
    return remember {
        screenHeightDp
    }
}

@Composable
fun rememberScreenHeightInPx(): Int {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp + WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding() + WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()
    return remember(screenHeightDp, configuration) {
        (screenHeightDp.value * configuration.densityDpi / 160f).roundToInt()
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun SwipeProgress<ModalBottomSheetValue>.watchForOffset(
    betweenFirst: ModalBottomSheetValue, betweenSecond: ModalBottomSheetValue, elseValue: Float = 1f
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
fun WindowSizeClass.rememberIsPad(): State<Boolean> {
    return remember(widthSizeClass, heightSizeClass) {
        derivedStateOf {
            widthSizeClass != WindowWidthSizeClass.Compact && heightSizeClass != WindowHeightSizeClass.Compact
        }
    }
}

@Composable
fun dayNightTextColor(alpha: Float = 1f): Color {
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    return remember(color) { color.copy(alpha = alpha) }
}

@Composable
fun dayNightTextColorFilter(alpha: Float = 1f): ColorFilter {
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    return remember(color) { color.copy(alpha = alpha).toColorFilter() }
}

fun Color.toColorFilter(): ColorFilter {
    return ColorFilter.tint(color = this)
}

@Composable
fun durationMsToString(duration: Long): String {
    return remember(duration) { duration.durationToTime() }
}

@DrawableRes
@Composable
fun mimeTypeToIcon(mimeType: String): Int {
    return remember(mimeType) { getMimeTypeIconRes(mimeType) }
}

@Composable
fun <T> buildScrollToItemAction(
    target: T,
    getIndex: (T) -> Int,
    state: LazyGridState = rememberLazyGridState(),
    scope: CoroutineScope = rememberCoroutineScope()
): () -> Unit {
    // 获取当前可见元素的平均高度
    fun getHeightAverage() = state.layoutInfo.visibleItemsInfo.average { it.size.height }

    // 获取精确的位移量（只能对可见元素获取）
    fun getTargetOffset(index: Int) =
        state.layoutInfo.visibleItemsInfo.find { it.index == index }?.offset?.y

    // 获取粗略的位移量（通过对可见元素的高度求平均再通过index的差，计算出粗略值）
    fun getRoughTargetOffset(index: Int) =
        getHeightAverage() * (index - state.firstVisibleItemIndex - 1)

    return {
        scope.launch {
            val index = getIndex(target)
            if (index >= 0) {
                // 若获取不到精确的位移量，则计算粗略位移量并开始scroll
                if (getTargetOffset(index) == null) {
                    state.animateScrollBy(
                        getRoughTargetOffset(index), SpringSpec(stiffness = Spring.StiffnessVeryLow)
                    )
                }

                // 若可以获取到精确的位移量，则直接滚动到目标歌曲位置
                getTargetOffset(index)?.let {
                    state.animateScrollBy(
                        it.toFloat(), SpringSpec(stiffness = Spring.StiffnessVeryLow)
                    )
                }
            }
        }
    }
}


/**
 * [LaunchedEffect] 在监听值的变化的同时无法兼顾Composition的变化，会在Compose移除后继续执行内部代码
 * [DisposableEffect] 在进入Composition的时候无法处理初始值，只会处理之后值变化的情况
 *
 * 为了处理初始值和避免Compose移除后仍处理值的变化的问题，创建了这个Effect
 */
@Composable
fun <T> LaunchedDisposeEffect(
    key: () -> T,
    onDispose: () -> Unit = {},
    onUpdate: (key: T) -> Unit
) {
    val item = key()
    DisposableEffect(item) {
        onUpdate(item)
        onDispose(onDispose)
    }

    LaunchedEffect(Unit) {
        onUpdate(item)
    }
}

/**
 * 监听滚动位置的变化，计算总的滚动距离
 */
@Composable
fun rememberScrollPosition(
    state: LazyGridState
): State<Float> {
    val itemsHeight = remember { mutableStateMapOf<Int, Int>() }

    return remember {
        derivedStateOf {
            val index = state.firstVisibleItemIndex
            itemsHeight[index] = state.layoutInfo.visibleItemsInfo[index].size.height
            val sumHeight = (0 until index)
                .mapNotNull { itemsHeight[it] }
                .fold(0f) { total, item -> total + item }

            state.firstVisibleItemScrollOffset + sumHeight
        }
    }
}

/**
 * 监听滚动位置的变化，计算总的滚动距离
 */
@Composable
fun rememberScrollPosition(
    state: LazyListState
): State<Float> {
    val itemsHeight = remember { mutableStateMapOf<Int, Int>() }

    return remember {
        derivedStateOf {
            val index = state.firstVisibleItemIndex
            itemsHeight[index] = state.layoutInfo.visibleItemsInfo[index].size
            val sumHeight = (0 until index)
                .mapNotNull { itemsHeight[it] }
                .fold(0f) { total, item -> total + item }

            state.firstVisibleItemScrollOffset + sumHeight
        }
    }
}

@Composable
fun rememberFixedStatusBarHeight(): Int {
    val context = LocalContext.current
    return remember {
        SystemUiUtil.getFixedStatusHeight(context)
    }
}

@Composable
fun rememberFixedStatusBarHeightDp(): Dp {
    val density = LocalDensity.current
    val px = rememberFixedStatusBarHeight()
    return remember { density.run { px.toDp() } }
}

@Composable
fun rememberIsPadLandScape(): State<Boolean> {
    val configuration = LocalConfiguration.current
    val windowSize = LocalWindowSize.current
    val isPad by windowSize.rememberIsPad()
    return remember(configuration.orientation, isPad) {
        derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && isPad }
    }
}

@Composable
inline fun <reified T : ViewModel> singleViewModel(): T =
    koinViewModel(viewModelStoreOwner = koinInject())