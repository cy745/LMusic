package com.lalilu.lmusic.compose

import android.content.Context
import android.util.AttributeSet
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
import com.lalilu.lmusic.utils.AccumulatedValue

/**
 * 由于Compose的HorizontalPager实在过于拉胯，于是使用AndroidView封装了一个简易ViewPager
 */
object PagerWrapper {
    val nestedScrollConn = mutableStateOf<NestedScrollConnection?>(null)

    @Composable
    fun Content(
        mainContent: @Composable () -> Unit,
        secondContent: @Composable () -> Unit,
    ) {
        val adapter = remember { MyPagerAdapter(mainContent, secondContent) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { MyViewPager(it).also(nestedScrollConn.component2()) }
        ) {
            if (it.adapter !== adapter) {
                it.adapter = adapter
            }
        }
    }

    private class MyViewPager @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : ViewPager(context, attrs), NestedScrollConnection {
        private var accumulatedValueForPre = AccumulatedValue()
        private var accumulatedValue = AccumulatedValue()

        private var isTouchStarted = false
        private var startScrollX = Int.MIN_VALUE

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            startScrollX = width
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val intercept = startScrollX != Int.MIN_VALUE
                    && scrollX < startScrollX
                    && available.x < 0

            if (intercept) {
                val deltaX = accumulatedValueForPre.ifAccumulate(available.x)

                return if (scrollX - deltaX >= startScrollX) {
                    scrollTo(startScrollX, 0)
                    available.copy(x = available.x - (startScrollX - scrollX))
                } else {
                    scrollBy(-accumulatedValueForPre.accumulate(available.x), 0)
                    available
                }
            }
            return super.onPreScroll(available, source)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (available.x > 0 && !isTouchStarted && startScrollX == Int.MIN_VALUE) {
                isTouchStarted = true
                startScrollX = scrollX
            }

            if (available.x > 0) {
                var targetScrollX = scrollX - accumulatedValue.accumulate(available.x)
                if (targetScrollX <= 0) {
                    targetScrollX = 0
                }
                scrollTo(targetScrollX, 0)
                return available
            }
            return super.onPostScroll(consumed, available, source)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            isTouchStarted = false
            println("[onPreFling]: x: ${available.x}")

            return super.onPreFling(available)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            isTouchStarted = false

            setCurrentItem(currentItem, true)
            println("[onPostFling]: x: ${available.x} ${consumed.x}")

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