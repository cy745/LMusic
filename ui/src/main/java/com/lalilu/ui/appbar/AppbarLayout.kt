package com.lalilu.ui.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.core.math.MathUtils
import androidx.core.util.ObjectsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lalilu.ui.R
import com.lalilu.ui.internal.StateHelper.Companion.STATE_EXPENDED
import com.lalilu.ui.internal.StateHelper.Companion.STATE_MIDDLE
import kotlin.math.abs
import kotlin.math.max

@SuppressLint("PrivateResource", "CustomViewStyleable")
open class AppbarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), AttachedBehavior {

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return MyAppbarBehavior(context, null)
    }

    fun interface OnOffsetChangedListener {
        fun onOffsetChanged(appbarLayout: AppbarLayout, verticalOffset: Int)
    }

    /**
     * 原AppbarLayout计算可用滚动距离，作为上半部的数据源
     */
    var totalScrollRange = INVALID_SCROLL_RANGE
        get() {
            if (field != INVALID_SCROLL_RANGE) return field

            var range = 0
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val lp = view.layoutParams as LayoutParams
                val childHeight = view.measuredHeight
                val flags = lp.scrollFlags

                if ((flags and LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                    range += childHeight + lp.topMargin + lp.bottomMargin
                    if (i == 0 && ViewCompat.getFitsSystemWindows(view)) {
                        range -= topInset
                    }
                    if (flags and LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED != 0) {
                        range -= ViewCompat.getMinimumHeight(view)
                        break
                    }
                } else {
                    break
                }
            }
            return max(range, 0).also { field = it }
        }

    var haveChildWithInterpolator: Boolean = false
    val hasScrollableChildren: Boolean
        get() = totalScrollRange != 0

    private var lastInsets: WindowInsetsCompat? = null
    private var listeners: MutableList<OnOffsetChangedListener>? = null
    var pendingAction = PENDING_ACTION_NONE
        private set

    @VisibleForTesting
    private val topInset: Int
        get() = if (lastInsets != null)
            lastInsets!!.getInsets(WindowInsetsCompat.Type.statusBars()).top
        else 0

    fun addOnOffsetChangedListener(listener: OnOffsetChangedListener?) {
        listeners = listeners ?: ArrayList()
        if (listener != null && !listeners!!.contains(listener)) {
            listeners!!.add(listener)
        }
    }

    fun removeOnOffsetChangedListener(listener: OnOffsetChangedListener?) {
        if (listeners != null && listener != null) {
            listeners!!.remove(listener)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode != MeasureSpec.EXACTLY &&
            ViewCompat.getFitsSystemWindows(this) &&
            shouldOffsetFirstChild()
        ) {
            var newHeight = measuredHeight
            newHeight += when (heightMode) {
                MeasureSpec.AT_MOST -> MathUtils.clamp(
                    measuredHeight + topInset, 0, MeasureSpec.getSize(heightMeasureSpec)
                )
                MeasureSpec.UNSPECIFIED -> topInset
                else -> 0
            }
            setMeasuredDimension(measuredWidth, newHeight)
        }
        invalidateScrollRanges()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
            // If we need to offset the first child, we need to offset all of them to make space
            for (z in childCount - 1 downTo 0) {
                ViewCompat.offsetTopAndBottom(getChildAt(z), topInset)
            }
        }

        invalidateScrollRanges()
        haveChildWithInterpolator = false

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childLp = child.layoutParams as LayoutParams
            val interpolator = childLp.scrollInterpolator
            if (interpolator != null) {
                haveChildWithInterpolator = true
                break
            }
        }
    }

    private fun invalidateScrollRanges() {
        totalScrollRange = INVALID_SCROLL_RANGE
    }

    fun autoToggleExpand() {
        if (shouldSkipExpanded()) {
            setExpanded(!isExpanded(), true)
        } else {
            setExpanded(true, !isFullyExpanded())
        }
    }

    fun shouldSkipExpanded(): Boolean {
        if (behavior is MyAppbarBehavior) {
            return (behavior as MyAppbarBehavior).shouldSkipExpandedState(this)
        }
        return false
    }

    fun isFullyExpanded(): Boolean {
        if (behavior is MyAppbarBehavior) {
            return (behavior as MyAppbarBehavior).stateHelper.nowState >= STATE_MIDDLE
        }
        return false
    }

    fun isExpanded(): Boolean {
        if (behavior is MyAppbarBehavior) {
            return (behavior as MyAppbarBehavior).stateHelper.nowState >= STATE_EXPENDED
        }
        return false
    }

    fun setExpanded(
        action: Int,
        animate: Boolean = ViewCompat.isLaidOut(this),
        force: Boolean = true
    ) {
        pendingAction = action or
                (if (animate) PENDING_ACTION_ANIMATE_ENABLED else 0) or
                (if (force) PENDING_ACTION_FORCE else 0)
        requestLayout()
    }

    fun setExpanded(
        expanded: Boolean,
        fully: Boolean = false,
        animate: Boolean = ViewCompat.isLaidOut(this),
        force: Boolean = true
    ) = setExpanded(
        if (expanded)
            (if (fully) PENDING_ACTION_FULLY_EXPANDED else PENDING_ACTION_EXPANDED)
        else PENDING_ACTION_COLLAPSED,
        animate, force
    )

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p)
    }

    fun onOffsetChanged(offset: Int) {
        if (!willNotDraw()) {
            ViewCompat.postInvalidateOnAnimation(this)
        }

        listeners?.forEach {
            it.onOffsetChanged(this, offset)
        }
    }

    fun resetPendingAction() {
        pendingAction = PENDING_ACTION_NONE
    }

    private fun shouldOffsetFirstChild(): Boolean {
        if (childCount > 0) {
            val firstChild = getChildAt(0)
            return firstChild.visibility != GONE && !ViewCompat.getFitsSystemWindows(firstChild)
        }
        return false
    }

    private fun onWindowInsetChanged(insets: WindowInsetsCompat?): WindowInsetsCompat? {
        var newInsets: WindowInsetsCompat? = null
        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets
        }

        // If our insets have changed, keep them and trigger a layout...
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets
            setWillNotDraw(true)
            requestLayout()
        }
        return insets
    }

    class LayoutParams : LinearLayout.LayoutParams {
        @IntDef(
            flag = true,
            value = [
                SCROLL_FLAG_NO_SCROLL,
                SCROLL_FLAG_SCROLL,
                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
                SCROLL_FLAG_ENTER_ALWAYS,
                SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
                SCROLL_FLAG_SNAP,
                SCROLL_FLAG_SNAP_MARGINS
            ]
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class ScrollFlags

        @ScrollFlags
        var scrollFlags = SCROLL_FLAG_SCROLL
        var scrollEffect: ChildScrollEffect? = null
        var scrollInterpolator: Interpolator? = null
        val isCollapsible: Boolean
            get() = (scrollFlags and SCROLL_FLAG_SCROLL == SCROLL_FLAG_SCROLL
                    && scrollFlags and COLLAPSIBLE_FLAGS != 0)

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_Layout)
            scrollFlags = a.getInt(R.styleable.AppBarLayout_Layout_layout_scrollFlags, 0)
            scrollEffect = createScrollEffectFromInt(
                a.getInt(
                    R.styleable.AppBarLayout_Layout_layout_scrollEffect, SCROLL_EFFECT_NONE
                )
            )
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, weight: Float) : super(width, height, weight)
        constructor(p: ViewGroup.LayoutParams?) : super(p)
        constructor(source: MarginLayoutParams?) : super(source)

        @RequiresApi(19)
        constructor(source: LinearLayout.LayoutParams?) : super(source)

        @RequiresApi(19)
        constructor(source: LayoutParams) : super(source) {
            scrollFlags = source.scrollFlags
            scrollInterpolator = source.scrollInterpolator
        }

        private fun createScrollEffectFromInt(scrollEffectInt: Int): ChildScrollEffect? {
            return when (scrollEffectInt) {
                SCROLL_EFFECT_COMPRESS -> CompressChildScrollEffect()
                else -> null
            }
        }

        companion object {
            const val SCROLL_FLAG_NO_SCROLL = 0x0
            const val SCROLL_FLAG_SCROLL = 0x1
            const val SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 1 shl 1
            const val SCROLL_FLAG_ENTER_ALWAYS = 1 shl 2
            const val SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 1 shl 3
            const val SCROLL_FLAG_SNAP = 1 shl 4
            const val SCROLL_FLAG_SNAP_MARGINS = 1 shl 5

            const val FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
            const val FLAG_SNAP = SCROLL_FLAG_SCROLL or SCROLL_FLAG_SNAP
            const val COLLAPSIBLE_FLAGS =
                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED

            private const val SCROLL_EFFECT_NONE = 0
            private const val SCROLL_EFFECT_COMPRESS = 1
        }
    }

    abstract class ChildScrollEffect {
        abstract fun onOffsetChanged(
            appbarLayout: AppbarLayout, child: View, offset: Float
        )
    }

    class CompressChildScrollEffect : ChildScrollEffect() {
        private val relativeRect = Rect()
        private val ghostRect = Rect()
        override fun onOffsetChanged(
            appbarLayout: AppbarLayout, child: View, offset: Float
        ) {
            updateRelativeRect(relativeRect, appbarLayout, child)
            val distanceFromCeiling = relativeRect.top - abs(offset)
            // If the view is at the ceiling, begin the compress animation.
            if (distanceFromCeiling <= 0f) {
                // The "compressed" progress. When p = 0, the top of the child is at the top of the ceiling
                // (uncompressed). When p = 1, the bottom of the child is at the top of the ceiling
                // (fully compressed).
                val p =
                    MathUtils.clamp(abs(distanceFromCeiling / relativeRect.height()), 0f, 1f)

                // Set offsetY to the full distance from ceiling to keep the child exactly in place.
                var offsetY = -distanceFromCeiling

                // Decrease the offsetY so the child moves with the app bar parent. Here, it will move a
                // total of the child's height times the compress distance factor but will do so with an
                // eased-out value - moving at a near 1:1 speed with the app bar at first and slowing down
                // as it approaches the ceiling (p = 1).
                val easeOutQuad = 1f - (1f - p) * (1f - p)
                val distance = relativeRect.height() * COMPRESS_DISTANCE_FACTOR
                offsetY -= distance * easeOutQuad

                // Translate the view to create a parallax effect, letting the ghost clip when out of
                // bounds.
                child.translationY = offsetY

                // Use a rect to clip the child by its original bounds before it is given a
                // translation (compress effect). This masks and ensures the child doesn't overlap other
                // children inside the ABL.
                child.getDrawingRect(ghostRect)
                ghostRect.offset(0, (-offsetY).toInt())
                ViewCompat.setClipBounds(child, ghostRect)
            } else {
                // Reset both the clip bounds and translationY of this view
                ViewCompat.setClipBounds(child, null)
                child.translationY = 0f
            }
        }

        companion object {
            private const val COMPRESS_DISTANCE_FACTOR = .3f
            private fun updateRelativeRect(rect: Rect, appbarLayout: AppbarLayout, child: View) {
                child.getDrawingRect(rect)
                appbarLayout.offsetDescendantRectToMyCoords(child, rect)
                rect.offset(0, -appbarLayout.topInset)
            }
        }
    }

    companion object {
        const val PENDING_ACTION_NONE = 0x0
        const val PENDING_ACTION_COLLAPSED = 0x1
        const val PENDING_ACTION_EXPANDED = 0x1 shl 1
        const val PENDING_ACTION_FULLY_EXPANDED = 0x1 shl 2
        const val PENDING_ACTION_ANIMATE_ENABLED = 0x1 shl 3
        const val PENDING_ACTION_FORCE = 0x1 shl 4
        private const val INVALID_SCROLL_RANGE = -1
    }

    init {
        orientation = VERTICAL
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.AppbarLayout, defStyleAttr, R.style.AppbarLayout_Base
        )

        background = a.getDrawable(R.styleable.AppbarLayout_android_background)
        if (a.hasValue(R.styleable.AppbarLayout_appbar_expanded)) {
            setExpanded(a.getBoolean(R.styleable.AppbarLayout_appbar_expanded, false), false, false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (a.hasValue(R.styleable.AppbarLayout_android_keyboardNavigationCluster)) {
                this.isKeyboardNavigationCluster =
                    a.getBoolean(R.styleable.AppbarLayout_android_keyboardNavigationCluster, false)
            }
            if (a.hasValue(R.styleable.AppbarLayout_android_touchscreenBlocksFocus)) {
                this.touchscreenBlocksFocus =
                    a.getBoolean(R.styleable.AppbarLayout_android_touchscreenBlocksFocus, false)
            }
        }
        a.recycle()
        ViewCompat.setOnApplyWindowInsetsListener(this.rootView) { _, insets ->
            onWindowInsetChanged(insets) ?: insets
        }
    }
}