package com.lalilu.lmusic.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import com.lalilu.R

class CoverAppbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : LinearLayout(context, attrs), AttachedBehavior {
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
        private set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CoverAppbar).run {
            aspectRatio = getFloat(R.styleable.CoverAppbar_aspect_ratio, 1f)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (aspectRatio == 1f) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        } else {
            val width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpec = MeasureSpec.makeMeasureSpec((width / aspectRatio).toInt(), heightMode)
            super.onMeasure(widthMeasureSpec, heightSpec)
        }
        middleAnchorHeight = measuredHeight
        maxAnchorHeight = maxOf(
            (parent as? ViewGroup)?.measuredHeight ?: 0,
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = behaviorInternal
}