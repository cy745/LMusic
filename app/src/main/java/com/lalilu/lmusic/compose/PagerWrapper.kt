package com.lalilu.lmusic.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * 由于Compose的HorizontalPager实在过于拉胯，于是使用AndroidView封装了一个简易ViewPager
 */
object PagerWrapper {
    private val pagerView = mutableStateOf<ViewPager?>(null)
    val nestedScrollProp: NestedScrollConnection = MyNestedScrollConnection { pagerView.value }

    @Composable
    fun Content(
        mainContent: @Composable () -> Unit,
        secondContent: @Composable () -> Unit,
    ) {
        val adapter = remember { MyPagerAdapter(mainContent, secondContent) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ViewPager(it).also(pagerView.component2()) }
        ) {
            if (it.adapter !== adapter) {
                it.adapter = adapter
            }
        }
    }

    private class MyNestedScrollConnection(
        private val pager: () -> ViewPager?,
    ) : NestedScrollConnection {
        private var isStarted = false

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.x < 0 && isStarted) {
                pager()?.fakeDragBy(available.x)
                return available
            }
            return super.onPreScroll(available, source)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (available.x > 0 && !isStarted) {
                isStarted = true
                pager()?.apply {
                    if (isFakeDragging) endFakeDrag()
                    beginFakeDrag()
                }
            }

            if (available.x > 0 && isStarted) {
                pager()?.fakeDragBy(available.x)
                return available
            }
            return super.onPostScroll(consumed, available, source)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            isStarted = false
            pager()?.apply {
                if (isFakeDragging) endFakeDrag()
            }
            return super.onPostFling(consumed, available)
        }
    }

    private class MyPagerAdapter(
        private val mainContent: @Composable () -> Unit,
        private val secondContent: @Composable () -> Unit,
    ) : PagerAdapter() {
        override fun getCount(): Int = 2

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return ComposeView(container.context)
                .also(container::addView)
                .apply { setContent(if (position == 0) mainContent else secondContent) }
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            (obj as? View)?.also(container::removeView)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }
    }
}