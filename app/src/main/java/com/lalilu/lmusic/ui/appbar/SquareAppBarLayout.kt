package com.lalilu.lmusic.ui.appbar

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 如果处于展开状态则重新布局一次AppbarLayout的bottom
        if (AppBarStatusHelper.fullyExpend && changed) {
            this.layout(l, t, r, AppBarStatusHelper.mBottom)
        }

        super.onLayout(changed, l, t, r, b)
    }
}