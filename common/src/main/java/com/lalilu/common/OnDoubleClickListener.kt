package com.lalilu.common

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * 双击事件监听器
 */
class OnDoubleClickListener(
    private val onDoubleClickCallback: (View) -> Unit
) : View.OnTouchListener {
    private var mLastClickTime: Long = 0
    private var mLastClickX: Float = 0f
    private var mLastClickY: Float = 0f
    private var mLastClickView: View? = null
    private var mLastClickMotionEvent: MotionEvent? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        println("onTouch: $event")
        if (event.action == MotionEvent.ACTION_UP) {
            val clickTime = System.currentTimeMillis()
            val clickX = event.x
            val clickY = event.y
            if (clickTime - mLastClickTime < DOUBLE_CLICK_TIME_DELTA
                && abs(clickX - mLastClickX) < DOUBLE_CLICK_POSITION_DELTA
                && abs(clickY - mLastClickY) < DOUBLE_CLICK_POSITION_DELTA
                && mLastClickView == v
                && mLastClickMotionEvent?.action == MotionEvent.ACTION_DOWN
                && event.action == MotionEvent.ACTION_UP
            ) {
                onDoubleClickCallback(v)
                mLastClickTime = 0
                mLastClickX = 0f
                mLastClickY = 0f
                mLastClickView = null
                mLastClickMotionEvent = null
            } else {
                mLastClickTime = clickTime
                mLastClickX = clickX
                mLastClickY = clickY
                mLastClickView = v
                mLastClickMotionEvent = event
            }
        }
        return false
    }

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA = 300     //milliseconds
        private const val DOUBLE_CLICK_POSITION_DELTA = 20  //pixels
    }
}