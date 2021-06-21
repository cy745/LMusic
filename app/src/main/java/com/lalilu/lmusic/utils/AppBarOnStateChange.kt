package com.lalilu.lmusic.utils

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarOnStateChange : AppBarLayout.OnOffsetChangedListener {
    enum class AppBarState {
        STATE_EXPANDED, STATE_COLLAPSED, STATE_INTERMEDIATE
    }

    private var mCurrentState: AppBarState = AppBarState.STATE_EXPANDED

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (appBarLayout == null) return
        onStatePercentage(1 + verticalOffset.toFloat() / appBarLayout.totalScrollRange)
        val nextState = when {
            abs(verticalOffset) == 0 ->
                AppBarState.STATE_EXPANDED
            abs(verticalOffset) >= appBarLayout.totalScrollRange ->
                AppBarState.STATE_COLLAPSED
            else -> AppBarState.STATE_INTERMEDIATE
        }
        if (nextState != mCurrentState) onStateChanged(appBarLayout, nextState)
        mCurrentState = nextState
    }

    open fun onStatePercentage(percent: Float) {}

    abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: AppBarState)
}