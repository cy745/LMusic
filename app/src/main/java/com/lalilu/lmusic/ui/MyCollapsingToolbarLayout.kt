package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout

class MyCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CollapsingToolbarLayout(context, attrs, defStyleAttr) {
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
}