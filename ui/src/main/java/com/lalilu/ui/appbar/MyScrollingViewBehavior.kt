package com.lalilu.ui.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat

@SuppressLint("RestrictedApi")
class MyScrollingViewBehavior(
    context: Context?, attrs: AttributeSet?
) : HeaderScrollingViewBehavior(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout, child: View, dependency: View
    ): Boolean {
        offsetChildAsNeeded(child, dependency)
        updateLiftedStateIfNeeded(child, dependency)
        return false
    }

    override fun onDependentViewRemoved(
        parent: CoordinatorLayout, child: View, dependency: View
    ) {
        if (dependency is AppBarLayout) {
            ViewCompat.removeAccessibilityAction(
                parent,
                AccessibilityActionCompat.ACTION_SCROLL_FORWARD.id
            )
            ViewCompat.removeAccessibilityAction(
                parent,
                AccessibilityActionCompat.ACTION_SCROLL_BACKWARD.id
            )
        }
    }

    override fun onRequestChildRectangleOnScreen(
        parent: CoordinatorLayout,
        child: View,
        rectangle: Rect,
        immediate: Boolean
    ): Boolean {
        findFirstDependency(parent.getDependencies(child))?.let { header ->
            // Offset the rect by the child's left/top
            rectangle.offset(child.left, child.top)
            val parentRect = tempRect1
            parentRect.set(0, 0, parent.width, parent.height)
            if (!parentRect.contains(rectangle)) {
                // If the rectangle can not be fully seen the visible bounds, collapse
                // the AppBarLayout
                header.setExpanded(false, !immediate)
                return true
            }
        }
        return false
    }

    override fun getOverlapRatioForOffset(header: View?): Float {
        if (header is AppBarLayout) {
            val totalScrollRange = header.totalScrollRange
            val preScrollDown = header.downNestedPreScrollRange
            val offset = getAppBarLayoutOffset(header)
            if (preScrollDown != 0 && totalScrollRange + offset <= preScrollDown) {
                // If we're in a pre-scroll down. Don't use the offset at all.
                return 0f
            } else {
                val availScrollRange = totalScrollRange - preScrollDown
                if (availScrollRange != 0) {
                    // Else we'll use a interpolated ratio of the overlap, depending on offset
                    return 1f + offset / availScrollRange.toFloat()
                }
            }
        }
        return 0f
    }

    override fun findFirstDependency(views: MutableList<View>?): AppBarLayout? {
        return views?.first { it is AppBarLayout } as AppBarLayout?
    }

    override fun getScrollRange(v: View): Int {
        return if (v is AppBarLayout) v.totalScrollRange else super.getScrollRange(v)
    }

    private fun updateLiftedStateIfNeeded(child: View, dependency: View) {
        if (dependency is AppBarLayout) {
            if (dependency.isLiftOnScroll) {
                dependency.setLiftedState(dependency.shouldLift(child))
            }
        }
    }

    private fun offsetChildAsNeeded(child: View, dependency: View) {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is MyAppbarBehavior) {
            // Offset the child, pinning it to the bottom the header-dependency, maintaining
            // any vertical gap and overlap
            ViewCompat.offsetTopAndBottom(
                child, (dependency.bottom - child.top
                        + behavior.offsetDelta
                        + verticalLayoutGap)
                        - getOverlapPixelsForOffset(dependency)
            )
        }
    }

    private fun getAppBarLayoutOffset(abl: AppBarLayout): Int {
        val behavior = (abl.layoutParams as CoordinatorLayout.LayoutParams).behavior
        return if (behavior is MyAppbarBehavior) behavior.topAndBottomOffset else 0
    }
}