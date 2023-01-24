package com.lalilu.lmusic.compose.component

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.KeyboardUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.lmusic.compose.component.navigate.NavigateBar
import com.lalilu.lmusic.utils.extension.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
object SmartModalBottomSheet {
    private var scope: CoroutineScope? = null
    private val enableFadeEdgeForStatusBar = mutableStateOf(true)
    private val scaffoldState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = SpringSpec(stiffness = 1000f),
        isSkipHalfExpanded = true
    )

    val offset: Float
        get() = scaffoldState.offset.value
    val offsetHalfPercent: Float
        get() = scaffoldState.progress.watchForOffset(
            ModalBottomSheetValue.Expanded,
            ModalBottomSheetValue.Hidden
        )

    fun hide() = scope?.launch { scaffoldState.hide() }
    fun show() = scope?.launch { scaffoldState.show() }
    fun expend() = scope?.launch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    fun collapse() = scope?.launch { scaffoldState.animateTo(ModalBottomSheetValue.HalfExpanded) }
    fun fadeEdge(value: Boolean) = scope?.launch { enableFadeEdgeForStatusBar.value = value }

    @Composable
    fun SmartModalBottomSheetContent(
        scope: CoroutineScope = rememberCoroutineScope(),
        sheetContent: @Composable BoxScope.() -> Unit,
        content: @Composable BoxScope.() -> Unit
    ) {
        LaunchedEffect(scope) {
            SmartModalBottomSheet.scope = scope
        }
        val context = LocalContext.current
        val windowSize = LocalWindowSize.current
        val configuration = LocalConfiguration.current
        val navController = LocalNavigatorHost.current

        val offset = scaffoldState.offset.value
        val isDarkModeNow = isSystemInDarkTheme()
        val statusBarHeight = rememberStatusBarHeight()
        val systemUiController = rememberSystemUiController()
        val offsetRoundedCorner = LocalDensity.current.run { 15.dp.toPx() }
        val isPad by windowSize.rememberIsPad()

        val isExpended by remember(offset, statusBarHeight) {
            derivedStateOf { offset < statusBarHeight }
        }
        val isVisible by remember(scaffoldState.isVisible, scaffoldState.isAnimationRunning) {
            derivedStateOf { scaffoldState.isVisible || scaffoldState.isAnimationRunning }
        }
        val isLandscape by remember(configuration.orientation) {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }

        val screenHeightDp = configuration.screenHeightDp.dp +
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()

        /**
         * 监听isVisible变化，通过BottomSheet的可见性控制navController是否处理返回键事件
         * 若 [rememberIsPad] 为平板设备，则必须处理返回键事件
         */
        LaunchedEffect(isVisible, isPad) {
            // [enableOnBackPressed] 被 gradle 认为是 RestrictedApi
            //noinspection RestrictedApi
            navController.enableOnBackPressed(isVisible || isPad)
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
                    NavigateBar(horizontal = false)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    content = sheetContent
                )
                Box(
                    modifier = if (isLandscape) {
                        Modifier.width(screenHeightDp / 2)
                    } else {
                        Modifier.fillMaxWidth(0.5f)
                    }.fillMaxHeight(),
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
                sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .edgeTransparentForStatusBar(enableFadeEdgeForStatusBar.value),
                        content = sheetContent
                    )
                },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                GenericShape { size, _ ->
                                    addRect(
                                        Rect(
                                            0f, 0f, size.width,
                                            if (scaffoldState.isVisible) offset + offsetRoundedCorner else size.height
                                        )
                                    )
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
        BackHandler(enabled = isVisible) {
            hide()
        }
    }
}