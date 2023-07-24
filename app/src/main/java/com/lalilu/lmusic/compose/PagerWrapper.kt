package com.lalilu.lmusic.compose

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lalilu.lmusic.utils.AccumulatedValue
import kotlin.math.abs

/**
 * 由于Compose的HorizontalPager实在过于拉胯，于是使用AndroidView封装了一个简易ViewPager
 */
object PagerWrapper {
    private val nestedScrollConn = mutableStateOf<NestedScrollConnection?>(null)

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

    fun Modifier.nestedScrollForPager() = composed {
        val nestedScrollProp = nestedScrollConn.value
            ?: rememberNestedScrollInteropConnection()

        this.nestedScroll(connection = nestedScrollProp)
    }

    private class MyViewPager @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : ViewPager(context, attrs), NestedScrollConnection {
        private var accumulatedValueForPre = AccumulatedValue()
        private var accumulatedValue = AccumulatedValue()

        private var arrivedBound = false
        private var boundOffset = Int.MIN_VALUE
        private var canceledByUser = false
        private val overScroller by lazy { OverScroller(context) }

        private var animator = springAnimationOf(
            getter = { scrollX.toFloat() },
            setter = { scrollTo(it.toInt(), 0) }
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }.apply {
            addEndListener { _, canceled, _, _ ->
                if (!canceled) snapIfNeeded()
            }
        }

        fun snapIfNeeded() {
            val targetOffset = if (scrollX <= boundOffset * 2f / 3f) 0 else boundOffset

            if (scrollX != targetOffset) {
                animator.animateToFinalPosition(targetOffset.toFloat())
            }
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> if (animator.isRunning) {
                    animator.cancel()
                    canceledByUser = true
                }

                MotionEvent.ACTION_MOVE -> if (canceledByUser) {
                    canceledByUser = false
                }

                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_POINTER_UP,
                -> if (canceledByUser) {
                    snapIfNeeded()
                    canceledByUser = false
                }
            }
            return super.onInterceptTouchEvent(ev)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            boundOffset = width
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val intercept = boundOffset != Int.MIN_VALUE
                    && scrollX < boundOffset
                    && available.x < 0

            if (intercept) {
                val deltaX = accumulatedValueForPre.ifAccumulate(available.x)

                return if (scrollX - deltaX >= boundOffset) {
                    scrollTo(boundOffset, 0)
                    available.copy(x = available.x - (boundOffset - scrollX))
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
            if (available.x > 0 && !arrivedBound) {
                arrivedBound = true

                if (boundOffset == Int.MIN_VALUE) {
                    boundOffset = scrollX
                }
            }

            // 若是Fling至边缘，则在到达边缘的时候返回Offset.Zero，此时后续剩余的Velocity速度将不再进行消费
            if (arrivedBound && source == NestedScrollSource.Fling) {
                return Offset.Zero
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

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            arrivedBound = false

            if (abs(available.x) > 0.5f) {
                overScroller.fling(scrollX, 0, -available.x.toInt(), 0, 0, boundOffset, 0, 0)
                animator.animateToFinalPosition(overScroller.finalX.toFloat())
            } else {
                snapIfNeeded()
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