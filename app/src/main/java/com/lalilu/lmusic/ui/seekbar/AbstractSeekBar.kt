package com.lalilu.lmusic.ui.seekbar

import androidx.annotation.FloatRange

interface AbstractSeekBar {
    // 数据变化相关周期函数
    fun updateProgress(distance: Float)
    fun updatePosition(position: Long)

    // 触摸相关周期函数
    fun onTouchUpWithChange()

    // 边缘进度相关周期函数
    fun onProgressMax()
    fun onProgressMin()
    fun onProgressMiddle()
}

abstract class OnSeekBarListenerAdapter {
    // 处理进度变化的事件
    open fun onPositionUpdate(position: Long) {}

    // 处理进度到头和到尾的事件
    open fun onProgressToMax() {}
    open fun onProgressToMin() {}
    open fun onProgressToMiddle() {}

    // 处理点击和点击释放的事件
    open fun onPlayPause() {}
    open fun onPlayPrevious() {}
    open fun onPlayNext() {}
}

interface OnSeekBarDragUpProgressListener {
    fun onDragUpProgressUpdate(
        @FloatRange(from = 0.0, to = 1.0)
        dragUpProgress: Float
    )
}