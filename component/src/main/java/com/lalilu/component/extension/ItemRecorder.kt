package com.lalilu.component.extension

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

class LazyListRecordScope internal constructor(
    var recorder: ItemRecorder,
) {
    var lazyListScope: LazyListScope? = null
        internal set

    fun itemWithRecord(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable LazyItemScope.() -> Unit
    ) {
        lazyListScope?.let { scope ->
            recorder.record(key)
            scope.item(
                key = key,
                contentType = contentType,
                content = content
            )
        }
    }

    inline fun <T : Any> itemsWithRecord(
        items: List<T>,
        noinline key: ((item: T) -> Any)? = null,
        noinline contentType: (item: T) -> Any? = { null },
        crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
    ) {
        lazyListScope?.let { scope ->
            recorder.recordAll(items.map { key?.invoke(it) })
            scope.items(
                items = items,
                key = key,
                contentType = contentType,
                itemContent = itemContent
            )
        }
    }
}

class ItemRecorder {
    private val keys = mutableListOf<Any?>()
    private val scope = LazyListRecordScope(this)

    fun record(key: Any?) = this.keys.add(key)
    fun recordAll(keys: List<Any?>) = this.keys.addAll(keys)
    fun clear() = keys.clear()
    fun list() = keys

    internal fun startRecord(
        lazyListScope: LazyListScope,
        block: LazyListRecordScope.() -> Unit
    ) {
        clear()
        scope.lazyListScope = lazyListScope
        scope.block()
    }
}

fun LazyListScope.startRecord(
    recorder: ItemRecorder,
    block: LazyListRecordScope.() -> Unit
) {
    recorder.startRecord(
        lazyListScope = this,
        block = block
    )
}