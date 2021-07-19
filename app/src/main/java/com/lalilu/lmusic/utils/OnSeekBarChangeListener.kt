package com.lalilu.lmusic.utils

abstract class OnSeekBarChangeListenerAdapter {
    // 处理进度变化的事件
    open fun onPositionChanged(position: Long, fromUser: Boolean) {}
    open fun onStartTrackingTouch(position: Long) {}
    open fun onStopTrackingTouch(position: Long) {}

    // 处理进度到头和到尾的事件
    open fun onProgressToMax() {}
    open fun onProgressToMin() {}
    open fun onProgressToMiddle() {}

    // 处理点击和点击释放的事件
    open fun onClick() {}
    open fun onLongClickStart() {}
    open fun onLongClickEnd() {}
    open fun onLongClickCancel() {}
}