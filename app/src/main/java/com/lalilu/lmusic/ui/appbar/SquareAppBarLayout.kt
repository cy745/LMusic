package com.lalilu.lmusic.ui.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import com.google.android.material.appbar.AppBarLayout

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {

    var fullyExpend = false
    var mBottom: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = when (MeasureSpec.getMode(heightMeasureSpec)) {
            EXACTLY -> heightMeasureSpec
            AT_MOST -> widthMeasureSpec
            else -> widthMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 如果处于展开状态则重新布局一次AppbarLayout的bottom
        if (fullyExpend && changed) {
            this.layout(l, t, r, mBottom)
        }

        super.onLayout(changed, l, t, r, b)
    }
}