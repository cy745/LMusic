package com.lalilu.lmusic.ui.seekbar

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