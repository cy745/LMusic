package com.lalilu.lmusic.ui.appbar

import com.google.android.material.appbar.AppBarLayout

object AppBarStatusHelper : AppBarOnPercentChangeListener() {
    private lateinit var appbar: AppBarLayout
    private lateinit var listenPercent: (percent: Float) -> Unit
    private var lastStatus = AppBarStatus.STATUS_NORMAL

    var normalHeight = -1

    fun initial(appbar: AppBarLayout, listenPercent: (percent: Float) -> Unit): AppBarStatusHelper {
        if (normalHeight == -1 || normalHeight == 0) normalHeight = appbar.height

        this.appbar = appbar
        this.appbar.addOnOffsetChangedListener(this)
        this.listenPercent = listenPercent

        return this
    }

    fun getNextStatusByNextPosition(nextPosition: Int): AppBarStatus {
        lastStatus = when {
            (nextPosition > appbar.height) -> AppBarStatus.STATUS_EXPENDED
            (appbar.y + appbar.totalScrollRange <= 0 || verticalOffset < 0) -> AppBarStatus.STATUS_NORMAL
            else -> lastStatus
        }
        return lastStatus
    }

    override fun onPercentChanged(percent: Float) {
        listenPercent.invoke(percent)
    }
}