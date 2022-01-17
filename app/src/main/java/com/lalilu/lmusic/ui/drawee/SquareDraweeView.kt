package com.lalilu.lmusic.ui.drawee

import android.content.Context
import android.util.AttributeSet

/**
 * 默认提供重采样大小功能的 正方形 DraweeView
 */
class SquareDraweeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SamplingDraweeView(context, attrs, defStyleAttr) {
    override var samplingValue: Int = 400

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}