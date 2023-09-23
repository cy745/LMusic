package com.lalilu.ui.appbar

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.core.view.children
import com.lalilu.ui.R

class CoverAppbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), AttachedBehavior {
    private val behaviorInternal = AppbarBehavior(this)

    var minAnchorHeight: Int = 0
        private set
    var middleAnchorHeight: Int = 0
        private set
    var maxAnchorHeight: Int = 0
        private set
    var aspectRatio: Float = 1f
        private set
    var dragThreshold: Int = 120
        private set

    private var minHeightAnchorId: Int = -1

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CoverAppbar).run {
            aspectRatio = getFloat(R.styleable.CoverAppbar_aspect_ratio, 1f)
            minHeightAnchorId = getResourceId(R.styleable.CoverAppbar_min_height_anchor, -1)
            recycle()
        }
        minAnchorHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 96f, context.resources.displayMetrics
        ).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        this.findChildViewWithId(minHeightAnchorId)?.let {
            minAnchorHeight = it.measuredHeight
        }

        applyHeightWithAspectRatio(measuredWidth)

        maxAnchorHeight = maxOf(
            (parent as? ViewGroup)?.measuredHeight ?: 0,
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    fun updateAspectRatio(aspectRatio: Float) {
        if (this.aspectRatio == aspectRatio) return
        this.aspectRatio = aspectRatio
        applyHeightWithAspectRatio(width)
    }

    private fun applyHeightWithAspectRatio(width: Int) {
        val newHeight = (width / aspectRatio).toInt()
        if (middleAnchorHeight == newHeight) return
        middleAnchorHeight = newHeight

        if (behaviorInternal.positionHelper.isValueValidNow()) {
            behaviorInternal.positionHelper.snapIfNeeded(fromUser = false)
        }
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = behaviorInternal

    private fun ViewGroup.findChildViewWithId(id: Int): View? {
        var result: View? = null

        for (child in children) {
            when {
                child.id == id -> result = child
                child is ViewGroup -> result = child.findChildViewWithId(id)
            }
            if (result != null) break
        }

        return result
    }
}