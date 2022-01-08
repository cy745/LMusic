package com.lalilu.lmusic.utils

import android.graphics.Rect
import android.view.MotionEvent
import kotlin.math.max
import kotlin.math.min

/**
 *  提供拦截指定区域onTouchEvent事件的能力
 *
 */
interface AntiErrorTouchEvent {
    // 存储点击点的位置信息
    val rect: Rect
    val interceptSize: Int

    /**
     * 接管需要屏蔽的组件的 onTouchEvent 方法即可使用
     */
    fun checkTouchEvent(event: MotionEvent): Boolean {
        return event.rawY > rect.top
                && event.rawY < rect.bottom
                && whenToIntercept()
    }

    fun updateInterceptRect(y1: Int, y2: Int) {
        rect.bottom = max(y1, y2)
        rect.top = min(y1, y2)
    }


    fun whenToIntercept(): Boolean
}