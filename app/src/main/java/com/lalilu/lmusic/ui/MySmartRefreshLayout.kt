package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.lalilu.lmusic.utils.AntiSlideConflict
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlin.math.abs

class MySmartRefreshLayout(context: Context?, attrs: AttributeSet?) :
    SmartRefreshLayout(context, attrs) {
    private val antiSlideConflict = AntiSlideConflict(
        whenToSolve = { disX, disY -> abs(disX) != abs(disY) },
        isInterceptNow = { disX, disY -> abs(disX) < abs(disY) || disX < 0 }
    )

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        antiSlideConflict.dispatchTouchEvent(e, parent)
        return super.dispatchTouchEvent(e)
    }
}