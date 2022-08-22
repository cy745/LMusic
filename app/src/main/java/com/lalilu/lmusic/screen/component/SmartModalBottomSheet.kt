package com.lalilu.lmusic.screen.component

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lalilu.lmusic.utils.extension.BackHandlerWithNavigator
import com.lalilu.lmusic.utils.extension.watchForOffset
import com.lalilu.lmusic.utils.getActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
object SmartModalBottomSheet {
    private var scope: CoroutineScope? = null
    private val scaffoldState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val offset: State<Float>
        get() = scaffoldState.offset
    val offsetPercent: Float
        get() = scaffoldState.progress.watchForOffset(
            ModalBottomSheetValue.HalfExpanded,
            ModalBottomSheetValue.Hidden
        )

    fun hide() = scope?.launch { scaffoldState.hide() }
    fun show() = scope?.launch { scaffoldState.show() }
    fun expend() = scope?.launch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    fun collapse() = scope?.launch { scaffoldState.animateTo(ModalBottomSheetValue.HalfExpanded) }

    @Composable
    fun SmartModalBottomSheetContent(
        context: Context,
        navController: NavController,
        scope: CoroutineScope = rememberCoroutineScope(),
        sheetContent: @Composable ColumnScope.() -> Unit,
        content: @Composable () -> Unit
    ) {
        this.scope = scope
        val offset = scaffoldState.offset.value
        val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()
        val screenHeight = LocalDensity.current.run { screenHeightDp.toPx() }
        val isVisible = remember(offset, screenHeight) { offset < screenHeight }

        ModalBottomSheetLayout(
            sheetState = scaffoldState,
            modifier = Modifier.fillMaxWidth(),
            sheetBackgroundColor = MaterialTheme.colors.background,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
            sheetContent = { sheetContent() },
            content = content
        )

        BackHandlerWithNavigator(
            navController = navController,
            onBack = {
                if (isVisible) {
                    hide()
                } else {
                    context.getActivity()?.moveTaskToBack(false)
                }
            }
        )
    }
}