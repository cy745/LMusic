package com.lalilu.lmusic.utils

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.FloatProperty
import android.view.View
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY
import androidx.dynamicanimation.animation.SpringForce.STIFFNESS_LOW
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.common.Mathf
import com.lalilu.lmusic.AppBarOnStateChange
import com.lalilu.lmusic.AppBarOnStateChange.AppBarState
import com.lalilu.lmusic.R
import com.lalilu.lmusic.ui.PaletteDraweeView
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout


class AppBarZoomBehavior(context: Context, attrs: AttributeSet) :
    AppBarLayout.Behavior(context, attrs) {
    companion object {
        const val MAX_ZOOM_HEIGHT = 200
    }

    private var mDraweeView: PaletteDraweeView? = null
    private var mSpringAnimation: SpringAnimation? = null
    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null

    private var mAppbarHeight = -1          //记录AppbarLayout原始高度
    private var mDraweeHeight = -1          //记录ImageView原始高度
    private var mAppbarState = AppBarState.STATE_EXPANDED

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        abl: AppBarLayout,
        layoutDirection: Int
    ): Boolean {
        initialize(abl)
        return super.onLayoutChild(parent, abl, layoutDirection)
    }

    private fun initialize(appBarLayout: AppBarLayout) {
        appBarLayout.clipChildren = false

        appBarLayout.addOnOffsetChangedListener(object : AppBarOnStateChange() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: AppBarState) {
                mAppbarState = state
            }
        })

        mAppbarHeight = appBarLayout.height - appBarLayout.totalScrollRange
        mDraweeView = appBarLayout.findViewById(R.id.playing_song_album_pic)
        mCollapsingToolbarLayout = appBarLayout.findViewById(R.id.collapsingToolbarLayout)

        mDraweeView?.let { mDraweeHeight = it.height }
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val nextPosition = child.bottom - dy
        if (mAppbarState == AppBarState.STATE_EXPANDED && nextPosition > mAppbarHeight) {
            zoomDrawee(child, nextPosition)
            mSpringAnimation?.cancel()

            consumed[1] = when {
                nextPosition < mDraweeHeight -> {
                    dy - (mDraweeHeight - nextPosition)
                }
                nextPosition > mDraweeHeight + MAX_ZOOM_HEIGHT -> {
                    dy + (nextPosition - (mDraweeHeight + MAX_ZOOM_HEIGHT))
                }
                else -> dy
            }
            if (child.bottom <= mDraweeHeight) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            }
        } else {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    private fun zoomDrawee(abl: AppBarLayout, nextPosition: Int) {
        abl.bottom =
            Mathf.clamp(mDraweeHeight, mDraweeHeight + MAX_ZOOM_HEIGHT, nextPosition)

        val nowPosition = abl.bottom
        val movedDistance = nowPosition - mDraweeHeight
        val animatePercent = movedDistance / MAX_ZOOM_HEIGHT.toFloat()
        val scaleValue = 1 + animatePercent * 0.3f
        mDraweeView?.scaleX = scaleValue
        mDraweeView?.scaleY = scaleValue

        mCollapsingToolbarLayout?.translationY = movedDistance / 2f
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        abl: AppBarLayout,
        target: View,
        type: Int
    ) {
        if (abl.bottom > mDraweeHeight) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recoveryToPosition(abl, mDraweeHeight)
            }
        }
        super.onStopNestedScroll(coordinatorLayout, abl, target, type)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun recoveryToPosition(abl: AppBarLayout, position: Number) {
        if (mSpringAnimation == null) {
            mSpringAnimation =
                SpringAnimation(abl, appBarLayoutViewProperty, mDraweeHeight.toFloat()).apply {
                    this.spring.dampingRatio = DAMPING_RATIO_NO_BOUNCY
                    this.spring.stiffness = STIFFNESS_LOW
                }
        }
        mSpringAnimation!!.cancel()
        mSpringAnimation!!.animateToFinalPosition(position.toFloat())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private val appBarLayoutFloatProperty =
        object : FloatProperty<AppBarLayout>("appbar_bottom") {
            override fun get(`object`: AppBarLayout?): Float {
                return `object`?.bottom?.toFloat() ?: 0f
            }

            override fun setValue(`object`: AppBarLayout?, value: Float) {
                `object`?.let {
                    zoomDrawee(`object`, value.toInt())
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    private val appBarLayoutViewProperty =
        ViewProperty.createFloatPropertyCompat(appBarLayoutFloatProperty)
}