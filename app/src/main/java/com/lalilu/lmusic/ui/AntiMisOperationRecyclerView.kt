package com.lalilu.lmusic.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.common.Mathf

class AntiMisOperationRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var limitPadding = 40
    var totalScrollY = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val location = IntArray(2)
        this.getLocationInWindow(location)

        val result = e?.action == MotionEvent.ACTION_MOVE
                && location[1] > measuredWidth
        requestDisallowInterceptTouchEvent(result)
        return super.onTouchEvent(e)
    }

    init {
        addOnItemTouchListener(object : OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return isMisOperation(e.rawX, limitPadding, width - limitPadding)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun isMisOperation(raw: Number, limitLeft: Number, limitRight: Number): Boolean {
        return raw.toLong() < limitLeft.toLong() || raw.toLong() > limitRight.toLong()
    }

    override fun scrollToPosition(position: Int) {
        totalScrollY = position
        super.scrollToPosition(position)
    }

    override fun smoothScrollToPosition(position: Int) {
        totalScrollY = position
        super.smoothScrollToPosition(position)
    }

    override fun onScrolled(dx: Int, dy: Int) {
        totalScrollY = Mathf.clamp(0, Int.MAX_VALUE, totalScrollY + dy)
    }
}