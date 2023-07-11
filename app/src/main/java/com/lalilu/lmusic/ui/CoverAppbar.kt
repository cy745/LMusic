package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import com.lalilu.R

class CoverAppbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), AttachedBehavior {
    private val behaviorInternal = AppbarBehavior(this)

    var minAnchorHeight: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 96f, context.resources.displayMetrics
    ).toInt()
        private set
    var middleAnchorHeight: Int = 0
        private set
    var maxAnchorHeight: Int = 0
        private set
    var aspectRatio: Float = 1f
        set(value) {
            if (field == value) return
            field = value
            applyAspectRatio(width)
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CoverAppbar).run {
            aspectRatio = getFloat(R.styleable.CoverAppbar_aspect_ratio, 1f)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        applyAspectRatio(measuredWidth)

        maxAnchorHeight = maxOf(
            (parent as? ViewGroup)?.measuredHeight ?: 0,
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    private fun applyAspectRatio(width: Int) {
        middleAnchorHeight = (width / aspectRatio).toInt()
        behaviorInternal.positionHelper.onViewLayout(fromOutside = true)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = behaviorInternal
}