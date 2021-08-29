package com.lalilu.lmusic.ui.appbar

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.lmusic.ui.PaletteDraweeView
import com.lalilu.lmusic.ui.appbar.AppBarOnStateChangeListener.Companion.STATE_EXPANDED
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    var mAppbarState = STATE_EXPANDED
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
        addOnOffsetChangedListener(object : AppBarOnStateChangeListener() {
            override fun onStatePercentage(percent: Float) {
                paletteDraweeView?.let { it.alpha = percent }
            }

            override fun onStateChanged(appBarLayout: AppBarLayout?, state: Int) {
                mAppbarState = state
            }
        })
    }
}