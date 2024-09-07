package com.lalilu.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface LazyGridContent {

    @Composable
    fun register(): LazyGridScope.() -> Unit
}

@Composable
fun rememberGridItemPadding(
    count: Int,
    gapHorizontal: Dp,
    gapVertical: Dp = 0.dp,
    paddingValues: PaddingValues,
): (Int) -> PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return remember(count, paddingValues, gapHorizontal, gapVertical, layoutDirection) {
        val paddingStart = paddingValues.calculateStartPadding(layoutDirection)
        val paddingEnd = paddingValues.calculateStartPadding(layoutDirection)

        val averageGap =
            (paddingStart + paddingEnd + (gapHorizontal * (count - 1))) / count.toFloat()
        val resultList = mutableListOf<PaddingValues>()

        var tempPaddingStart = paddingStart
        var tempPaddingEnd = averageGap - tempPaddingStart

        for (i in 0 until count) {
            resultList.add(
                PaddingValues(
                    start = tempPaddingStart.coerceAtLeast(0.dp),
                    end = tempPaddingEnd.coerceAtLeast(0.dp)
                )
            )

            tempPaddingStart = gapHorizontal - tempPaddingEnd
            tempPaddingEnd = if (i != count - 1) averageGap - tempPaddingStart else paddingEnd
        }

        { index ->
            resultList.getOrNull(index % count)
                ?.let {
                    if (index < count) it else PaddingValues(
                        top = gapVertical,
                        start = it.calculateStartPadding(layoutDirection),
                        end = it.calculateEndPadding(layoutDirection)
                    )
                }
                ?: PaddingValues()
        }
    }
}

fun LazyGridScope.divider(block: (Modifier) -> Modifier = { it }) {
    item(
        contentType = "divider",
        span = { GridItemSpan(maxLineSpan) }
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .let(block)
        )
    }
}