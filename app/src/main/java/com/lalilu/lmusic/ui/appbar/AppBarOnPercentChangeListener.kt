package com.lalilu.lmusic.ui.appbar

import com.google.android.material.appbar.AppBarLayout

open class AppBarOnPercentChangeListener : AppBarLayout.OnOffsetChangedListener {

    @Volatile
    var verticalOffset = 0

    open fun onPercentChanged(percent: Float) {}

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        onPercentChanged(1 + verticalOffset.toFloat() / appBarLayout.totalScrollRange)
        this.verticalOffset = verticalOffset
    }
}