package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import com.lalilu.common.DeviceUtils
import me.qinc.lib.edgetranslucent.EdgeTransparentView

class MyEdgeTransparentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : EdgeTransparentView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(widthSize, DeviceUtils.getHeight(context))
    }
}