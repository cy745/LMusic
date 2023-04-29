package com.lalilu.lmusic.compose.component

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.KeyboardUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.lmusic.compose.new_screen.NavBar
import com.lalilu.lmusic.utils.extension.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
object SmartModalBottomSheet {
    private var scope: CoroutineScope? = null
    private val scaffoldState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = SpringSpec(stiffness = 1000f),
        isSkipHalfExpanded = true
    )
    val enableFadeEdgeForStatusBar = mutableStateOf(true)
    val isVisible = derivedStateOf { scaffoldState.isVisible || scaffoldState.isAnimationRunning }
    val isVisibleFlow = snapshotFlow { isVisible.value }

    val offset: Float
        get() = scaffoldState.offset.value
    val offsetHalfPercent: Float
        get() = scaffoldState.progress.watchForOffset(
            ModalBottomSheetValue.Expanded,
            ModalBottomSheetValue.Hidden
        )

    fun hide() = scope?.launch { scaffoldState.hide() }
    fun show() = scope?.launch { scaffoldState.show() }


    /**
     * 注册一个Composable用于短暂地关闭FadeEdge功能，在Composable被移除时恢复原状
     */
    @Composable
    fun RegisterForTemporaryDisableFadeEdge() {
        LaunchedEffect(Unit) {
            enableFadeEdgeForStatusBar.value = false
        }

        DisposableEffect(Unit) {
            onDispose {
                enableFadeEdgeForStatusBar.value = true
            }
        }
    }

    @SuppressLint("UnnecessaryComposedModifier")
    @Composable
    fun SmartModalBottomSheetContent(
        scope: CoroutineScope = rememberCoroutineScope(),
        sheetContent: @Composable BoxScope.() -> Unit,
        content: @Composable BoxScope.() -> Unit
    ) {
        this.scope = scope
        val context = LocalContext.current
        val windowSize = LocalWindowSize.current
        val configuration = LocalConfiguration.current
        val navController = LocalNavigatorHost.current

        val isDarkModeNow = isSystemInDarkTheme()
        val statusBarHeight = rememberFixedStatusBarHeight()
        val systemUiController = rememberSystemUiController()
        val offsetRoundedCorner = LocalDensity.current.run { 15.dp.toPx() }
        val isPad by windowSize.rememberIsPad()

        val isExpended by remember {
            derivedStateOf { offset < statusBarHeight }
        }
        val isLandscape by remember {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }

        val screenHeightDp = configuration.screenHeightDp.dp

        /**
         * 监听isVisible变化，通过BottomSheet的可见性控制navController是否处理返回键事件
         * 若 [rememberIsPad] 为平板设备，则必须处理返回键事件
         */
        LaunchedEffect(isVisible.value, isPad) {
            // [enableOnBackPressed] 被 gradle 认为是 RestrictedApi
            //noinspection RestrictedApi
            navController.enableOnBackPressed(isVisible.value || isPad)
        }

        LaunchedEffect(isExpended, isDarkModeNow) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = isExpended && !isDarkModeNow
            )
        }

        // TODO 待完善，响应速度比预期慢一大截
        LaunchedEffect(scaffoldState.currentValue) {
            if (scaffoldState.currentValue != ModalBottomSheetValue.Expanded) {
                context.getActivity()?.let {
                    if (KeyboardUtils.isSoftInputVisible(it)) {
                        KeyboardUtils.hideSoftInput(it)
                    }
                }
            }
        }

        if (isPad) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isLandscape) {
                    NavBar.verticalContent()
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    content = sheetContent
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .composed {
                            if (isLandscape) width(screenHeightDp / 2) else fillMaxWidth(0.5f)
                        },
                    content = {
                        Surface(
                            elevation = 5.dp,
                            color = MaterialTheme.colors.background
                        ) {
                            content()
                        }
                    }
                )
            }
        } else {
            ModalBottomSheetLayout(
                sheetState = scaffoldState,
                modifier = if (isLandscape) {
                    Modifier.aspectRatio(9f / 16f, true)
                } else {
                    Modifier.fillMaxSize()
                },
                sheetBackgroundColor = MaterialTheme.colors.background,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                /**
                 * 部分系统、部分机型设置了SheetShape后，会导致sheetContent中操作Canvas时出现异常情况
                 * 具体表现为Canvas使用saveLayer函数传入的Rect命中sheetShape的区域时，会导致Canvas绘制的内容被”移除“
                 * 猜测为SheetShape截除区域时将Canvas的saveLayer也认为是需要截除的内容了
                 *
                 * sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                 */
                sheetContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        content = sheetContent
                    )
                },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                GenericShape { size, _ ->
                                    val height =
                                        if (scaffoldState.isVisible) offset + offsetRoundedCorner else size.height
                                    addRect(Rect(0f, 0f, size.width, height))
                                }
                            ),
                        content = content
                    )
                }
            )
        }

        /**
         * 注册一个返回事件处理机，当BottomSheet可见时启用
         */
        BackHandler(enabled = isVisible.value) {
            hide()
        }
    }
}