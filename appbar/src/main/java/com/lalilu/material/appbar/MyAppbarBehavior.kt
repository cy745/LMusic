package com.lalilu.material.appbar

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec
import android.widget.ListView
import android.widget.ScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.view.children
import androidx.customview.view.AbsSavedState
import com.lalilu.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.lalilu.material.appbar.AppBarLayout.LayoutParams.WRAP_CONTENT
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.roundToInt

class MyAppbarBehavior(
    context: Context?, attrs: AttributeSet?
) : ExpendHeaderBehavior<AppBarLayout>(context, attrs) {
    val offsetDelta: Int = 0

    private var listeners: ArrayList<AppBarLayout.OnOffsetChangedListener> = ArrayList()
    private var savedState: SavedState? = null
    private val onDragCallback: AppBarLayout.BaseBehavior.BaseDragCallback<AppBarLayout>? = null

    @NestedScrollType
    private var lastStartedType = 0
    private var lastNestedScrollingChildRef: WeakReference<View>? = null

    fun addOnOffsetExpendChangedListener(
        listener: AppBarLayout.OnOffsetChangedListener
    ) {
        listeners.add(listener)
    }

    private fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        listeners.forEach {
            it.onOffsetChanged(appBarLayout, offset)
        }
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
        if (started) cancelAnimation()

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
            consumed[1] = scroll(parent, child, dy)
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
            consumed[1] = scroll(parent, child, dyUnconsumed)
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
        setHeaderTopBottomOffset(parent, child, topAndBottomOffset)

        // Update the AppBarLayout's drawable state for any elevation changes. This is needed so that
        // the elevation is set in the first layout, so that we don't get a visual jump pre-N (due to
        // the draw dispatch skip)
        updateAppBarLayoutDrawableState(
            parent, child, topAndBottomOffset, 0 /* direction */, true /* forceJump */
        )

        // Make sure we dispatch the offset update
        child.onOffsetChanged(topAndBottomOffset.coerceAtMost(0))
        this.onOffsetChanged(child, topAndBottomOffset)
        return handled
    }

    override fun setHeaderTopBottomOffset(
        parent: CoordinatorLayout,
        header: AppBarLayout,
        newOffset: Int,
        minOffset: Int,
        maxOffset: Int
    ): Int {
        val curOffset = topAndBottomOffset
        var consumed = 0
        if (minOffset != 0 && curOffset in minOffset..maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            val offset = newOffset.coerceIn(minOffset, maxOffset)
            if (curOffset != offset) {
                val offsetChanged = setTopAndBottomOffset(offset)

                // Update how much dy we have consumed
                consumed = curOffset - offset

                if (offsetChanged) {
                    // If the offset has changed, pass the change to any child scroll effect.
                    for (i in 0 until header.childCount) {
                        val params = header.getChildAt(i).layoutParams as AppBarLayout.LayoutParams
                        val scrollEffect = params.scrollEffect
                        if (scrollEffect != null && params.getScrollFlags() and SCROLL_FLAG_SCROLL != 0) {
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
                header.onOffsetChanged(topAndBottomOffset.coerceAtMost(0))
                this.onOffsetChanged(header, topAndBottomOffset)

                // Update the AppBarLayout's drawable state (for any elevation changes)
                updateAppBarLayoutDrawableState(
                    parent, header, offset,
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

    override fun onSaveInstanceState(parent: CoordinatorLayout, abl: AppBarLayout): Parcelable? {
        val superState = super.onSaveInstanceState(parent, abl)
        val scrollState = saveScrollState(superState, abl)
        return scrollState ?: superState
    }

    override fun onRestoreInstanceState(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        state: Parcelable
    ) {
        if (state is SavedState) {
            restoreScrollState(state as SavedState?, true)
            super.onRestoreInstanceState(parent, child, savedState!!.superState!!)
        } else {
            super.onRestoreInstanceState(parent, child, state)
            savedState = null
        }
    }

    fun restoreScrollState(state: SavedState?, force: Boolean) {
        if (savedState == null || force) {
            savedState = state
        }
    }

    fun saveScrollState(
        superState: Parcelable?,
        abl: AppBarLayout
    ): SavedState? {
        val offset = topAndBottomOffset

        // Try and find the first visible child...
        for (i in 0 until abl.childCount) {
            val child: View = abl.getChildAt(i)
            val visBottom = child.bottom + offset
            if (child.top + offset <= 0 && visBottom >= 0) {
                val ss = SavedState(superState ?: AbsSavedState.EMPTY_STATE)
                ss.fullyExpanded = offset == 0
                ss.fullyScrolled = !ss.fullyExpanded && -offset >= abl.totalScrollRange
                ss.firstVisibleChildIndex = i
                ss.firstVisibleChildAtMinimumHeight =
                    visBottom == ViewCompat.getMinimumHeight(child) + abl.topInset
                ss.firstVisibleChildPercentageShown = visBottom / child.height.toFloat()
                return ss
            }
        }
        return null
    }

    class SavedState : AbsSavedState {
        var fullyScrolled = false
        var fullyExpanded = false
        var firstVisibleChildIndex = 0
        var firstVisibleChildPercentageShown = 0f
        var firstVisibleChildAtMinimumHeight = false

        constructor(superState: Parcelable) : super(superState)
        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            fullyScrolled = source.readByte().toInt() != 0
            fullyExpanded = source.readByte().toInt() != 0
            firstVisibleChildIndex = source.readInt()
            firstVisibleChildPercentageShown = source.readFloat()
            firstVisibleChildAtMinimumHeight = source.readByte().toInt() != 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeByte((if (fullyScrolled) 1 else 0).toByte())
            dest.writeByte((if (fullyExpanded) 1 else 0).toByte())
            dest.writeInt(firstVisibleChildIndex)
            dest.writeFloat(firstVisibleChildPercentageShown)
            dest.writeByte((if (firstVisibleChildAtMinimumHeight) 1 else 0).toByte())
        }
    }

    override fun onFlingFinished(parent: CoordinatorLayout, layout: AppBarLayout) {
        // At the end of a manual fling, check to see if we need to snap to the edge-child
        snapToChildIfNeeded(parent, layout)
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

    private fun checkFlag(flags: Int, check: Int): Boolean {
        return flags and check == check
    }

    private fun canScrollChildren(
        parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View
    ): Boolean {
        return child.hasScrollableChildren() && parent.height - directTargetChild.height <= child.height
    }
}