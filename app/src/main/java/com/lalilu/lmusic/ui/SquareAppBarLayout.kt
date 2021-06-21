package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.lmusic.utils.AppBarOnStateChange
import com.lalilu.lmusic.utils.AppBarOnStateChange.AppBarState
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    var mAppbarState = AppBarState.STATE_EXPANDED
    var paletteDraweeView: PaletteDraweeView? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        paletteDraweeView =
            (getChildAt(0) as CollapsingToolbarLayout).getChildAt(0) as PaletteDraweeView
    }

    init {
        addOnOffsetChangedListener(object : AppBarOnStateChange() {
            override fun onStatePercentage(percent: Float) {
                paletteDraweeView?.let { it.alpha = percent }
            }

            override fun onStateChanged(appBarLayout: AppBarLayout?, state: AppBarState) {
                mAppbarState = state
            }
        })
    }
}