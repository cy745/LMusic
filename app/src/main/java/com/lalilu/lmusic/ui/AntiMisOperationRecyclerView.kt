package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class AntiMisOperationRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), RecyclerView.OnItemTouchListener {
    private var limitPadding = 40

    init {
        addOnItemTouchListener(this)
    }

    private fun isMisOperation(raw: Number, limitLeft: Number, limitRight: Number): Boolean {
        return raw.toLong() < limitLeft.toLong() || raw.toLong() > limitRight.toLong()
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return isMisOperation(e.rawX, limitPadding, width - limitPadding)
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}