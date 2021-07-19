package com.lalilu.lmusic.ui.seekbar

import android.support.v4.media.session.PlaybackStateCompat
import android.view.MotionEvent

interface AbstractSeekBar {
    // 数据变化相关周期函数
    fun updateProgress(event: MotionEvent, moving: Boolean)
    fun updatePosition(playbackStateCompat: PlaybackStateCompat)
    fun updatePosition(position: Long)

    // 触摸相关周期函数
    fun onTouchUp()
    fun onTouchUpWithChange()
    fun onTouchMove()
    fun onTouchDown()
    fun onTouchLongClickStart()
    fun onTouchLongClickEnd()
    fun onTouchLongClickCancel()

    // 边缘进度相关周期函数
    fun onProgressMax()
    fun onProgressMin()
    fun onProgressMiddle()
}