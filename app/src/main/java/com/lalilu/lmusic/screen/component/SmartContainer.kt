package com.lalilu.lmusic.screen.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
        val statusBarHeight by SmartBar.statusBarHeightDpLiveData.observeAsState(0.dp)

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = state,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            contentPadding = PaddingValues(
                start = contentPaddingForHorizontal,
                end = contentPaddingForHorizontal,
                top = statusBarHeight
            )
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
        val statusBarHeight by SmartBar.statusBarHeightDpLiveData.observeAsState(0.dp)

        LazyVerticalGrid(
            columns = columns,
            modifier = modifier.fillMaxSize(),
            state = state,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            contentPadding = PaddingValues(
                start = contentPaddingForHorizontal,
                end = contentPaddingForHorizontal,
                top = statusBarHeight
            )
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
        val statusBarHeight by SmartBar.statusBarHeightDpLiveData.observeAsState(0.dp)

        StaggeredVerticalGrid(
            columns = columns,
            content = content,
            contentPadding = PaddingValues(
                start = contentPaddingForHorizontal,
                end = contentPaddingForHorizontal,
                top = statusBarHeight,
                bottom = SmartBar.smartBarHeightDpState.value
            )
        )
    }
}