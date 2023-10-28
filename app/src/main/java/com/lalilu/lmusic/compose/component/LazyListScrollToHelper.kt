package com.lalilu.lmusic.compose.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LazyListScrollToHelper internal constructor(
    private val onScrollTo: (delay: Long, action: () -> Int?) -> Unit
) {
    private val keys: MutableSet<Any> = mutableSetOf()
    private var finished: Boolean = false

    fun startRecord() {
        keys.clear()
        finished = false
    }

    fun doRecord(key: Any) {
        if (finished) return
        keys.add(key)
    }

    fun doRecord(key: Collection<Any>) {
        if (finished) return
        keys.addAll(key)
    }

    fun endRecord() {
        finished = true
    }

    fun scrollToItem(
        key: Any,
        delay: Long = 0L
    ) {
        onScrollTo(delay) {
            keys.indexOf(key)
                .takeIf { it >= 0 }
        }
    }
}

@Composable
fun rememberLazyListScrollToHelper(
    listState: LazyListState
): LazyListScrollToHelper {
    val scope = rememberCoroutineScope()

    return remember {
        LazyListScrollToHelper { delayTimeMillis, action ->
            scope.launch {
                delay(delayTimeMillis)
                val index = action() ?: return@launch
                listState.scrollToItem(index = index)
            }
        }
    }
}