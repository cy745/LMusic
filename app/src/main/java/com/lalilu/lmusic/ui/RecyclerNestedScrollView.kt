package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.lmusic.utils.AppBarOnStateChange

class RecyclerNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private lateinit var recyclerView: RecyclerView
    private var mAppbarState = AppBarOnStateChange.AppBarState.STATE_EXPANDED

    init {
        LMusicViewModel.getInstance(null).mAppBar.observeForever {
            mAppbarState = it.mAppbarState
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView = (getChildAt(0) as LinearLayout).getChildAt(0) as RecyclerView
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val params = recyclerView.layoutParams as LinearLayout.LayoutParams
        params.height = measuredHeight
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        requestDisallowInterceptTouchEvent(mAppbarState == AppBarOnStateChange.AppBarState.STATE_EXPANDED)
        super.onNestedPreScroll(target, dx, dy, consumed, type)
    }
}