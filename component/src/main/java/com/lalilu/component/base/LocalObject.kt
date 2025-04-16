package com.lalilu.component.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalSmartBarPadding = compositionLocalOf { mutableStateOf(PaddingValues(0.dp)) }

val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass hasn't been initialized")
}

@Composable
private fun SmartPaddingContent() {
    val bottomHeight = LocalSmartBarPadding.current.value.calculateBottomPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
            WindowInsets.ime.asPaddingValues().calculateBottomPadding() +
            16.dp

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(bottomHeight)
    )
}

fun LazyListScope.smartBarPadding() {
    item(
        key = "smartBarPadding",
        contentType = "smartBarPadding",
        content = { SmartPaddingContent() }
    )
}

fun LazyGridScope.smartBarPadding() {
    item(
        key = "smartBarPadding",
        contentType = "smartBarPadding",
        span = { GridItemSpan(maxLineSpan) },
        content = { SmartPaddingContent() }
    )
}

fun LazyStaggeredGridScope.smartBarPadding() {
    item(
        key = "smartBarPadding",
        contentType = "smartBarPadding",
        span = StaggeredGridItemSpan.FullLine,
        content = { SmartPaddingContent() }
    )
}