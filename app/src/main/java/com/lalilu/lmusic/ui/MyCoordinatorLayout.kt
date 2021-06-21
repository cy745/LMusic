package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout

class MyCoordinatorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        println("[onTouchEvent]: " + ev?.action)
        return super.onTouchEvent(ev)
    }
}