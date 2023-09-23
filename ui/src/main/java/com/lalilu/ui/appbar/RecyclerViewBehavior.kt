package com.lalilu.ui.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("RestrictedApi")
class RecyclerViewBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<RecyclerView>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: RecyclerView,
        dependency: View,
    ): Boolean {
        return dependency is CoverAppbar
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: RecyclerView,
        dependency: View,
    ): Boolean {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior

        if (behavior is AppbarBehavior) {
            ViewCompat.offsetTopAndBottom(child, dependency.bottom - child.top)
        }
        return false
    }

    private val available = Rect()
    private val outRect = Rect()

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: RecyclerView,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int,
    ): Boolean {
        parent.children.find { it is CoverAppbar }?.let { it as? CoverAppbar }?.let { header ->
            var availableHeight = MeasureSpec.getSize(parentHeightMeasureSpec)
                .takeIf { it > 0 }
                ?: parent.measuredHeight

            availableHeight -= header.minAnchorHeight

            val heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY)

            parent.onMeasureChild(
                child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed
            )
            return true
        }
        return false
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: RecyclerView,
        layoutDirection: Int,
    ): Boolean {
        parent.children.find { it is CoverAppbar }?.let { header ->
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            available.set(
                parent.paddingLeft + lp.leftMargin,
                header.bottom + lp.topMargin,
                parent.width - parent.paddingRight - lp.rightMargin,
                parent.height + header.bottom - parent.paddingBottom - lp.bottomMargin
            )

            val parentInsets = parent.lastWindowInsets
            if (parentInsets != null && ViewCompat.getFitsSystemWindows(parent)
                && !ViewCompat.getFitsSystemWindows(child)
            ) {
                available.left += parentInsets.systemWindowInsetLeft
                available.right -= parentInsets.systemWindowInsetRight
            }

            GravityCompat.apply(
                resolveGravity(lp.gravity),
                child.measuredWidth,
                child.measuredHeight,
                available, outRect,
                layoutDirection
            )
            child.layout(outRect.left, outRect.top, outRect.right, outRect.bottom)
            return true
        }
        parent.onLayoutChild(child, layoutDirection)
        return true
    }

    private fun resolveGravity(gravity: Int): Int {
        return if (gravity == Gravity.NO_GRAVITY) GravityCompat.START or Gravity.TOP else gravity
    }
}

