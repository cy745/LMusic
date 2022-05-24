package com.lalilu.ui.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.Toolbar
import androidx.core.math.MathUtils
import androidx.core.util.ObjectsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.internal.CollapsingTextHelper
import com.lalilu.ui.R
import com.lalilu.ui.internal.DescendantOffsetUtils
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("RestrictedApi", "CustomViewStyleable")
class CollapsingLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val collapsingTextHelper: CollapsingTextHelper = CollapsingTextHelper(this)

    companion object {
        const val TITLE_COLLAPSE_MODE_SCALE = 0
        const val TITLE_COLLAPSE_MODE_FADE = 1
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = [TITLE_COLLAPSE_MODE_SCALE, TITLE_COLLAPSE_MODE_FADE])
    @Retention(AnnotationRetention.SOURCE)
    annotation class TitleCollapseMode

    private var expandedMarginStart = 0
    private var expandedMarginTop = 0
    private var expandedMarginEnd = 0
    private var expandedMarginBottom = 0

    @TitleCollapseMode
    private var titleCollapseMode = 0
        set(value) {
            field = value
            collapsingTextHelper.setFadeModeEnabled(value == TITLE_COLLAPSE_MODE_FADE)
        }

    private var refreshToolbar = true
    private var toolbarId = 0
    private var toolbar: ViewGroup? = null
    private var toolbarDirectChild: View? = null
    private var dummyView: View? = null
    private val tmpRect = Rect()

    private var drawCollapsingTitle = false
    private var topInsetApplied = 0
    private var forceApplySystemWindowInsetTop = false
    private var extraMultilineHeight = 0
    private var extraMultilineHeightEnabled = false
    private var onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null
    private var lastInsets: WindowInsetsCompat? = null
    val insetTop: Int
        get() = lastInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0

    @ColorInt
    var expendedTitleColor: Int = 0
        set(value) {
            collapsingTextHelper.expandedTextColor = ColorStateList.valueOf(value)
            field = value
        }

    var collapsingTitleEnabled = false
        set(value) {
            if (value != field) {
                field = value
                updateContentDescriptionFromTitle()
                updateDummyView()
                requestLayout()
            }
        }

    var title: CharSequence?
        get() = if (collapsingTitleEnabled) collapsingTextHelper.text else null
        set(value) {
            collapsingTextHelper.text = value
            updateContentDescriptionFromTitle()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent is AppBarLayout) {
            val appBarLayout = parent as AppBarLayout

            // Copy over from the ABL whether we should fit system windows
            this.fitsSystemWindows = ViewCompat.getFitsSystemWindows(appBarLayout)
            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = OffsetUpdateListener()
            }
            appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener)

            // We're attached, so lets request an inset dispatch
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun onDetachedFromWindow() {
        val parent = parent
        if (onOffsetChangedListener != null && parent is AppBarLayout) {
            parent.removeOnOffsetChangedListener(onOffsetChangedListener)
        }
        super.onDetachedFromWindow()
    }

    private fun onWindowInsetChanged(insets: WindowInsetsCompat?): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null
        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets
            requestLayout()
        }

        // Consume the insets. This is done so that child views with fitSystemWindows=true do not
        // get the default padding functionality from View
        return WindowInsetsCompat.CONSUMED
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        ensureToolbar()
        if (collapsingTitleEnabled && drawCollapsingTitle) {
            collapsingTextHelper.draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec
        ensureToolbar()
        super.onMeasure(widthMeasureSpec, heightSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val topInset = insetTop
        if ((mode == MeasureSpec.UNSPECIFIED || forceApplySystemWindowInsetTop) && topInset > 0) {
            // If we have a top inset and we're set to wrap_content height or force apply,
            // we need to make sure we add the top inset to our height, therefore we re-measure
            topInsetApplied = topInset
            val newHeight = measuredHeight + topInset
            heightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, heightSpec)
        }
        if (extraMultilineHeightEnabled && collapsingTextHelper.maxLines > 1) {
            // Need to update title and bounds in order to calculate line count and text height.
            updateTitleFromToolbarIfNeeded()
            updateTextBounds(0, 0, measuredWidth, measuredHeight,  /* forceRecalculate= */true)
            val lineCount = collapsingTextHelper.expandedLineCount
            if (lineCount > 1) {
                // Add extra height based on the amount of height beyond the first line of title text.
                val expandedTextHeight = collapsingTextHelper.expandedTextFullHeight.roundToInt()
                extraMultilineHeight = expandedTextHeight * (lineCount - 1)
                val newHeight = measuredHeight + extraMultilineHeight
                heightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
                super.onMeasure(widthMeasureSpec, heightSpec)
            }
        }

        // Set our minimum height to enable proper AppBarLayout collapsing
        if (toolbar != null) {
            minimumHeight = if (toolbarDirectChild == null || toolbarDirectChild === this) {
                getHeightWithMargins(toolbar!!)
            } else {
                getHeightWithMargins(toolbarDirectChild!!)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (lastInsets != null) {
            // Shift down any views which are not set to fit system windows
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.top < insetTop) {
                        ViewCompat.offsetTopAndBottom(child, insetTop)
                    }
                }
            }
        }

        // Update our child view offset helpers so that they track the correct layout coordinates
        for (i in 0 until childCount) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout()
        }

        updateTextBounds(left, top, right, bottom, false)
        updateTitleFromToolbarIfNeeded()

        // Apply any view offsets, this should be done at the very end of layout
        for (i in 0 until childCount) {
            getViewOffsetHelper(getChildAt(i)).applyOffsets()
        }
    }

    private fun updateTextBounds(
        left: Int, top: Int,
        right: Int, bottom: Int,
        forceRecalculate: Boolean
    ) {
        // Update the collapsed bounds by getting its transformed bounds
        if (collapsingTitleEnabled && dummyView != null) {
            // We only draw the title if the dummy view is being displayed (Toolbar removes
            // views if there is no space)
            drawCollapsingTitle = ViewCompat.isAttachedToWindow(dummyView!!)
                    && dummyView!!.visibility == VISIBLE
            if (drawCollapsingTitle || forceRecalculate) {
                val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

                // Update the collapsed bounds
                updateCollapsedBounds(isRtl)

                // Update the expanded bounds
                collapsingTextHelper.setExpandedBounds(
                    if (isRtl) expandedMarginEnd else expandedMarginStart,
                    tmpRect.top + expandedMarginTop,
                    right - left - if (isRtl) expandedMarginStart else expandedMarginEnd,
                    bottom - top - expandedMarginBottom
                )

                // Now recalculate using the new bounds
                collapsingTextHelper.recalculate(forceRecalculate)
            }
        }
    }

    private fun updateCollapsedBounds(isRtl: Boolean) {
        val maxOffset =
            getMaxOffsetForPinChild((if (toolbarDirectChild != null) toolbarDirectChild else toolbar)!!)
        DescendantOffsetUtils.getDescendantRect(this, dummyView!!, tmpRect)
        val titleMarginStart: Int
        val titleMarginEnd: Int
        val titleMarginTop: Int
        val titleMarginBottom: Int
        if (toolbar is Toolbar) {
            val compatToolbar = toolbar as Toolbar
            titleMarginStart = compatToolbar.titleMarginStart
            titleMarginEnd = compatToolbar.titleMarginEnd
            titleMarginTop = compatToolbar.titleMarginTop
            titleMarginBottom = compatToolbar.titleMarginBottom
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && toolbar is android.widget.Toolbar) {
            val frameworkToolbar = toolbar as android.widget.Toolbar
            titleMarginStart = frameworkToolbar.titleMarginStart
            titleMarginEnd = frameworkToolbar.titleMarginEnd
            titleMarginTop = frameworkToolbar.titleMarginTop
            titleMarginBottom = frameworkToolbar.titleMarginBottom
        } else {
            titleMarginStart = 0
            titleMarginEnd = 0
            titleMarginTop = 0
            titleMarginBottom = 0
        }
        collapsingTextHelper.setCollapsedBounds(
            tmpRect.left + if (isRtl) titleMarginEnd else titleMarginStart,
            tmpRect.top + maxOffset + titleMarginTop,
            tmpRect.right - if (isRtl) titleMarginStart else titleMarginEnd,
            tmpRect.bottom + maxOffset - titleMarginBottom
        )
    }

    private fun updateTitleFromToolbarIfNeeded() {
        if (toolbar != null) {
            if (collapsingTitleEnabled && TextUtils.isEmpty(title)) {
                // If we do not currently have a title, try and grab it from the Toolbar
                title = getToolbarTitle(toolbar!!)
            }
        }
    }

    private fun getToolbarTitle(view: View): CharSequence? {
        return when (view) {
            is Toolbar -> view.title
            is android.widget.Toolbar -> view.title
            else -> null
        }
    }

    private fun getHeightWithMargins(view: View): Int {
        val lp = view.layoutParams
        if (lp is MarginLayoutParams) {
            return view.measuredHeight + lp.topMargin + lp.bottomMargin
        }
        return view.measuredHeight
    }

    private fun ensureToolbar() {
        if (!refreshToolbar) return

        toolbar = null
        toolbarDirectChild = null
        if (toolbarId != -1) {
            toolbar = findViewById(toolbarId)
            if (toolbar != null) {
                toolbarDirectChild = findDirectChild(toolbar!!)
            }
        }
        if (toolbar == null) {
            var toolbar: ViewGroup? = null
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (isToolbar(child)) {
                    toolbar = child as ViewGroup
                    break
                }
            }
            this.toolbar = toolbar
        }
        updateDummyView()
        refreshToolbar = false
    }

    private fun updateDummyView() {
        if (!collapsingTitleEnabled && dummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            val parent = dummyView!!.parent
            if (parent is ViewGroup) {
                parent.removeView(dummyView)
            }
        }
        if (collapsingTitleEnabled && toolbar != null) {
            if (dummyView == null) {
                dummyView = View(context)
            }
            if (dummyView!!.parent == null) {
                toolbar!!.addView(
                    dummyView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    }

    private fun isToolbar(view: View): Boolean {
        return view is Toolbar || view is android.widget.Toolbar
    }

    private fun findDirectChild(descendant: View): View {
        var directChild = descendant
        var p = descendant.parent
        while (p !== this && p != null) {
            if (p is View) directChild = p
            p = p.parent
        }
        return directChild
    }

    private fun updateContentDescriptionFromTitle() {
        contentDescription = title
    }

    private fun getViewOffsetHelper(view: View): ViewOffsetHelper {
        var offsetHelper = view.getTag(R.id.view_offset_helper) as ViewOffsetHelper?
        if (offsetHelper == null) {
            offsetHelper = ViewOffsetHelper(view)
            view.setTag(R.id.view_offset_helper, offsetHelper)
        }
        return offsetHelper
    }

    private fun getMaxOffsetForPinChild(child: View): Int {
        val offsetHelper = getViewOffsetHelper(child)
        val lp = child.layoutParams as LayoutParams
        return height - offsetHelper.layoutTop - child.height - lp.bottomMargin
    }

    private inner class OffsetUpdateListener : AppBarLayout.OnOffsetChangedListener {
        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            for (i in 0 until childCount) {
                val child: View = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                val offsetHelper = getViewOffsetHelper(child)
                when (lp.collapseMode) {
                    LayoutParams.COLLAPSE_MODE_PIN -> offsetHelper.topAndBottomOffset =
                        MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child))
                    LayoutParams.COLLAPSE_MODE_PARALLAX -> offsetHelper.topAndBottomOffset =
                        (-verticalOffset * lp.parallaxMultiplier).roundToInt()
                    else -> {}
                }
            }

            // Update the collapsing text's fraction
            val expandRange = height - ViewCompat.getMinimumHeight(this@CollapsingLayout) - insetTop

            collapsingTextHelper.setCurrentOffsetY(verticalOffset + expandRange)
            collapsingTextHelper.expansionFraction = abs(verticalOffset) / expandRange.toFloat()
        }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): FrameLayout.LayoutParams {
        return LayoutParams(p)
    }

    class LayoutParams : FrameLayout.LayoutParams {
        companion object {
            private const val DEFAULT_PARALLAX_MULTIPLIER = 0.5f
            const val COLLAPSE_MODE_OFF = 0
            const val COLLAPSE_MODE_PIN = 1
            const val COLLAPSE_MODE_PARALLAX = 2
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @IntDef(COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX)
        @Retention(AnnotationRetention.SOURCE)
        internal annotation class CollapseMode

        @CollapseMode
        var collapseMode = COLLAPSE_MODE_OFF
        var parallaxMultiplier = DEFAULT_PARALLAX_MULTIPLIER

        @RequiresApi(19)
        constructor(source: FrameLayout.LayoutParams) : super(source)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(p: ViewGroup.LayoutParams) : super(p)
        constructor(source: MarginLayoutParams) : super(source)

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.CollapsingLayout_Layout)
            collapseMode = a.getInt(
                R.styleable.CollapsingLayout_Layout_cp_layout_collapseMode, COLLAPSE_MODE_OFF
            )
            parallaxMultiplier = a.getFloat(
                R.styleable.CollapsingLayout_Layout_cp_layout_collapseParallaxMultiplier,
                DEFAULT_PARALLAX_MULTIPLIER
            )
            a.recycle()
        }
    }

    init {
        collapsingTextHelper.isRtlTextDirectionHeuristicsEnabled = false
        titleCollapseMode = TITLE_COLLAPSE_MODE_SCALE

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CollapsingLayout, defStyleAttr, R.style.CollapsingLayout_Base
        )

        collapsingTextHelper.expandedTextGravity = a.getInt(
            R.styleable.CollapsingLayout_cp_expended_title_gravity,
            GravityCompat.START or Gravity.BOTTOM
        )
        collapsingTextHelper.collapsedTextGravity = a.getInt(
            R.styleable.CollapsingLayout_cp_collapsed_title_gravity,
            GravityCompat.START or Gravity.CENTER_VERTICAL
        )

        a.getDimensionPixelSize(
            R.styleable.CollapsingLayout_cp_expended_text_margin, 0
        ).also {
            expandedMarginStart = it
            expandedMarginBottom = it
            expandedMarginEnd = it
            expandedMarginTop = it
        }

        if (a.hasValue(R.styleable.CollapsingLayout_cp_expended_text_margin_start)) {
            expandedMarginStart = a.getDimensionPixelSize(
                R.styleable.CollapsingLayout_cp_expended_text_margin_start, 0
            )
        }
        if (a.hasValue(R.styleable.CollapsingLayout_cp_expended_text_margin_end)) {
            expandedMarginEnd = a.getDimensionPixelSize(
                R.styleable.CollapsingLayout_cp_expended_text_margin_end, 0
            )
        }
        if (a.hasValue(R.styleable.CollapsingLayout_cp_expended_text_margin_top)) {
            expandedMarginTop = a.getDimensionPixelSize(
                R.styleable.CollapsingLayout_cp_expended_text_margin_top, 0
            )
        }
        if (a.hasValue(R.styleable.CollapsingLayout_cp_expended_text_margin_bottom)) {
            expandedMarginBottom = a.getDimensionPixelSize(
                R.styleable.CollapsingLayout_cp_expended_text_margin_bottom, 0
            )
        }
        collapsingTitleEnabled =
            a.getBoolean(R.styleable.CollapsingLayout_cp_title_enable, true)
        title = a.getText(R.styleable.CollapsingLayout_cp_title)

        if (a.hasValue(R.styleable.CollapsingLayout_cp_collapsed_text_color)) {
            collapsingTextHelper.collapsedTextColor = ColorStateList.valueOf(
                a.getColor(R.styleable.CollapsingLayout_cp_collapsed_text_color, Color.WHITE)
            )
        }
        if (a.hasValue(R.styleable.CollapsingLayout_cp_expended_text_color)) {
            collapsingTextHelper.expandedTextColor = ColorStateList.valueOf(
                a.getColor(R.styleable.CollapsingLayout_cp_expended_text_color, Color.WHITE)
            )
        }
        if (a.hasValue(R.styleable.CollapsingLayout_cp_collapsed_text_size)) {
            collapsingTextHelper.collapsedTextSize =
                a.getDimension(R.styleable.CollapsingLayout_cp_collapsed_text_size, 18f)
        }
        if (a.hasValue(R.styleable.CollapsingLayout_cp_expended_text_size)) {
            collapsingTextHelper.expandedTextSize =
                a.getDimension(R.styleable.CollapsingLayout_cp_expended_text_size, 26f)
        }

        if (a.hasValue(R.styleable.CollapsingLayout_cp_max_lines)) {
            collapsingTextHelper.maxLines =
                a.getInt(R.styleable.CollapsingLayout_cp_max_lines, 1)
        }

        toolbarId = a.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1)
        forceApplySystemWindowInsetTop =
            a.getBoolean(R.styleable.CollapsingLayout_cp_force_apply_window_inset_top, false)
        extraMultilineHeightEnabled =
            a.getBoolean(R.styleable.CollapsingLayout_cp_extra_multiline_height_enabled, false)

        a.recycle()
        setWillNotDraw(false)
        ViewCompat.setOnApplyWindowInsetsListener(this) { _: View?, insets: WindowInsetsCompat? ->
            onWindowInsetChanged(insets)
        }
    }
}