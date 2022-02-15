package com.lalilu.material.appbar

import android.view.MotionEvent

/**
 *  提供拦截指定区域onTouchEvent事件的能力
 *
 */
interface AntiMisTouchEvent {
    fun isTimeToIntercept(): Boolean
    fun isInPlaceToIntercept(rawY: Float): Boolean

    /**
     * 接管需要屏蔽的组件的 onTouchEvent 方法即可使用
     */
    fun checkTouchEvent(event: MotionEvent): Boolean {
        return isInPlaceToIntercept(event.rawY)
                && isTimeToIntercept()
    }
}