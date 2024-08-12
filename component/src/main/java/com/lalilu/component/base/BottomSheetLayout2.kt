package com.lalilu.component.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetLayout2(
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = 56.dp,
    sheetContent: @Composable (EnhanceSheetState) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val enhanceSheetState = remember(scaffoldState.bottomSheetState) {
        EnhanceBottomSheetState(
            bottomSheetState = scaffoldState.bottomSheetState,
            scope = scope
        )
    }

    CompositionLocalProvider(LocalEnhanceSheetState provides enhanceSheetState) {
        BottomSheetScaffold(
            modifier = modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetElevation = 0.dp,
            sheetBackgroundColor = Color.Transparent,
            sheetPeekHeight = sheetPeekHeight,
            sheetContent = {
                BackHandler(enabled = enhanceSheetState.isVisible) {
                    enhanceSheetState.hide()
                }
                sheetContent(enhanceSheetState)
            },
            content = content
        )
    }
}