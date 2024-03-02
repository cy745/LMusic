package com.lalilu.component.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

class BottomSheetNestedScrollInterceptor : NestedScrollConnection {
    private var arrivedBoundarySource: NestedScrollSource? = null

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // 重置到达边界时的状态
        if (source == NestedScrollSource.Drag && arrivedBoundarySource == NestedScrollSource.Fling) {
            arrivedBoundarySource = null
        }

        return super.onPreScroll(available, source)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // 子布局无法消费完即到达边界
        if (arrivedBoundarySource == null && abs(available.y) > 0) {
            arrivedBoundarySource = source
        }

        // 根据到达边界时的子布局消费情况决定是否消费
        if (arrivedBoundarySource == NestedScrollSource.Fling) {
            return available
        }

        return Offset.Zero
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {
        arrivedBoundarySource = null
        return super.onPostFling(consumed, available)
    }
}

@Composable
fun rememberBottomSheetNestedScrollInterceptor(): BottomSheetNestedScrollInterceptor {
    return remember { BottomSheetNestedScrollInterceptor() }
}