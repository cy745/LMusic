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
        return dependency is AppbarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout, child: View, dependency: View
    ): Boolean {
        offsetChildAsNeeded(child, dependency)
        return false
    }

    override fun onDependentViewRemoved(
        parent: CoordinatorLayout, child: View, dependency: View
    ) {
        if (dependency is AppbarLayout) {
            ViewCompat.removeAccessibilityAction(
                parent, AccessibilityActionCompat.ACTION_SCROLL_FORWARD.id
            )
            ViewCompat.removeAccessibilityAction(
                parent, AccessibilityActionCompat.ACTION_SCROLL_BACKWARD.id
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

    override fun findFirstDependency(views: MutableList<View>?): AppbarLayout? {
        return views?.first { it is AppbarLayout } as AppbarLayout?
    }

    override fun getScrollRange(v: View): Int {
        return if (v is AppbarLayout) v.totalScrollRange else super.getScrollRange(v)
    }

    private fun offsetChildAsNeeded(child: View, dependency: View) {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is MyAppbarBehavior) {
            // Offset the child, pinning it to the bottom the header-dependency
            ViewCompat.offsetTopAndBottom(child, dependency.bottom - child.top)
        }
    }
}