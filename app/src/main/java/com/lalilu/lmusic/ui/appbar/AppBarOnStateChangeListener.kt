package com.lalilu.lmusic.ui.appbar

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarOnStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    companion object {
        const val STATE_EXPANDED = 0
        const val STATE_COLLAPSED = 1
        const val STATE_INTERMEDIATE = 2
    }

    private var mCurrentState: Int = STATE_EXPANDED

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val percent = 1 + verticalOffset.toFloat() / appBarLayout.totalScrollRange
        onStatePercentage(percent)

        val nextState = when {
            verticalOffset >= 0 -> STATE_EXPANDED
            abs(verticalOffset) >= appBarLayout.totalScrollRange -> STATE_COLLAPSED
            else -> STATE_INTERMEDIATE
        }
        if (nextState != mCurrentState) onStateChanged(appBarLayout, nextState)
        mCurrentState = nextState
    }

    open fun onStatePercentage(percent: Float) {}

    abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: Int)
}