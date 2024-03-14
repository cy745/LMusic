package com.lalilu.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TwoColumnWithPad(
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current,
    minWidthBreakPoint: Dp = 500.dp,
    modifierForPad: Modifier = Modifier,
    modifierForNormal: Modifier = Modifier,
    arrangementForPad: Arrangement.Vertical = Arrangement.Top,
    arrangementForNormal: Arrangement.Vertical = Arrangement.Top,
    columnForPad: LazyListScope.() -> Unit = {},
    columnForNormal: LazyListScope.(isPad: Boolean) -> Unit = {},
) {
    val paddingTop = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
        .calculateTopPadding()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isPad = with(density) { constraints.maxWidth.toDp() } > minWidthBreakPoint

        Row(modifier = Modifier.fillMaxSize()) {
            if (isPad) {
                LLazyColumn(
                    modifier = modifierForPad
                        .fillMaxHeight()
                        .wrapContentWidth(),
                    contentPadding = PaddingValues(top = paddingTop, start = 20.dp),
                    verticalArrangement = arrangementForPad,
                    content = columnForPad,
                )
            }
            LLazyColumn(
                modifier = modifierForNormal
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(top = paddingTop),
                content = { columnForNormal(isPad) },
                verticalArrangement = arrangementForNormal
            )
        }
    }
}