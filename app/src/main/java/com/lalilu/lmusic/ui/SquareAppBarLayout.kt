package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.dirror.lyricviewx.LyricViewX
import com.lalilu.R
import com.lalilu.common.DeviceUtils
import com.lalilu.common.HapticUtils
import com.lalilu.common.ifNaN
import com.lalilu.lmusic.ui.drawee.BlurImageView
import com.lalilu.lmusic.utils.interpolator.ParabolaInterpolator
import com.lalilu.ui.appbar.AppBarLayout
import com.lalilu.ui.appbar.AppBarLayout.OnOffsetChangedListener
import com.lalilu.ui.appbar.CollapsingToolbarLayout
import com.lalilu.ui.appbar.ExpendHeaderBehavior
import com.lalilu.ui.appbar.MyAppbarBehavior
import me.qinc.lib.edgetranslucent.EdgeTransparentView
import kotlin.math.max
import kotlin.math.roundToInt

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var mTips: TextView? = null
    private var mToolbar: Toolbar? = null
    private var mLyricViewX: LyricViewX? = null
    private var mDraweeView: BlurImageView? = null
    private var mEdgeTransparentView: EdgeTransparentView? = null
    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var interpolator = AccelerateDecelerateInterpolator()
    private var parabolaInterpolator = ParabolaInterpolator()
    private var behavior = MyAppbarBehavior(context, null)

    private var maxDragHeight = 200

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        mTips = findViewById(R.id.fm_tips)
        mToolbar = findViewById(R.id.fm_toolbar)
        mDraweeView = findViewById(R.id.fm_top_pic)
        mLyricViewX = findViewById(R.id.fm_lyric_view_x)
        mEdgeTransparentView = findViewById(R.id.fm_edge_transparent_view)
        mCollapsingToolbarLayout = findViewById(R.id.fm_collapse_layout)

        val deviceHeight = DeviceUtils.getHeight(context)
        setHeightToView(mDraweeView, deviceHeight)
        setHeightToView(mLyricViewX, deviceHeight - 100)
        setHeightToView(mEdgeTransparentView, deviceHeight - 100)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<AppBarLayout> = behavior

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    private fun setHeightToView(view: View?, height: Number) {
        view?.let {
            if (it.height == height.toInt()) return
            it.layoutParams = it.layoutParams.also { params -> params.height = height.toInt() }
        }
    }

    private fun getMutableDragOffset(): Float {
        return max(maxDragHeight, mDraweeView?.maxOffset ?: 0).toFloat()
    }

    private fun getMutableDragPercent(offset: Float): Float {
        return (offset / getMutableDragOffset()).coerceIn(0F, 1F).ifNaN(0f)
    }

    private fun getMutableScalePercent(offset: Float, fullyExpendedOffset: Number): Float {
        val dragOffset = (mDraweeView?.maxOffset ?: 0).toFloat()
        return ((offset - dragOffset) / (fullyExpendedOffset.toFloat() - dragOffset))
            .coerceIn(0F, 1F).ifNaN(0f)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.clipChildren = false

        addOnOffsetChangedListener(OnOffsetChangedListener { appbar, offset ->
            if (offset > 0) return@OnOffsetChangedListener

            val collapsedOffset = offset.coerceAtMost(0).toFloat()
            val minCollapsedOffset = behavior.getCollapsedOffset(parent as View, appbar)
            val collapsedPercent = (collapsedOffset / minCollapsedOffset.toFloat()).coerceIn(0F, 1F)

            mDraweeView?.let { it.alpha = 1f - collapsedPercent }
        })
        behavior.addOnOffsetExpendChangedListener { appbar, offset ->
            val expendedOffset = offset.coerceAtLeast(0).toFloat()
            val maxExpendedOffset = behavior.getFullyExpendOffset(parent as View, appbar)
            val expendedPercent = (expendedOffset / maxExpendedOffset.toFloat()).coerceIn(0F, 1F)

            val interpolation = interpolator.getInterpolation(expendedPercent)
            val reverseValue = parabolaInterpolator.getInterpolation(expendedPercent)
            val alphaPercentDecrease = (1F - interpolation * 2).coerceAtLeast(0F)
            val alphaPercentIncrease = (2 * interpolation - 1F).coerceAtLeast(0F)

            val scalePercent = getMutableScalePercent(expendedOffset, maxExpendedOffset)
            val dragPercent = getMutableDragPercent(expendedOffset)
            val topOffset = appbar.width / 2f * reverseValue

            mDraweeView?.let {
                it.dragPercent = dragPercent
                it.scalePercent = scalePercent
                it.blurPercent = scalePercent
                it.translationY = -topOffset * 0.6f
            }

            mCollapsingToolbarLayout?.let {
                it.translationY = topOffset
                val toolbarTextColor =
                    Color.argb((alphaPercentDecrease * 255).roundToInt(), 255, 255, 255)
                it.setExpandedTitleColor(toolbarTextColor)
            }

            mToolbar?.let {
                it.visibility =
                    if (alphaPercentDecrease <= 0.05) View.INVISIBLE else View.VISIBLE
                it.alpha = alphaPercentDecrease
            }

            mTips?.let {
                it.visibility =
                    if (alphaPercentDecrease <= 0.05) View.INVISIBLE else View.VISIBLE
                it.alpha = alphaPercentDecrease
            }

            mLyricViewX?.let {
                it.alpha = alphaPercentIncrease
            }
        }
        behavior.addOnStateChangeListener(object :
            ExpendHeaderBehavior.OnScrollToThresholdListener() {
            override fun onScrollToThreshold() {
                HapticUtils.haptic(
                    this@SquareAppBarLayout,
                    HapticUtils.Strength.HAPTIC_STRONG
                )
            }
        })
    }
}