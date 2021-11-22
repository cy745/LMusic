package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.lalilu.lmusic.utils.AntiErrorSlide

/**
 *  可处理滑动冲突的 CoordinatorLayout
 */
class MyCoordinatorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {
    private val antiErrorSlide = AntiErrorSlide()

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        antiErrorSlide.dispatchTouchEvent(ev, parent)
        return super.dispatchTouchEvent(ev)
    }
}