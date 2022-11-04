package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import com.dirror.lyricviewx.LyricViewX
import com.lalilu.common.DeviceUtils

class MyLyricViewX(context: Context, attrs: AttributeSet) : LyricViewX(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(widthSize, DeviceUtils.getHeight(context))
    }
}