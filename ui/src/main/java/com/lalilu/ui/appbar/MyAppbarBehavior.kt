package com.lalilu.ui.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import com.lalilu.ui.appbar.AppbarLayout.LayoutParams.Companion.SCROLL_FLAG_SCROLL
import java.lang.ref.WeakReference
import kotlin.math.abs

class MyAppbarBehavior(
    context: Context? = null, attrs: AttributeSet? = null
) : ExpendHeaderBehavior<AppbarLayout>(context, attrs) {
    val offsetDelta: Int = 0

    private var mOffsetChangedListeners = LinkedHashSet<AppbarLayout.OnOffsetChangedListener>()
    private val onDragCallback: BaseDragCallback<AppbarLayout>? = null

    @NestedScrollType
    private var lastStartedType = 0
    private var lastNestedScrollingChildRef: WeakReference<View>? = null

    fun addOnOffsetExpendChangedListener(
        listener: AppbarLayout.OnOffsetChangedListener
    ) {
        mOffsetChangedListeners.add(listener)
    }

    private fun onOffsetChanged(appbarLayout: AppbarLayout, offset: Int) {
        mOffsetChangedListeners.forEach {
            it.onOffsetChanged(appbarLayout, offset)
        }
    }

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppbarLayout,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        // Return true if we're nested scrolling vertically, and we either have lift on scroll enabled
        // or we can scroll the children.
        // 决定何时开始接受子View的滑动事件
        val started = canScrollChildren(
            parent,
            child,
            directTargetChild
        ) && (axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)

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
        child: AppbarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // 只处理向上滚动的情况
        if (dy > 0) {
            consumed[1] = scroll(dy)
        }
    }

    /**
     * 当target滚动到达边缘且不再可被拖动的时候将调用此方法
     */
    override fun onNestedScroll(
        parent: CoordinatorLayout,
        child: AppbarLayout,
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
            consumed[1] = scroll(dyUnconsumed)
        }
    }

    override fun onStopNestedScroll(
        parent: CoordinatorLayout, child: AppbarLayout, target: View, type: Int
    ) {
        // onStartNestedScroll for a fling will happen before onStopNestedScroll for the scroll. This
        // isn't necessarily guaranteed yet, but it should be in the future. We use this to our
        // advantage to check if a fling (ViewCompat.TYPE_NON_TOUCH) will start after the touch scroll
        // (ViewCompat.TYPE_TOUCH) ends
        if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
            // If we haven't been flung, or a fling is ending
            snapToChildIfNeeded()
        }

        // Keep a reference to the previous nested scrolling child
        lastNestedScrollingChildRef = WeakReference(target)
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: AppbarLayout,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        parentWeakReference = WeakReference(parent)
        childWeakReference = WeakReference(child)

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
            parent, child, parentWidthMeasureSpec,
            widthUsed, parentHeightMeasureSpec, heightUsed
        )
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: AppbarLayout,
        layoutDirection: Int
    ): Boolean {
        val handled = super.onLayoutChild(parent, child, layoutDirection)

        // The priority for actions here is (first which is true wins):
        // 1. forced pending actions
        // 2. offsets for restorations
        // 3. non-forced pending actions
        val pendingAction: Int = child.pendingAction
        if (pendingAction and AppbarLayout.PENDING_ACTION_FORCE == 0) {
            // TODO: 恢复之前的状态
        } else if (pendingAction != AppbarLayout.PENDING_ACTION_NONE) {
            val animate = pendingAction and AppbarLayout.PENDING_ACTION_ANIMATE_ENABLED != 0
            if (pendingAction and AppbarLayout.PENDING_ACTION_COLLAPSED != 0) {
                val offset: Int = getCollapsedOffset(parent, child)
                if (animate) {
                    animateOffsetTo(offset)
                } else {
                    setHeaderTopBottomOffset(offset)
                }
            } else if (pendingAction and AppbarLayout.PENDING_ACTION_EXPANDED != 0) {
                if (animate) {
                    animateOffsetTo(0)
                } else {
                    setHeaderTopBottomOffset(0)
                }
            }
        }

        // Finally reset any pending states
        child.resetPendingAction()

        // We may have changed size, so let's constrain the top and bottom offset correctly,
        // just in case we're out of the bounds
        val offset = when (nowState) {
            STATE_FULLY_EXPENDED -> getFullyExpendOffset(parent, child)
            STATE_COLLAPSED -> getCollapsedOffset(parent, child)
            STATE_EXPENDED -> 0
            else -> topAndBottomOffset
        }
        setTopAndBottomOffset(offset)

        // Make sure we dispatch the offset update
        child.onOffsetChanged(topAndBottomOffset.coerceAtMost(0))
        this.onOffsetChanged(child, topAndBottomOffset)
        return handled
    }

    override fun setHeaderTopBottomOffset(
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

                val parent = parentWeakReference?.get() ?: return consumed
                val child = childWeakReference?.get() ?: return consumed

                if (offsetChanged) {
                    // If the offset has changed, pass the change to any child scroll effect.
                    for (i in 0 until child.childCount) {
                        val params = child.getChildAt(i).layoutParams as AppbarLayout.LayoutParams
                        val scrollEffect = params.scrollEffect
                        if (scrollEffect != null && params.scrollFlags and SCROLL_FLAG_SCROLL != 0) {
                            scrollEffect.onOffsetChanged(
                                child,
                                child.getChildAt(i),
                                topAndBottomOffset.toFloat()
                            )
                        }
                    }
                }
                if (!offsetChanged && child.haveChildWithInterpolator) {
                    // If the offset hasn't changed and we're using an interpolated scroll
                    // then we need to keep any dependent views updated. CoL will do this for
                    // us when we move, but we need to do it manually when we don't (as an
                    // interpolated scroll may finish early).
                    parent.dispatchDependentViewsChanged(child)
                }

                // Dispatch the updates to any listeners
                child.onOffsetChanged(topAndBottomOffset.coerceAtMost(0))
                this.onOffsetChanged(child, topAndBottomOffset)
            }
        }
        return consumed
    }

    override fun canDragView(view: AppbarLayout): Boolean {
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

    private fun getAppBarChildOnOffset(
        layout: AppbarLayout, offset: Int
    ): View? {
        val absOffset = abs(offset)
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (absOffset in child.top..child.bottom) return child
        }
        return null
    }

    private fun canScrollChildren(
        parent: CoordinatorLayout, child: AppbarLayout, directTargetChild: View
    ): Boolean {
        return child.hasScrollableChildren && parent.height - directTargetChild.height <= child.height
    }

    abstract class BaseDragCallback<T : AppbarLayout?> {
        abstract fun canDrag(appBarLayout: T): Boolean
    }
}