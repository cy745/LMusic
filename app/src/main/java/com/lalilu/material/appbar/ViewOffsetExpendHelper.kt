package com.lalilu.material.appbar

import android.view.View
import androidx.core.view.ViewCompat

class ViewOffsetExpendHelper(private val view: View) {
    private var layoutTop = 0
    private var layoutBottom = 0
    private var offsetTop = 0
    var isVerticalOffsetEnabled = true

    var topAndBottomOffset: Int
        get() = offsetTop
        set(value) {
            setTopAndBottomOffset(value)
        }

    fun setTopAndBottomOffset(offset: Int): Boolean {
        if (isVerticalOffsetEnabled && offsetTop != offset) {
            offsetTop = offset
            applyOffsets()
            return true
        }
        return false
    }

    fun onViewLayout() {
        // Grab the original top and left
        layoutBottom = view.bottom
        layoutTop = view.top
    }

    fun applyOffsets() {
        ViewCompat.offsetTopAndBottom(view, offsetTop.coerceAtMost(0) - (view.top - layoutTop))
        view.bottom = layoutBottom + offsetTop
    }
}