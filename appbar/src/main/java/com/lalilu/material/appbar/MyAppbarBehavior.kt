package com.lalilu.material.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec
import android.widget.ListView
import android.widget.ScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.view.children
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.lalilu.material.appbar.AppBarLayout.LayoutParams.*
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.roundToInt

class MyAppbarBehavior(
    context: Context?, attrs: AttributeSet?
) : ExpendHeaderBehavior<AppBarLayout>(context, attrs) {
    val offsetDelta: Int = 0

    private var savedState: AppBarLayout.BaseBehavior.SavedState? = null
    private val onDragCallback: AppBarLayout.BaseBehavior.BaseDragCallback<AppBarLayout>? = null

    @NestedScrollType
    private var lastStartedType = 0
    private var lastNestedScrollingChildRef: WeakReference<View>? = null


    override fun getMaxDragOffset(view: AppBarLayout): Int {
        return -view.downNestedScrollRange
    }

    override fun getScrollRangeForDragFling(view: AppBarLayout): Int {
        return view.totalScrollRange
    }

    override fun getTopBottomOffsetForScrollingSibling(): Int {
        return topAndBottomOffset
    }

    private fun getDownOffset(parent: CoordinatorLayout, child: AppBarLayout): Int {
        return parent.height / 3
    }

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        // Return true if we're nested scrolling vertically, and we either have lift on scroll enabled
        // or we can scroll the children.
        // 决定何时开始接受子View的滑动事件
        val started =
            (axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 && (
                    child.isLiftOnScroll || canScrollChildren(
                        parent,
                        child,
                        directTargetChild
                    )))

        // 如果决定接受则停止当前动画
        if (started) mSpringAnimation?.cancel()

        // A new nested scroll has started so clear out the previous ref
        // 清除子View的引用
        lastNestedScrollingChildRef = null

        // Track the last started type so we know if a fling is about to happen once scrolling ends
        lastStartedType = type
        return started
    }

    override fun onNestedPreScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // 只处理向上滚动的情况
        if (dy > 0) {
            consumed[1] = scroll(
                parent, child, dy,
                -child.upNestedPreScrollRange,
                getDownOffset(parent, child)
            )
        }
    }

    /**
     * 当target滚动到达边缘且不再可被拖动的时候将调用此方法
     */
    override fun onNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        // 处理向下滚动的情况
        if (dyUnconsumed < 0) {
            // If the scrolling view is scrolling down but not consuming, it's probably be at
            // the top of it's content
            consumed[1] = scroll(
                parent, child, dyUnconsumed,
                -child.downNestedScrollRange,
                getDownOffset(parent, child)
            )
        }
    }

    override fun onStopNestedScroll(
        parent: CoordinatorLayout, child: AppBarLayout, target: View, type: Int
    ) {
        // onStartNestedScroll for a fling will happen before onStopNestedScroll for the scroll. This
        // isn't necessarily guaranteed yet, but it should be in the future. We use this to our
        // advantage to check if a fling (ViewCompat.TYPE_NON_TOUCH) will start after the touch scroll
        // (ViewCompat.TYPE_TOUCH) ends
        if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
            // If we haven't been flung, or a fling is ending
            snapToChildIfNeeded(parent, child)
        }

        // Keep a reference to the previous nested scrolling child
        lastNestedScrollingChildRef = WeakReference(target)
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        if (lp.height == WRAP_CONTENT) {
            // If the view is set to wrap on it's height, CoordinatorLayout by default will
            // cap the view at the CoL's height. Since the AppBarLayout can scroll, this isn't
            // what we actually want, so we measure it ourselves with an unspecified spec to
            // allow the child to be larger than it's parent
            parent.onMeasureChild(
                child,
                parentWidthMeasureSpec,
                widthUsed,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                heightUsed
            )
            return true
        }

        // Let the parent handle it as normal
        return super.onMeasureChild(
            parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed
        )
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        layoutDirection: Int
    ): Boolean {
        val handled = super.onLayoutChild(parent, child, layoutDirection)

        // The priority for actions here is (first which is true wins):
        // 1. forced pending actions
        // 2. offsets for restorations
        // 3. non-forced pending actions
        val pendingAction: Int = child.pendingAction
        if (savedState != null && pendingAction and AppBarLayout.PENDING_ACTION_FORCE == 0) {
            savedState!!.let {
                when {
                    it.fullyExpanded -> {
                        setHeaderTopBottomOffset(parent, child, -child.totalScrollRange)
                    }
                    it.fullyScrolled -> {
                        setHeaderTopBottomOffset(parent, child, 0)
                    }
                    else -> {
                        val target: View = child.getChildAt(it.firstVisibleChildIndex)
                        var offset = -target.bottom
                        offset += if (it.firstVisibleChildAtMinimumHeight) {
                            ViewCompat.getMinimumHeight(target) + child.topInset
                        } else {
                            (target.height * it.firstVisibleChildPercentageShown).roundToInt()
                        }
                        setHeaderTopBottomOffset(parent, child, offset)
                    }
                }
            }
        } else if (pendingAction != AppBarLayout.PENDING_ACTION_NONE) {
            val animate = pendingAction and AppBarLayout.PENDING_ACTION_ANIMATE_ENABLED != 0
            if (pendingAction and AppBarLayout.PENDING_ACTION_COLLAPSED != 0) {
                val offset: Int = -child.upNestedPreScrollRange
                if (animate) {
                    animateOffsetTo(parent, child, offset)
                } else {
                    setHeaderTopBottomOffset(parent, child, offset)
                }
            } else if (pendingAction and AppBarLayout.PENDING_ACTION_EXPANDED != 0) {
                if (animate) {
                    animateOffsetTo(parent, child, 0)
                } else {
                    setHeaderTopBottomOffset(parent, child, 0)
                }
            }
        }

        // Finally reset any pending states
        child.resetPendingAction()
        savedState = null

        // We may have changed size, so let's constrain the top and bottom offset correctly,
        // just in case we're out of the bounds
        topAndBottomOffset =
            MathUtils.clamp(topAndBottomOffset, -child.totalScrollRange, 0)


        // Update the AppBarLayout's drawable state for any elevation changes. This is needed so that
        // the elevation is set in the first layout, so that we don't get a visual jump pre-N (due to
        // the draw dispatch skip)
        updateAppBarLayoutDrawableState(
            parent, child, topAndBottomOffset, 0 /* direction */, true /* forceJump */
        )

        // Make sure we dispatch the offset update
        child.onOffsetChanged(topAndBottomOffset)
        return handled
    }

    private fun snapToChildIfNeeded(coordinatorLayout: CoordinatorLayout, abl: AppBarLayout) {
        val topInset: Int = abl.topInset + abl.paddingTop
        // The "baseline" of scrolling is the top of the first child. We "add" insets and paddings
        // to the scrolling amount to align offsets and views with the same y-coordinate. (The origin
        // is at the top of the AppBarLayout, so all the coordinates are with negative values.)
        val offset = getTopBottomOffsetForScrollingSibling() - topInset
        val offsetChildIndex: Int = getChildIndexOnOffset(abl, offset)

        if (offsetChildIndex >= 0) {
            val offsetChild: View = abl.getChildAt(offsetChildIndex)
            val lp = offsetChild.layoutParams as AppBarLayout.LayoutParams
            val flags = lp.getScrollFlags()
            if (checkFlag(flags, SCROLL_FLAG_SNAP)) {
                // We're set the snap, so animate the offset to the nearest edge
                var snapTop = -offsetChild.top
                var snapBottom = -offsetChild.bottom

                // If the child is set to fit system windows, its top will include the inset area, we need
                // to minus the inset from snapTop to make the calculation consistent.
                if (offsetChildIndex == 0 && ViewCompat.getFitsSystemWindows(abl)
                    && ViewCompat.getFitsSystemWindows(offsetChild)
                ) {
                    snapTop -= abl.topInset
                }
                if (checkFlag(flags, SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)) {
                    // If the view is set only exit until it is collapsed, we'll abide by that
                    snapBottom += ViewCompat.getMinimumHeight(offsetChild)
                } else if (checkFlag(flags, FLAG_QUICK_RETURN or SCROLL_FLAG_ENTER_ALWAYS)) {
                    // If it's set to always enter collapsed, it actually has two states. We
                    // select the state and then snap within the state
                    val seam = snapBottom + ViewCompat.getMinimumHeight(offsetChild)
                    if (offset < seam) {
                        snapTop = seam
                    } else {
                        snapBottom = seam
                    }
                }
                if (checkFlag(flags, SCROLL_FLAG_SNAP_MARGINS)) {
                    // Update snap destinations to include margins
                    snapTop += lp.topMargin
                    snapBottom -= lp.bottomMargin
                }

                // Excludes insets and paddings from the offset. (Offsets use the top of child views as
                // the origin.)
                val newOffset: Int = calculateSnapOffset(offset, snapBottom, snapTop) + topInset
                animateOffsetTo(
                    coordinatorLayout, abl,
                    MathUtils.clamp(newOffset, -abl.totalScrollRange, 0)
                )
            }
        }
    }

    override fun setHeaderTopBottomOffset(
        parent: CoordinatorLayout,
        header: AppBarLayout,
        newOffset: Int,
        minOffset: Int,
        maxOffset: Int
    ): Int {
        val curOffset = getTopBottomOffsetForScrollingSibling()
        var consumed = 0
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            val offset = MathUtils.clamp(newOffset, minOffset, maxOffset)
            if (curOffset != offset) {
                val offsetChanged = setTopAndBottomOffset(offset)

                // Update how much dy we have consumed
                consumed = curOffset - offset

                if (offsetChanged) {
                    // If the offset has changed, pass the change to any child scroll effect.
                    for (i in 0 until header.childCount) {
                        val params =
                            header.getChildAt(i).layoutParams as AppBarLayout.LayoutParams
                        val scrollEffect = params.scrollEffect
                        if (scrollEffect != null
                            && params.getScrollFlags() and SCROLL_FLAG_SCROLL != 0
                        ) {
                            scrollEffect.onOffsetChanged(
                                header,
                                header.getChildAt(i),
                                topAndBottomOffset.toFloat()
                            )
                        }
                    }
                }
                if (!offsetChanged && header.hasChildWithInterpolator()) {
                    // If the offset hasn't changed and we're using an interpolated scroll
                    // then we need to keep any dependent views updated. CoL will do this for
                    // us when we move, but we need to do it manually when we don't (as an
                    // interpolated scroll may finish early).
                    parent.dispatchDependentViewsChanged(header)
                }

                // Dispatch the updates to any listeners
                header.onOffsetChanged(topAndBottomOffset)

                // Update the AppBarLayout's drawable state (for any elevation changes)
                updateAppBarLayoutDrawableState(
                    parent,
                    header,
                    offset,
                    if (offset < curOffset) -1 else 1,
                    false /* forceJump */
                )
            }
        }
        return consumed
    }

    override fun canDragView(view: AppBarLayout): Boolean {
        if (onDragCallback != null) {
            // If there is a drag callback set, it's in control
            return onDragCallback.canDrag(view)
        }

        // Else we'll use the default behaviour of seeing if it can scroll down
        return if (lastNestedScrollingChildRef != null) {
            // If we have a reference to a scrolling view, check it
            val scrollingView = lastNestedScrollingChildRef!!.get()
            (scrollingView != null && scrollingView.isShown
                    && !scrollingView.canScrollVertically(-1))
        } else {
            // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
            true
        }
    }

    private fun updateAppBarLayoutDrawableState(
        parent: CoordinatorLayout,
        layout: AppBarLayout,
        offset: Int,
        direction: Int,
        forceJump: Boolean
    ) {
        val child = getAppBarChildOnOffset(layout, offset)
        var lifted = false
        if (child != null) {
            val childLp = child.layoutParams as AppBarLayout.LayoutParams
            val flags = childLp.getScrollFlags()
            if (flags and SCROLL_FLAG_SCROLL != 0) {
                val minHeight = ViewCompat.getMinimumHeight(child)
                lifted = -offset >= child.bottom - minHeight - layout.topInset
            }
        }
        val changed: Boolean = layout.setLiftedState(lifted)
        if (forceJump || changed && shouldJumpElevationState(parent, layout)) {
            // If the collapsed state changed, we may need to
            // jump to the current state if we have an overlapping view
            layout.jumpDrawablesToCurrentState()
        }
    }

    private fun shouldJumpElevationState(parent: CoordinatorLayout, layout: AppBarLayout): Boolean {
        // We should jump the elevated state if we have a dependent scrolling view which has
        // an overlapping top (i.e. overlaps us)
        val dependencies = parent.getDependents(layout)
        for (i in 0 until dependencies.size) {
            val dependency = dependencies[i]
            val lp = dependency.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = lp.behavior
            if (behavior is AppBarLayout.ScrollingViewBehavior) {
                return behavior.overlayTop != 0
            }
        }
        return false
    }

    override fun onFlingFinished(parent: CoordinatorLayout, layout: AppBarLayout) {
        // At the end of a manual fling, check to see if we need to snap to the edge-child
        snapToChildIfNeeded(parent, layout)
    }

    private fun animateOffsetTo(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        offset: Int
    ) {
        mSpringAnimation = mSpringAnimation
            ?: SpringAnimation(
                this, HeaderOffsetFloatProperty(parent, child),
                getTopBottomOffsetForScrollingSibling().toFloat()
            ).also {
                it.spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                it.spring.stiffness = SpringForce.STIFFNESS_LOW
            }
        mSpringAnimation?.cancel()
        mSpringAnimation?.animateToFinalPosition(offset.toFloat())
    }


    private fun getAppBarChildOnOffset(
        layout: AppBarLayout, offset: Int
    ): View? {
        val absOffset = abs(offset)
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (absOffset in child.top..child.bottom) return child
        }
        return null
    }

    private fun findFirstScrollingChild(parent: CoordinatorLayout): View? {
        parent.children.forEach {
            when (it) {
                is NestedScrollingChild,
                is ListView,
                is ScrollView -> return it
            }
        }
        return null
    }

    private fun getChildIndexOnOffset(abl: AppBarLayout, offset: Int): Int {
        for (i in 0 until abl.childCount) {
            val child: View = abl.getChildAt(i)
            val lp = child.layoutParams as AppBarLayout.LayoutParams

            var top = child.top
            var bottom = child.bottom
            if (checkFlag(
                    lp.getScrollFlags(),
                    SCROLL_FLAG_SNAP_MARGINS
                )
            ) {
                // Update top and bottom to include margins
                top -= lp.topMargin
                bottom += lp.bottomMargin
            }
            if (-offset in top..bottom) return i
        }
        return -1
    }

    private fun calculateSnapOffset(value: Int, bottom: Int, top: Int): Int {
        return if (value < (bottom + top) / 2) bottom else top
    }

    private fun checkFlag(flags: Int, check: Int): Boolean {
        return flags and check == check
    }

    private fun canScrollChildren(
        parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View
    ): Boolean {
        return child.hasScrollableChildren() && parent.height - directTargetChild.height <= child.height
    }
}