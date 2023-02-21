package com.lalilu.lmusic.compose.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmusic.utils.extension.LocalWindowSize

object SmartContainer {

    @Composable
    @JvmName("SmartContainerLazyColumn")
    fun LazyColumn(
        modifier: Modifier = Modifier,
        reverseLayout: Boolean = false,
        state: LazyListState = rememberLazyListState(),
        verticalArrangement: Arrangement.Vertical =
            if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
        horizontalAlignment: Alignment.Horizontal = Alignment.Start,
        flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
        contentPaddingForHorizontal: Dp = 0.dp,
        userScrollEnabled: Boolean = true,
        content: LazyListScope.() -> Unit
    ) {
        val statusPaddingValues = rememberStatusBarContentPadding(contentPaddingForHorizontal)

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            state = state,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            contentPadding = statusPaddingValues
        ) {
            content()
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SmartBar.smartBarHeightDpState.value)
                )
            }
        }
    }

    @Composable
    @JvmName("SmartContainerLazyVerticalGrid")
    fun LazyVerticalGrid(
        columns: (WindowWidthSizeClass) -> Int,
        modifier: Modifier = Modifier,
        state: LazyGridState = rememberLazyGridState(),
        reverseLayout: Boolean = false,
        verticalArrangement: Arrangement.Vertical =
            if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
        contentPaddingForHorizontal: Dp = 0.dp,
        userScrollEnabled: Boolean = true,
        content: LazyGridScope.() -> Unit
    ) {
        val windowSize = LocalWindowSize.current
        val statusPaddingValues = rememberStatusBarContentPadding(contentPaddingForHorizontal)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns(windowSize.widthSizeClass)),
            modifier = modifier
                .fillMaxSize(),
            state = state,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            contentPadding = statusPaddingValues
        ) {
            content()
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SmartBar.smartBarHeightDpState.value)
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    @JvmName("SmartContainerLazyStaggeredVerticalGrid")
    fun LazyStaggeredVerticalGrid(
        columns: (WindowWidthSizeClass) -> Int,
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
        contentPaddingForHorizontal: Dp = 0.dp,
        content: LazyStaggeredGridScope.() -> Unit
    ) {
        val windowSize = LocalWindowSize.current
        val contentPadding = rememberStatusBarContentPadding(
            horizontalPadding = contentPaddingForHorizontal,
            bottomPadding = SmartBar.smartBarHeightDpState.value
        )

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columns(windowSize.widthSizeClass)),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            content = content
        )
    }

    @Composable
    fun rememberStatusBarContentPadding(
        horizontalPadding: Dp = 0.dp,
        bottomPadding: Dp = 0.dp
    ): PaddingValues {
        val density = LocalDensity.current
        val context = LocalContext.current
        val statusBarHeight = remember {
            density.run { SystemUiUtil.getFixedStatusHeight(context).toDp() }
        }
        return remember(bottomPadding) {
            PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = statusBarHeight,
                bottom = bottomPadding
            )
        }
    }
}