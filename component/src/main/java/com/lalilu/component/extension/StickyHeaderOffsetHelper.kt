package com.lalilu.component.extension

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex


@Composable
fun StickyHeaderOffsetHelper(
    modifier: Modifier = Modifier,
    key: Any,
    minOffset: Int = 0,
    listState: LazyListState,
    block: @Composable (Modifier, isFloating: Boolean) -> Unit
) {
    val zIndex = remember { mutableFloatStateOf(0f) }
    val floating = remember { mutableStateOf(false) }

    block(
        modifier
            .offset {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val index = visibleItems.indexOfFirst { it.key == key }
                val item = visibleItems.getOrNull(index)

                if (item == null) {
                    floating.value = false
                    return@offset IntOffset.Zero
                }

                val offset = item.offset
                zIndex.floatValue = index.toFloat()

                when {
                    offset > minOffset -> {
                        floating.value = false
                        IntOffset.Zero
                    }

                    else -> {
                        floating.value = true
                        IntOffset(0, minOffset - offset)
                    }
                }
            }
            .zIndex(zIndex.floatValue),
        floating.value
    )
}