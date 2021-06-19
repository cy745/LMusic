package com.lalilu.lmusic

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarOnStateChange : AppBarLayout.OnOffsetChangedListener {
    enum class AppBarState {
        STATE_EXPANDED, STATE_COLLAPSED, STATE_INTERMEDIATE
    }

    private var mCurrentState: AppBarState = AppBarState.STATE_EXPANDED

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (appBarLayout == null) return
        if (verticalOffset == 0) {
            if (mCurrentState != AppBarState.STATE_EXPANDED) onStateChanged(
                appBarLayout,
                AppBarState.STATE_EXPANDED
            )
            mCurrentState = AppBarState.STATE_EXPANDED
        } else if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != AppBarState.STATE_COLLAPSED) onStateChanged(
                appBarLayout,
                AppBarState.STATE_COLLAPSED
            )
            mCurrentState = AppBarState.STATE_COLLAPSED
        } else {
            if (mCurrentState != AppBarState.STATE_INTERMEDIATE) onStateChanged(
                appBarLayout,
                AppBarState.STATE_INTERMEDIATE
            )
            mCurrentState = AppBarState.STATE_INTERMEDIATE
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: AppBarState)
}