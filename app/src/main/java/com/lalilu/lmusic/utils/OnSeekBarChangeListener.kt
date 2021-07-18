package com.lalilu.lmusic.utils

abstract class OnSeekBarChangeListenerAdapter {
    open fun onPositionChanged(position: Long, fromUser: Boolean) {}

    open fun onStartTrackingTouch(position: Long) {}

    open fun onStopTrackingTouch(position: Long) {}
}