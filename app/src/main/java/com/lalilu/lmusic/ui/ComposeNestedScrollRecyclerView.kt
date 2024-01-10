package com.lalilu.lmusic.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 重写onTouchEvent，修改其计算dy的逻辑，解决RecyclerView嵌入Compose结合NestedScroll时，
 * RecyclerView内MotionEvent的getY获取到的值出现异常波动的问题
 */
class ComposeNestedScrollRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : RecyclerView(context, attrs) {
    private val position = intArrayOf(0, 0)
    private var downX = 0
    private var downY = 0
    private var verticalDrag: Boolean? = null

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN) {
            downX = (e.x + 0.5f).toInt()
            downY = (e.y + 0.5f).toInt()
            getLocationOnScreen(position)
            verticalDrag = null
        }

        if (e.actionMasked == MotionEvent.ACTION_MOVE) {
            val result = super.onInterceptTouchEvent(e)
            val currentX = (e.x + 0.5f).toInt()
            val currentY = (e.y + 0.5f).toInt()

            // 当开始拖拽时计算其滑动方向并记录
            if (result) {
                verticalDrag = abs(currentY - downY) > abs(currentX - downX)
            }
            return result
        }
        return super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (verticalDrag == true && e.actionMasked == MotionEvent.ACTION_MOVE) {
            e.setLocation(e.x, e.rawY - position[1])
        }
        return super.onTouchEvent(e)
    }
}