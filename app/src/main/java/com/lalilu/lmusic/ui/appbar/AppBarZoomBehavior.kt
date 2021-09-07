package com.lalilu.lmusic.ui.appbar

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY
import androidx.dynamicanimation.animation.SpringForce.STIFFNESS_LOW
import com.dirror.lyricviewx.LyricViewX
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.R
import com.lalilu.lmusic.ui.PaletteDraweeView
import com.lalilu.lmusic.ui.appbar.AppBarOnStateChangeListener.Companion.STATE_EXPANDED
import com.lalilu.lmusic.utils.Mathf
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout


class AppBarZoomBehavior(private val context: Context, attrs: AttributeSet? = null) :
    AppBarLayout.Behavior(context, attrs) {

    private var mDraweeView: PaletteDraweeView? = null
    private var mSpringAnimation: SpringAnimation? = null
    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var mLyricViewX: LyricViewX? = null

    private var mAppbarHeight = -1
    private var mDraweeHeight = -1
    private var maxExpandHeight = 200
    private var maxDragHeight = 200

    private var fullyExpend = false
    private var mAppbarState = STATE_EXPANDED

    /**
     *  在布局子控件时进行初始化
     */
    override fun onLayoutChild(
        parent: CoordinatorLayout,
        abl: AppBarLayout,
        layoutDirection: Int
    ): Boolean {
        initialize(parent, abl)
        return super.onLayoutChild(parent, abl, layoutDirection)
    }

    /**
     *  初始化各个子控件,获取基础的长宽变量
     */
    private fun initialize(parent: CoordinatorLayout, appBarLayout: AppBarLayout) {
        appBarLayout.clipChildren = false
        appBarLayout.addOnOffsetChangedListener(object : AppBarOnStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: Int) {
                mAppbarState = state
            }

            override fun onStatePercentage(percent: Float) {
                mDraweeView?.let { it.alpha = percent }
            }
        })

        mDraweeView = appBarLayout.findViewById(R.id.fm_top_pic)
        mCollapsingToolbarLayout = appBarLayout.findViewById(R.id.fm_collapse_layout)
        mLyricViewX = appBarLayout.findViewById(R.id.fm_lyric_view_x)
        nestedChildView = (parent.getChildAt(1) as ViewGroup).getChildAt(0) as ViewGroup?

        // 修复 Appbar 收起后无法再次展开的问题
        mAppbarHeight =
            if (mAppbarHeight == -1) appBarLayout.height - appBarLayout.totalScrollRange else mAppbarHeight
        mDraweeHeight = mDraweeView?.height ?: 0

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        maxExpandHeight = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val height = windowManager.currentWindowMetrics.bounds.height()
                height - mDraweeHeight
            }
            else -> {
                val outMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getRealMetrics(outMetrics)
                outMetrics.heightPixels - mDraweeHeight
            }
        }
    }

    private var lastX = -1F
    private var lastY = -1F
    private var nestedChildView: ViewGroup? = null

    /**
     *  记录 AppBar 区域上手指的滑动，并传递给 Appbar 的 child 使其模拟嵌套滑动
     */
    override fun onTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        nestedChildView ?: return super.onTouchEvent(parent, child, ev)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.rawY
                lastX = ev.rawX
                nestedChildView!!.startNestedScroll(
                    ViewCompat.SCROLL_AXIS_VERTICAL
                )
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = -(ev.rawY - lastY).toInt()
                nestedChildScrollBy(nestedChildView!!, dy)
                lastY = ev.rawY
                lastX = ev.rawX
            }
            MotionEvent.ACTION_UP -> {
                nestedChildView!!.stopNestedScroll()
                lastX = -1F
                lastY = -1F
            }
        }
        return true
    }

    /**
     *  模拟子控件嵌套滚动
     */
    private fun nestedChildScrollBy(nestedChildView: ViewGroup, dy: Int) {
        nestedChildView.dispatchNestedPreScroll(0, dy, null, null)
        nestedChildView.dispatchNestedScroll(0, 0, 0, dy, null)
    }

    /**
     *  拖动子控件,嵌套滚动发生时的周期函数
     */
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout, child: AppBarLayout,
        target: View, dx: Int, dy: Int, consumed: IntArray, type: Int
    ) {
        val isAppbarHidden = child.y + child.totalScrollRange <= 0
        val isAppbarMaxExpend = mAppbarState == STATE_EXPANDED

        // 计算此事件中appbar的bottom应该到达的下一个位置
        var nextPosition = child.bottom - dy
        val offsetPosition = child.bottom - mDraweeHeight

        // 判断专辑封面是否已展开
        if (!isAppbarHidden && isAppbarMaxExpend && nextPosition > mAppbarHeight) {

            // 判断这次事件是否需要展开 mDraweeView
            if (child.bottom >= mDraweeHeight && dy < 0) {

                // 根据所在位置的百分比为滑动添加阻尼
                // 经过阻尼衰减得到正确的 nextPosition
                val percent = 1 - offsetPosition / maxDragHeight.toFloat()
                if (percent in 0F..1F) nextPosition = child.bottom - (dy * percent).toInt()
            }

            // 通过 nextPosition 重新定位
            resizeChild(child, nextPosition)
            mSpringAnimation?.cancel()

            val isNeedConsume = nextPosition in mDraweeHeight..mDraweeHeight + maxExpandHeight
            consumed[1] = if (isNeedConsume) Int.MAX_VALUE else 0

            if (child.bottom <= mDraweeHeight) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            }
        } else {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }


    /**
     *  重新定位各个控件的大小和位置
     */
    private fun resizeChild(abl: AppBarLayout, nextPosition: Int) {
        val appbar = (abl as SquareAppBarLayout)

        // 限定 appbar 的高度在指定范围内
        val bottom = Mathf.clamp(mDraweeHeight, mDraweeHeight + maxExpandHeight, nextPosition)
        appbar.bottom = bottom
        appbar.mBottom = bottom

        val offsetPosition = abl.bottom - mDraweeHeight
        val scaleValue = abl.bottom / mDraweeHeight.toFloat()
        val animatePercent = Mathf.clamp(0F, 1F, offsetPosition / maxExpandHeight.toFloat())


        mDraweeView?.blurBg(animatePercent)
        mDraweeView?.scaleX = scaleValue
        mDraweeView?.scaleY = scaleValue
        mDraweeView?.translationY = offsetPosition / 2f

        val value = if (animatePercent in 0F..0.5F) animatePercent else 1 - animatePercent
        mCollapsingToolbarLayout?.top = (maxExpandHeight / 2 * value).toInt()
        mCollapsingToolbarLayout?.bottom =
            (mDraweeHeight + maxExpandHeight * animatePercent).toInt()

        mLyricViewX?.top = (maxExpandHeight / 4 * animatePercent).toInt()
        mLyricViewX?.bottom = (mDraweeHeight + maxExpandHeight / 2 * animatePercent).toInt()

        // 文字透明过渡插值器
        val interpolation = AccelerateDecelerateInterpolator().getInterpolation(animatePercent)
        val alphaPercentDecrease = (1F - interpolation * 2).coerceAtLeast(0F)
        val alphaPercentIncrease = (2 * interpolation - 1F).coerceAtLeast(0F)
        mCollapsingToolbarLayout?.textAlpha((alphaPercentDecrease * 255).toInt())
        mLyricViewX?.alpha = alphaPercentIncrease

        // 根据 offsetPosition 计算出拖动时与最近边距离的百分比
        val dragPercent = when (offsetPosition) {
            in 0..maxDragHeight -> offsetPosition / maxDragHeight.toFloat()
            in (maxExpandHeight - maxDragHeight)..maxExpandHeight -> {
                (offsetPosition - maxExpandHeight + maxDragHeight) / maxDragHeight.toFloat() - 1F
            }
            else -> Float.NaN
        }

        if (dragPercent.isNaN()) return
        when {
            dragPercent in 0.05F..0.6F && fullyExpend -> fullyExpend = false
            dragPercent in 0.6F..1F && !fullyExpend -> {
                fullyExpend = true
                onDragToBottom(abl)
            }
            dragPercent in -1F..-0.6F && fullyExpend -> {
                fullyExpend = false
                onDragToTop(abl)
            }
            dragPercent in -0.6F..-0.05F && !fullyExpend -> fullyExpend = true

        }
        appbar.fullyExpend = fullyExpend
    }

    private fun onDragToTop(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun onDragToBottom(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
        } else {
            view.performHapticFeedback(31011)
        }
    }

    /**
     *  嵌套滚动结束时,根据就近边恢复位置
     */
    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        abl: AppBarLayout,
        target: View,
        type: Int
    ) {
        if (abl.bottom > mDraweeHeight) {
            var toPosition = 0
            toPosition += mDraweeHeight
            toPosition += if (fullyExpend) maxExpandHeight else 0
            recoveryToPosition(abl, toPosition)
        }
        super.onStopNestedScroll(coordinatorLayout, abl, target, type)
    }

    /**
     *  回复到任意位置的方法
     */
    private fun recoveryToPosition(abl: AppBarLayout, position: Number) {
        if (mSpringAnimation == null) {
            mSpringAnimation =
                SpringAnimation(abl, appBarLayoutFloatProperty, mDraweeHeight.toFloat()).apply {
                    this.spring.dampingRatio = DAMPING_RATIO_NO_BOUNCY
                    this.spring.stiffness = STIFFNESS_LOW
                }
        }
        mSpringAnimation!!.cancel()
        mSpringAnimation!!.animateToFinalPosition(position.toFloat())
    }

    private val appBarLayoutFloatProperty =
        object : FloatPropertyCompat<AppBarLayout>("appbar_bottom") {
            override fun setValue(`object`: AppBarLayout?, value: Float) {
                `object`?.let {
                    resizeChild(`object`, value.toInt())
                }
            }

            override fun getValue(`object`: AppBarLayout?): Float {
                return `object`?.bottom?.toFloat() ?: 0f
            }
        }
}