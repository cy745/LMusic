package com.lalilu.lmusic.compose.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.utils.recomposeHighlighter

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
                .fillMaxSize()
                .recomposeHighlighter(),
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
        columns: GridCells = GridCells.Fixed(2),
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
        val statusPaddingValues = rememberStatusBarContentPadding(contentPaddingForHorizontal)

        LazyVerticalGrid(
            columns = columns,
            modifier = modifier
                .fillMaxSize()
                .recomposeHighlighter(),
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

    @Composable
    @JvmName("SmartContainerLazyStaggeredVerticalGrid")
    fun StaggeredVerticalGrid(
        columns: Int,
        contentPaddingForHorizontal: Dp = 0.dp,
        content: @Composable () -> Unit
    ) {
        val contentPadding = rememberStatusBarContentPadding(
            horizontalPadding = contentPaddingForHorizontal,
            bottomPadding = SmartBar.smartBarHeightDpState.value
        )

        com.lalilu.lmusic.compose.component.base.StaggeredVerticalGrid(
            columns = columns,
            content = content,
            contentPadding = contentPadding
        )
    }

    @Composable
    fun rememberStatusBarContentPadding(
        horizontalPadding: Dp = 0.dp,
        bottomPadding: Dp = 0.dp
    ): PaddingValues {
        val density = LocalDensity.current
        val statusBarsInsets = WindowInsets.statusBars
        return remember(statusBarsInsets, bottomPadding) {
            PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = density.run { statusBarsInsets.getTop(density).toDp() },
                bottom = bottomPadding
            )
        }
    }
}