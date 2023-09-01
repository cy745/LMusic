package com.lalilu.lmusic.compose

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.SpringSpec
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.recomposeHighlighter
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
            animationSpec = SpringSpec(stiffness = 1000f),
            isSkipHalfExpanded = true,
        )
    }

    fun show() = scope?.launch { sheetState.show() }
    fun hide() = scope?.launch { sheetState.hide() }

    @Composable
    fun BackHandler(
        forVisible: () -> Boolean = { false },
        enable: () -> Boolean = { true },
        callback: () -> Unit,
    ) {
        BackHandler(
            enabled = sheetState.isVisible == forVisible() && enable(),
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

        LaunchedEffect(sheetState.isVisible) {
            navController.enableOnBackPressed(sheetState.isVisible)
        }

        ModalBottomSheetLayout(
            modifier = Modifier
                .recomposeHighlighter()
                .nestedScroll(this),
            sheetBackgroundColor = MaterialTheme.colors.background,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetState = sheetState,
            sheetContent = { secondContent() }
        ) {
            mainContent()
        }

        BackHandler(enabled = sheetState.isVisible) {
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