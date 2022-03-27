package com.lalilu.ui.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

open class ViewOffsetExpendBehavior<V : View>(
    context: Context?, attrs: AttributeSet?
) : CoordinatorLayout.Behavior<V>(context, attrs) {
    private var viewOffsetHelper: ViewOffsetExpendHelper? = null
    private var tempTopBottomOffset = 0
    var topAndBottomOffset: Int
        get() = viewOffsetHelper?.topAndBottomOffset ?: 0
        set(value) {
            setTopAndBottomOffset(value)
        }

    open fun setTopAndBottomOffset(offset: Int): Boolean {
        viewOffsetHelper?.let {
            return it.setTopAndBottomOffset(offset)
        }
        tempTopBottomOffset = offset
        return false
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)

        viewOffsetHelper = viewOffsetHelper ?: ViewOffsetExpendHelper(child)
        viewOffsetHelper?.let {
            it.onViewLayout()
            it.applyOffsets()

            if (tempTopBottomOffset != 0) {
                it.topAndBottomOffset = tempTopBottomOffset
                tempTopBottomOffset = 0
            }
        }
        return true
    }

    fun setVerticalOffsetEnabled(verticalOffsetEnabled: Boolean) {
        viewOffsetHelper?.let {
            it.isVerticalOffsetEnabled = verticalOffsetEnabled
        }
    }

    fun isVerticalOffsetEnabled(): Boolean {
        if (viewOffsetHelper == null) return false
        return viewOffsetHelper!!.isVerticalOffsetEnabled
    }
}