package com.lalilu.lmusic.utils

import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewParent

/**
 *  为子父组件之间提供处理滑动冲突的能力
 *
 *  @param whenToSolve 决定何时应该进行拦截判断
 *  @param isInterceptNow 决定此刻是否需要拦截
 */
class AntiSlideConflict(
    val whenToSolve: (disX: Float, disY: Float) -> Boolean,
    val isInterceptNow: (disX: Float, disY: Float) -> Boolean
) {
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
                    val disX = ev.x - mPointGapF.x
                    val disY = ev.y - mPointGapF.y
                    if (whenToSolve(disX, disY)) {
                        isIntercept = isInterceptNow(disX, disY)
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