package com.lalilu.component.base.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.InternalLazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.ScrollbarSettings


@Composable
fun SongsScreenScrollBar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()

        InternalLazyColumnScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.5f),
            state = listState,
            settings = ScrollbarSettings(
                alwaysShowScrollbar = true,
                scrollbarPadding = 4.dp,
                thumbMinLength = 0.2f,
                thumbShape = RectangleShape,
                selectionMode = ScrollbarSelectionMode.Full,
                thumbUnselectedColor = MaterialTheme.colors.onBackground.copy(0.4f),
                thumbSelectedColor = MaterialTheme.colors.onBackground.copy(0.8f),
            )
        )
    }
}