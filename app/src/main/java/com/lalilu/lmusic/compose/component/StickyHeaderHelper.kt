package com.lalilu.lmusic.compose.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex


class StickyHelper(
    val headerKeyFirst: State<Any?>,
    val headerKeySecond: State<Any?>,
    val headerOffsetFirst: State<Int>,
    val headerOffsetSecond: State<Int>,
    val contentType: () -> Any
)

abstract class ExtentLazyItemScope(
    private val key: () -> Any,
    private val helper: StickyHelper,
    private val scope: LazyItemScope
) : LazyItemScope by scope {

    fun Modifier.offsetWithHelper() = this.offset {
        IntOffset(
            x = 0,
            y = when (key()) {
                helper.headerKeyFirst.value -> helper.headerOffsetFirst.value
                helper.headerKeySecond.value -> helper.headerOffsetSecond.value
                else -> 0
            }
        )
    }

    fun Modifier.zIndexWithHelper() = this.zIndex(
        when (key()) {
            helper.headerKeyFirst.value -> 1f
            helper.headerKeySecond.value -> 2f
            else -> 0f
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.stickyHeaderExtent(
    key: () -> Any,
    helper: StickyHelper,
    headerContent: @Composable ExtentLazyItemScope.() -> Unit
) {
    stickyHeader(
        key = key(),
        contentType = helper.contentType()
    ) {
        val scope = object : ExtentLazyItemScope(key = key, helper = helper, scope = this) {}
        scope.headerContent()
    }
}

@Composable
fun rememberStickyHelper(
    listState: LazyListState,
    headerMinOffset: () -> Int = { 0 },
    contentType: () -> Any,
): StickyHelper {
    val minOffset by remember { derivedStateOf { headerMinOffset() } }

    val headerFirst by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.contentType == contentType() }
        }
    }
    val headerSecond by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .filter { it.contentType == contentType() }
                .getOrNull(1)
        }
    }

    val headerKeyFirst = remember { derivedStateOf { headerFirst?.key } }
    val headerKeySecond = remember { derivedStateOf { headerSecond?.key } }

    val headerOffsetSecond = remember(minOffset) {
        derivedStateOf {
            val offset = headerSecond?.offset
            if (offset == null || offset > minOffset) 0 else minOffset - offset
        }
    }

    val headerOffsetFirst = remember(minOffset) {
        derivedStateOf {
            val offset = headerFirst?.offset
            if (headerOffsetSecond.value > 0) return@derivedStateOf Int.MAX_VALUE
            if (offset == null || offset > minOffset) 0 else minOffset - offset
        }
    }

    return remember {
        StickyHelper(
            headerKeyFirst = headerKeyFirst,
            headerKeySecond = headerKeySecond,
            headerOffsetFirst = headerOffsetFirst,
            headerOffsetSecond = headerOffsetSecond,
            contentType = contentType
        )
    }
}