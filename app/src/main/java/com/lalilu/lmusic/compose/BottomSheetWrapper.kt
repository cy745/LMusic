package com.lalilu.lmusic.compose

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
object BottomSheetWrapper : NavController.OnDestinationChangedListener, NestedScrollConnection {
    private lateinit var sheetState: ModalBottomSheetState
    private var scope: CoroutineScope? = null

    private fun createSheetState(density: Density) {
        sheetState = ModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            density = density,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 1000f
            ),
            isSkipHalfExpanded = true,
        )
    }

    fun show() = scope?.launch { sheetState.show() }
    fun hide() = scope?.launch { sheetState.hide() }

    @Composable
    fun collectBottomSheetIsExpended(): State<Boolean> = remember {
        derivedStateOf {
            if (!BottomSheetWrapper::sheetState.isInitialized) return@derivedStateOf false

            if (sheetState.currentValue == sheetState.targetValue && sheetState.progress == 1f) {
                return@derivedStateOf sheetState.currentValue == ModalBottomSheetValue.Expanded
            }

            when (sheetState.currentValue) {
                ModalBottomSheetValue.Hidden -> sheetState.progress >= 0.95f
                ModalBottomSheetValue.Expanded -> sheetState.progress <= 0.05f

                else -> false
            }
        }
    }

    @Composable
    fun BackHandler(
        forVisible: () -> Boolean = { false },
        enable: () -> Boolean = { true },
        callback: () -> Unit,
    ) {
        if (!BottomSheetWrapper::sheetState.isInitialized) {
            createSheetState(LocalDensity.current)
        }
        val isVisible by collectBottomSheetIsExpended()

        BackHandler(
            enabled = isVisible == forVisible() && enable(),
            onBack = callback
        )
    }

    @Composable
    fun Content(
        mainContent: @Composable () -> Unit,
        secondContent: @Composable () -> Unit,
        scope: CoroutineScope = rememberCoroutineScope(),
        navController: NavHostController = LocalNavigatorHost.current,
    ) {
        this.scope = scope
        if (!BottomSheetWrapper::sheetState.isInitialized) {
            createSheetState(LocalDensity.current)
            navController.removeOnDestinationChangedListener(this)
            navController.addOnDestinationChangedListener(this)
        }

        val systemUiController = rememberSystemUiController()
        val isDarkModeNow = isSystemInDarkTheme()
        val isExpended by collectBottomSheetIsExpended()

        LaunchedEffect(isExpended, isDarkModeNow) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = isExpended && !isDarkModeNow
            )
        }

        LaunchedEffect(isExpended) {
            navController.enableOnBackPressed(isExpended)
        }

        ModalBottomSheetLayout(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(this),
            sheetState = sheetState,
            sheetBackgroundColor = MaterialTheme.colors.background,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetContent = { secondContent() },
            content = { mainContent() }
        )

        BackHandler(enabled = isExpended) {
            hide()
        }
    }

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

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        println("destination.navigatorName: ${destination.route}")
    }
}