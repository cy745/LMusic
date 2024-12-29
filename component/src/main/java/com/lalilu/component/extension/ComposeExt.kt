package com.lalilu.component.extension

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LocalNavigatorScreenLifecycleProvider
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.jetpack.ScreenLifecycleKMPOwner
import com.lalilu.common.SystemUiUtil
import com.lalilu.component.R
import com.lalilu.component.base.LocalWindowSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.viewmodel.defaultExtras
import kotlin.math.roundToInt

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

@Deprecated("弃用")
@Composable
fun dayNightTextColor(alpha: Float = 1f): Color {
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    return remember(color) { color.copy(alpha = alpha) }
}

@Deprecated("弃用")
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
    return remember(duration) {
        duration.run {
            val hour = this / 3600000
            val minute = this / 60000 % 60
            val second = this / 1000 % 60
            if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
            else "%02d:%02d".format(minute, second)
        }
    }
}

@DrawableRes
@Composable
fun mimeTypeToIcon(mimeType: String): Int {
    return remember(mimeType) {
        val strings = mimeType.split("/").toTypedArray()
        when (strings[strings.size - 1].uppercase()) {
            "FLAC" -> R.drawable.ic_flac_line
            "MPEG", "MP3" -> R.drawable.ic_mp3_line
            "MP4" -> R.drawable.ic_mp4_line
            "APE" -> R.drawable.ic_ape_line
            "DSD" -> R.drawable.ic_dsd_line
            "DSF", "FFMPEG" -> R.drawable.ic_dsf
            "WAV", "X-WAV", "EXT-WAV" -> R.drawable.ic_wav_line
            "OGG" -> R.drawable.ic_ogg
            else -> R.drawable.ic_mp3_line
        }
    }
}

fun <T> List<T>.calcAverage(numToCalc: (T) -> Number): Float {
    return this.fold(0f) { acc, t ->
        acc + numToCalc(t).toFloat()
    } / this.size
}

@Composable
fun <T> buildScrollToItemAction(
    target: T,
    getIndex: (T) -> Int,
    state: LazyGridState = rememberLazyGridState(),
    scope: CoroutineScope = rememberCoroutineScope()
): () -> Unit {
    // 获取当前可见元素的平均高度
    fun getHeightAverage() = state.layoutInfo.visibleItemsInfo.calcAverage { it.size.height }

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

            itemsHeight[index] =
                state.layoutInfo.visibleItemsInfo.getOrNull(index)?.size?.height ?: 0
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

            itemsHeight[index] = state.layoutInfo.visibleItemsInfo.getOrNull(index)?.size ?: 0
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

@Deprecated(message = "弃用")
@Composable
inline fun <reified T : ViewModel> singleViewModel(): T =
    koinViewModel(viewModelStoreOwner = koinInject())

@Composable
inline fun <reified T : ViewModel> Screen.screenVM(
    qualifier: Qualifier? = null,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(
        value = getScreenViewModelStoreOwner() ?: LocalViewModelStoreOwner.current,
        lazyMessage = { "No ViewModelStoreOwner was provided for ${T::class.java}" }
    ),
    key: String? = this.key,
    extras: CreationExtras = defaultExtras(viewModelStoreOwner),
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): T {
    return koinViewModel<T>(
        qualifier = qualifier,
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        extras = extras,
        scope = scope,
        parameters = parameters
    )
}

@OptIn(ExperimentalVoyagerApi::class, InternalVoyagerApi::class)
@Composable
fun Screen.getScreenViewModelStoreOwner(): ViewModelStoreOwner? {
    val provider = LocalNavigatorScreenLifecycleProvider.current

    return remember {
        provider?.provide(this)?.get(0)
            ?.let { it as? ScreenLifecycleKMPOwner }
            ?.owner
    }
}