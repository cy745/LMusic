package com.lalilu.lmusic.utils.extension

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

/**
 * 在布局元素时获取其测量好的宽高并通过callback返回给调用方
 */
fun Modifier.measure(callback: (width: Int, height: Int) -> Unit): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        callback.invoke(placeable.measuredWidth, placeable.measuredHeight)
        layout(constraints.maxWidth, placeable.measuredHeight) {
            placeable.place(0, 0)
        }
    }