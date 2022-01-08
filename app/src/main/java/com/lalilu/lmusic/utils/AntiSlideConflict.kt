package com.lalilu.lmusic.utils

import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewParent
import kotlin.math.abs

/**
 *  为子父组件之间提供处理滑动冲突的能力
 *
 *  内部开始竖向开始滑动后固定滑动方向，屏蔽父组件的横向滑动
 */
class AntiSlideConflict {
    // 存储点击点的位置信息
    private val mPointGapF: PointF = PointF(0f, 0f)
    private var isSolve: Boolean = false
    private var isIntercept: Boolean = false

    /**
     * 接管需要屏蔽的组件的 dispatchTouchEvent 方法即可使用
     */
    fun dispatchTouchEvent(ev: MotionEvent, parent: ViewParent) {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mPointGapF.set(ev.x, ev.y)
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isSolve) {
                    val disX = abs(ev.x - mPointGapF.x)
                    val disY = abs(ev.y - mPointGapF.y)
                    if (disX != disY) {
                        // 判断滑动的方向，如果是竖向滑动则触发拦截
                        isIntercept = disX < disY
                        isSolve = true
                    }
                }
                parent.requestDisallowInterceptTouchEvent(isIntercept)
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                isIntercept = false
                isSolve = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
    }
}