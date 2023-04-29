package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.dirror.lyricviewx.LyricViewX
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.common.calculatePercentIn
import com.lalilu.common.ifNaN
import com.lalilu.lmusic.utils.interpolator.ParabolaInterpolator
import com.lalilu.ui.appbar.AppbarLayout
import com.lalilu.ui.appbar.CollapsingLayout
import com.lalilu.ui.appbar.MyAppbarBehavior
import com.lalilu.ui.internal.StateHelper
import me.qinc.lib.edgetranslucent.EdgeTransparentView
import kotlin.math.max

class SquareAppbarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppbarLayout(context, attrs, defStyleAttr) {
    private var mToolbar: Toolbar? = null
    private var mLyricViewX: LyricViewX? = null
    private var mDraweeView: BlurImageView? = null
    private var mEdgeTransparentView: EdgeTransparentView? = null
    private var mCollapsingToolbarLayout: CollapsingLayout? = null
    private var interpolator = AccelerateDecelerateInterpolator()
    private var parabolaInterpolator = ParabolaInterpolator()
    private var behavior = MyAppbarBehavior(context, null)
    private var maxDragHeight = 200

    override fun getBehavior(): CoordinatorLayout.Behavior<AppbarLayout> = behavior

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 正常测量高度，以将最大高度传递给子View
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 将宽度值传递给高度，将View的宽高设置为正方形宽高
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

    private fun getMutableDragOffset(): Float {
        return max(maxDragHeight, mDraweeView?.maxOffset ?: 0).toFloat()
    }

    private fun getMutableDragPercent(offset: Float): Float {
        val dragOffset = getMutableDragOffset()
        if (dragOffset == 0f || offset == 0f) return 0f
        return (offset / dragOffset).coerceIn(0F, 1F)
    }

    private fun getMutableScalePercent(offset: Float, fullyExpendedOffset: Number): Float {
        val dragOffset = (mDraweeView?.maxOffset ?: 0).toFloat()
        return ((offset - dragOffset) / (fullyExpendedOffset.toFloat() - dragOffset))
            .coerceIn(0F, 1F).ifNaN(0f)
    }

//    init {
//        rootView.setOnApplyWindowInsetsListener { v, insets ->
//            WindowInsetsCompat
//                .toWindowInsetsCompat(insets)
//                .getInsets(WindowInsetsCompat.Type.statusBars())
//                .top.takeIf { it > 0 }?.let {
//                    mToolbar?.setPadding(0, it, 0, 0)
//                    mToolbar?.layoutParams?.apply { height += it }
//                }
//            insets
//        }
//    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mToolbar = findViewById(R.id.fm_toolbar)
        mDraweeView = findViewById(R.id.fm_top_pic)
        mLyricViewX = findViewById(R.id.fm_lyric_view_x)
        mEdgeTransparentView = findViewById(R.id.fm_edge_transparent_view)
        mCollapsingToolbarLayout = findViewById(R.id.fm_collapse_layout)
        this.clipChildren = false

        behavior.addOnOffsetExpendChangedListener { appbar, offset ->
            val expendedOffset = offset.coerceAtLeast(0).toFloat()
            val minCollapsedOffset = behavior.getCollapsedOffset(parent as View, appbar)
            val maxExpendedOffset = behavior.getFullyExpendOffset(parent as View, appbar)
            if (minCollapsedOffset >= 0 || maxExpendedOffset <= 0)
                return@addOnOffsetExpendChangedListener

            val expendedPercent = (expendedOffset / maxExpendedOffset.toFloat())
                .coerceIn(0F, 1F)
            val alphaPercent = calculatePercentIn(
                start = minCollapsedOffset,
                end = if (shouldSkipExpanded()) maxExpendedOffset else 0,
                num = offset
            ).toFloat()

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
                it.alpha = alphaPercent
            }

            mCollapsingToolbarLayout?.let {
                it.translationY = topOffset
                it.expendedTitleColor =
                    Color.argb((alphaPercentDecrease * 255).toInt(), 255, 255, 255)
            }

            mToolbar?.let {
                it.visibility = if (alphaPercentDecrease <= 0.05) INVISIBLE else VISIBLE
                it.alpha = alphaPercentDecrease
            }

            mLyricViewX?.let {
                it.alpha = alphaPercentIncrease
            }

            mEdgeTransparentView?.let {
                it.alpha = alphaPercentIncrease
            }
        }
        behavior.addOnStateChangeListener(StateHelper.OnScrollToThresholdListener {
            HapticUtils.haptic(
                this@SquareAppbarLayout,
                HapticUtils.Strength.HAPTIC_STRONG
            )
        })
    }
}