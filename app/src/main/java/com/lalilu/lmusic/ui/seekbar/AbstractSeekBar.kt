package com.lalilu.lmusic.ui.seekbar

import android.view.MotionEvent

interface AbstractSeekBar {
    // 数据变化相关周期函数
    //    fun updatePosition(playbackStateCompat: PlaybackStateCompat)
    fun updateProgress(event: MotionEvent, moving: Boolean)
    fun updatePosition(position: Long)

    // 触摸相关周期函数
    fun onTouchUp()
    fun onTouchUpWithChange()
    fun onTouchMove()
    fun onTouchDown()

    // 边缘进度相关周期函数
    fun onProgressMax()
    fun onProgressMin()
    fun onProgressMiddle()
}