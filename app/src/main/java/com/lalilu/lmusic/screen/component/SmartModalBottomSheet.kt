package com.lalilu.lmusic.screen.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.blankj.utilcode.util.KeyboardUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.lmusic.utils.extension.edgeTransparentForStatusBar
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.extension.rememberStatusBarHeight
import com.lalilu.lmusic.utils.extension.watchForOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
object SmartModalBottomSheet : KeyboardUtils.OnSoftInputChangedListener {
    private var scope: CoroutineScope? = null
    private val scaffoldState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    private val enableFadeEdgeForStatusBar = mutableStateOf(true)

    val offset: Float
        get() = scaffoldState.offset.value
    val offsetHalfPercent: Float
        get() = scaffoldState.progress.watchForOffset(
            ModalBottomSheetValue.HalfExpanded,
            ModalBottomSheetValue.Hidden
        )

    fun hide() = scope?.launch { scaffoldState.hide() }
    fun show() = scope?.launch { scaffoldState.show() }
    fun expend() = scope?.launch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    fun collapse() = scope?.launch { scaffoldState.animateTo(ModalBottomSheetValue.HalfExpanded) }
    fun enableFadeEdge() = scope?.launch { enableFadeEdgeForStatusBar.value = true }
    fun disableFadeEdge() = scope?.launch { enableFadeEdgeForStatusBar.value = false }

    override fun onSoftInputChanged(height: Int) {
//        if (height > 100 && scaffoldState.currentValue != ModalBottomSheetValue.Expanded) {
//            expend()
//        }
        println("onSoftInputChanged: $height")
    }

    @Composable
    fun SmartModalBottomSheetContent(
        navController: NavController,
        scope: CoroutineScope = rememberCoroutineScope(),
        sheetContent: @Composable BoxScope.() -> Unit,
        content: @Composable BoxScope.() -> Unit
    ) {
        this.scope = scope
        val context = LocalContext.current
        val offset = scaffoldState.offset.value
        val isDarkModeNow = isSystemInDarkTheme()
        val statusBarHeight = rememberStatusBarHeight()
        val systemUiController = rememberSystemUiController()
        val isExpended = remember(offset, statusBarHeight) { offset < statusBarHeight }
        val isVisible = scaffoldState.isVisible || scaffoldState.isAnimationRunning

        /**
         * 监听isVisible变化，通过BottomSheet的可见性控制navController是否处理返回键事件
         */
        LaunchedEffect(isVisible) {
            navController.enableOnBackPressed(isVisible)
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

//        LaunchedEffect(Unit) {
//            context.getActivity()?.let {
//                KeyboardUtils.registerSoftInputChangedListener(it, this@SmartModalBottomSheet)
//            }
//        }
//
//        DisposableEffect(Unit) {
//            onDispose {
//                context.getActivity()?.let {
//                    KeyboardUtils.unregisterSoftInputChangedListener(it.window)
//                }
//            }
//        }
        val offsetRoundedCorner = LocalDensity.current.run { 15.dp.toPx() }

        ModalBottomSheetLayout(
            sheetState = scaffoldState,
            modifier = Modifier
                .fillMaxWidth(),
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

        /**
         * 注册一个返回事件处理机，当BottomSheet可见时启用
         */
        BackHandler(enabled = isVisible) {
            hide()
        }
    }
}