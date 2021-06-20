package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.lmusic.utils.AppBarOnStateChange
import com.lalilu.lmusic.utils.AppBarOnStateChange.AppBarState

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    var mAppbarState = AppBarState.STATE_EXPANDED

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    init {
        addOnOffsetChangedListener(object : AppBarOnStateChange() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: AppBarState) {
                mAppbarState = state
            }
        })
    }
}