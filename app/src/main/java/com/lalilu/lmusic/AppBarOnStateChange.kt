package com.lalilu.lmusic

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarOnStateChange : AppBarLayout.OnOffsetChangedListener {
    companion object {
        const val STATE_EXPANDED = 0
        const val STATE_COLLAPSED = 1
        const val STATE_INTERMEDIATE = 2
    }

    private var mCurrentState: Int = STATE_EXPANDED

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (appBarLayout == null) return
        if (verticalOffset == 0) {
            if (mCurrentState != STATE_EXPANDED) onStateChanged(appBarLayout, STATE_EXPANDED)
            mCurrentState = STATE_EXPANDED
        } else if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != STATE_COLLAPSED) onStateChanged(appBarLayout, STATE_COLLAPSED)
            mCurrentState = STATE_COLLAPSED
        } else {
            if (mCurrentState != STATE_INTERMEDIATE) onStateChanged(
                appBarLayout,
                STATE_INTERMEDIATE
            )
            mCurrentState = STATE_INTERMEDIATE
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: Int)
}