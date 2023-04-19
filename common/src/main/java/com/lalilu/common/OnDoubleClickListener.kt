package com.lalilu.common

import android.view.View

/**
 * 双击事件监听器
 */
class OnDoubleClickListener(
    private val onDoubleClickCallback: (View) -> Unit
) : View.OnClickListener {
    private var mLastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - mLastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClickCallback(v)
        }
        mLastClickTime = clickTime
    }

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA = 300     //milliseconds
    }
}