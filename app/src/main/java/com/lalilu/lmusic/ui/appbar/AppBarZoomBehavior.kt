package com.lalilu.lmusic.ui.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY
import androidx.dynamicanimation.animation.SpringForce.STIFFNESS_LOW
import com.dirror.lyricviewx.LyricViewX
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.lalilu.R
import com.lalilu.lmusic.ui.PaletteDraweeView
import com.lalilu.lmusic.ui.appbar.AppBarStatusHelper.fullyExpend
import com.lalilu.lmusic.ui.appbar.AppBarStatusHelper.maxDragHeight
import com.lalilu.lmusic.ui.appbar.AppBarStatusHelper.maxExpandHeight
import com.lalilu.lmusic.ui.appbar.AppBarStatusHelper.normalHeight
import com.lalilu.lmusic.utils.DeviceUtil
import com.lalilu.lmusic.utils.Mathf


class AppBarZoomBehavior(private val context: Context, attrs: AttributeSet? = null) :
    AppBarLayout.Behavior(context, attrs), GestureDetector.OnGestureListener {

    private var mLyricViewX: LyricViewX? = null
    private var mDraweeView: PaletteDraweeView? = null
    private var nestedChildView: ViewGroup? = null
    private var mSpringAnimation: SpringAnimation? = null
    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null

    private var gestureDetector = GestureDetectorCompat(context, this)
    private lateinit var appBarStatusHelper: AppBarStatusHelper

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

        mDraweeView = appBarLayout.findViewById(R.id.fm_top_pic)
        mLyricViewX = appBarLayout.findViewById(R.id.fm_lyric_view_x)
        nestedChildView = parent.getChildAt(1) as ViewGroup
        mCollapsingToolbarLayout = appBarLayout.findViewById(R.id.fm_collapse_layout)
        mLyricViewX?.let {
            val temp = it.layoutParams
            temp.height = DeviceUtil.getHeight(context)
            it.layoutParams = temp
        }

        normalHeight = mDraweeView?.height ?: -1
        appBarStatusHelper = AppBarStatusHelper.initial(appBarLayout) { percent ->
            mDraweeView?.let { it.alpha = percent }
        }
        maxExpandHeight = DeviceUtil.getHeight(context) - normalHeight
    }

    /**
     *  记录 AppBar 区域上手指的滑动，并传递给 Appbar 的 child 使其模拟嵌套滑动
     */
    override fun onTouchEvent(
        parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent
    ): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) nestedChildView?.stopNestedScroll()
        return gestureDetector.onTouchEvent(ev)
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
        // 计算此事件中 appbar 的 bottom 应该到达的下一个位置
        var nextPosition = child.bottom - dy
        val offsetPosition = child.bottom - appBarStatusHelper.normalHeight

        // 获取现在所处位置的状态 Status，决定是否需要使 Appbar 拉伸
        if (appBarStatusHelper.getNextStatusByNextPosition(nextPosition) == AppBarStatus.STATUS_EXPENDED) {
            if (dy < 0) {
                // 根据所在位置的百分比为滑动添加阻尼
                // 经过阻尼衰减得到正确的 nextPosition
                val percent = 1 - offsetPosition / maxDragHeight.toFloat()
                if (percent in 0F..1F) nextPosition = child.bottom - (dy * percent).toInt()
            }

            // 通过 nextPosition 重新定位
            resizeChild(child, nextPosition)
            mSpringAnimation?.cancel()

            val isNeedConsume = nextPosition in normalHeight..normalHeight + maxExpandHeight
            consumed[1] = if (isNeedConsume) Int.MAX_VALUE else 0

            if (child.bottom > normalHeight) return
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }


    /**
     *  重新定位各个控件的大小和位置
     */
    @SuppressLint("RestrictedApi")
    private fun resizeChild(abl: AppBarLayout, nextPosition: Int) {
        val appbar = (abl as SquareAppBarLayout)

        // 限定 appbar 的高度在指定范围内
        val bottom = Mathf.clamp(normalHeight, normalHeight + maxExpandHeight, nextPosition)
        appbar.bottom = bottom
        appbar.mBottom = bottom

        val offsetPosition = abl.bottom - normalHeight
        val scaleValue = abl.bottom / normalHeight.toFloat()
        val animatePercent = Mathf.clamp(0F, 1F, offsetPosition / maxExpandHeight.toFloat())


        mDraweeView?.blurBg(animatePercent)
        mDraweeView?.scaleX = scaleValue
        mDraweeView?.scaleY = scaleValue
        mDraweeView?.translationY = offsetPosition / 2f

        val value = if (animatePercent in 0F..0.5F) animatePercent else 1 - animatePercent
        mCollapsingToolbarLayout?.top = (maxExpandHeight / 2 * value).toInt()
        mCollapsingToolbarLayout?.bottom =
            (normalHeight + maxExpandHeight * animatePercent).toInt()

//        mLyricViewX?.top = (maxExpandHeight / 4 * animatePercent).toInt()
//        mLyricViewX?.bottom = (normalHeight + maxExpandHeight / 2 * animatePercent).toInt()

        // 文字透明过渡插值器
        val interpolation = AccelerateDecelerateInterpolator().getInterpolation(animatePercent)
        val alphaPercentDecrease = (1F - interpolation * 2).coerceAtLeast(0F)
        val alphaPercentIncrease = (2 * interpolation - 1F).coerceAtLeast(0F)
        mCollapsingToolbarLayout?.collapsingTextHelper?.textAlphaHelper?.updateTextAlpha((alphaPercentDecrease * 255).toInt())
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
        if (abl.bottom > appBarStatusHelper.normalHeight) {
            var toPosition = 0
            toPosition += appBarStatusHelper.normalHeight
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
                SpringAnimation(abl, appBarLayoutFloatProperty, normalHeight.toFloat()).apply {
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

    override fun onDown(e: MotionEvent?): Boolean {
        nestedChildView?.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean = false

    override fun onScroll(
        e1: MotionEvent?, e2: MotionEvent?,
        distanceX: Float, distanceY: Float
    ): Boolean {
        nestedChildView?.let { nestedChildScrollBy(it, distanceY.toInt()) }
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent?,
        velocityX: Float, velocityY: Float
    ): Boolean {
        nestedChildView?.stopNestedScroll()
        return true
    }
}