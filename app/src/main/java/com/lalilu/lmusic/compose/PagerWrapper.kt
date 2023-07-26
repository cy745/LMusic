package com.lalilu.lmusic.compose

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
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
    val LocalPagerPosition = compositionLocalOf { -1 }

    private val nestedScrollConn = mutableStateOf<NestedScrollConnection?>(null)
    private val currentPage = mutableStateOf(0)
    private val pagerExist = mutableStateOf(false)

    var animateToPage: (Int) -> Unit = {}
        private set

    @Composable
    fun Content(
        mainContent: @Composable () -> Unit,
        secondContent: @Composable () -> Unit,
    ) {
        val adapter = remember { MyPagerAdapter(mainContent, secondContent) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MyViewPager(context)
                    .also(nestedScrollConn.component2())
                    .apply { animateToPage = this::animateToPage }
                    .onPageSelected { currentPage.value = it }
            }
        ) { pager ->
            if (pager.adapter !== adapter) {
                pager.adapter = adapter
            }
        }

        BackHandlerForPager(forPage = 1) {
            animateToPage.invoke(0)
        }

        SideEffect {
            pagerExist.value = true
        }

        DisposableEffect(Unit) {
            onDispose {
                pagerExist.value = false
                animateToPage = {}
            }
        }
    }

    @Composable
    fun rememberIsCurrentPage(
        forPage: Int = LocalPagerPosition.current,
    ): State<Boolean> {
        return remember { derivedStateOf { pagerExist.value && currentPage.value == forPage } }
    }

    @Composable
    fun OnPagerChangeHandler(
        forPage: Int = LocalPagerPosition.current,
        enable: () -> Boolean = { true },
        onPagerChange: suspend (isCurrentPage: Boolean) -> Unit = {},
    ) {
        val enabled = remember(pagerExist.value, enable()) {
            derivedStateOf { pagerExist.value && enable() }
        }

        LaunchedEffect(enabled.value, currentPage.value) {
            if (!enabled.value) return@LaunchedEffect

            onPagerChange(currentPage.value == forPage)
        }
    }

    @Composable
    fun BackHandlerForPager(
        forPage: Int = LocalPagerPosition.current,
        enable: () -> Boolean = { true },
        callback: () -> Unit,
    ) {
        BackHandler(
            enabled = currentPage.value == forPage && pagerExist.value && enable(),
            onBack = callback
        )
    }

    fun Modifier.nestedScrollForPager() = composed {
        val nestedScrollProp =
            remember(pagerExist.value) { nestedScrollConn.value?.takeIf { pagerExist.value } }
                ?: rememberNestedScrollInteropConnection()

        this.nestedScroll(connection = nestedScrollProp)
    }

    private class MyViewPager @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : ViewPager(context, attrs), NestedScrollConnection {

        private var arrivedBound = false
        private var boundOffset = Int.MIN_VALUE
        private var canceledByUser = false
        private var onPageSelectedCallback: (position: Int) -> Unit = {}

        private val accumulatedValueForPre = AccumulatedValue()
        private val accumulatedValue = AccumulatedValue()
        private val overScroller by lazy { OverScroller(context) }

        private val animator by lazy {
            springAnimationOf(
                getter = { scrollX.toFloat() },
                setter = { scrollTo(it.toInt(), 0) }
            ).withSpringForceProperties {
                dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }.apply {
                addEndListener { _, canceled, value, _ ->
                    if (!canceled) {
                        snapIfNeeded()

                        val closeToLeft = abs(value - 0f) < 1f
                        val closeToRight = abs(value - boundOffset) < 1f
                        if (closeToLeft || closeToRight) {
                            currentItem = if (closeToLeft) 0 else 1
                        }
                    }
                }
            }
        }

        init {
            addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {
                    onPageSelectedCallback(position)
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })
        }

        fun onPageSelected(action: (position: Int) -> Unit) = apply {
            onPageSelectedCallback = action
        }

        fun snapIfNeeded() {
            val targetOffset = if (scrollX <= boundOffset * 2f / 3f) 0 else boundOffset

            if (scrollX != targetOffset) {
                animator.animateToFinalPosition(targetOffset.toFloat())
            }
        }

        fun animateToPage(position: Int) {
            val targetOffset = position * boundOffset

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
                .apply {
                    setContent {
                        CompositionLocalProvider(LocalPagerPosition provides position) {
                            if (position == 0) {
                                mainContent()
                            } else {
                                secondContent()
                            }
                        }
                    }
                }
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            (obj as? View)?.also(container::removeView)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }
    }
}