package com.lalilu.lmusic.ui.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.appbar.AppBarLayout

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    private val rect = Rect(0, 0, 0, 0)
    private val size = 100

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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        rect.bottom = height
        rect.top = height - size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (event.rawY > rect.top && event.rawY < rect.bottom) true
        else super.onTouchEvent(event)
    }
}