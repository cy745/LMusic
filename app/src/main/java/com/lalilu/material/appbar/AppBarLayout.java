/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lalilu.material.appbar;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.core.math.MathUtils.clamp;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.abs;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.R;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of material
 * designs app bar concept, namely scrolling gestures.
 */
@SuppressLint("RestrictedApi")
public class AppBarLayout extends LinearLayout implements CoordinatorLayout.AttachedBehavior {

    static final int PENDING_ACTION_NONE = 0x0;
    static final int PENDING_ACTION_EXPANDED = 0x1;
    static final int PENDING_ACTION_COLLAPSED = 1 << 1;
    static final int PENDING_ACTION_ANIMATE_ENABLED = 1 << 2;
    static final int PENDING_ACTION_FORCE = 1 << 3;

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new MyAppbarBehavior(getContext(), null);
    }

    /**
     * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
     * offset changes.
     */
    // TODO(b/76413401): remove this base interface after the widget migration
    public interface BaseOnOffsetChangedListener<T extends AppBarLayout> {

        /**
         * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows child
         * views to implement custom behavior based on the offset (for instance pinning a view at a
         * certain y value).
         *
         * @param appBarLayout   the {@link AppBarLayout} which offset has changed
         * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
         */
        void onOffsetChanged(T appBarLayout, int verticalOffset);
    }

    /**
     * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
     * offset changes.
     */
    // TODO(b/76413401): update this interface after the widget migration
    public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout> {
        void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
    }

    /**
     * Definition for a callback to be invoked when the lift on scroll elevation and background color
     * change.
     */
    public interface LiftOnScrollListener {
        void onUpdate(@Dimension float elevation, @ColorInt int backgroundColor);
    }

    private static final int DEF_STYLE_RES = R.style.Widget_Design_AppBarLayout;
    private static final int INVALID_SCROLL_RANGE = -1;

    private int currentOffset;
    private int totalScrollRange = INVALID_SCROLL_RANGE;
    private int downPreScrollRange = INVALID_SCROLL_RANGE;
    private int downScrollRange = INVALID_SCROLL_RANGE;

    private boolean haveChildWithInterpolator;

    private int pendingAction = PENDING_ACTION_NONE;

    @Nullable
    private WindowInsetsCompat lastInsets;

    private List<BaseOnOffsetChangedListener> listeners;

    private boolean liftableOverride;
    private boolean liftable;
    private boolean lifted;

    private boolean liftOnScroll;
    @IdRes
    private int liftOnScrollTargetViewId;
    @Nullable
    private WeakReference<View> liftOnScrollTargetView;
    @Nullable
    private ValueAnimator elevationOverlayAnimator;
    private final List<LiftOnScrollListener> liftOnScrollListeners = new ArrayList<>();

    private int[] tmpStatesArray;

    @Nullable
    private Drawable statusBarForeground;

    public AppBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.appBarLayoutStyle);
    }

    public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();
        setOrientation(VERTICAL);

        if (Build.VERSION.SDK_INT >= 21) {
            // Use the bounds view outline provider so that we cast a shadow, even without a
            // background
            if (getOutlineProvider() == ViewOutlineProvider.BACKGROUND) {
                ViewUtilsLollipop.setBoundsViewOutlineProvider(this);
            }

            // If we're running on API 21+, we should reset any state list animator from our
            // default style
            ViewUtilsLollipop.setStateListAnimatorFromAttrs(this, attrs, defStyleAttr, DEF_STYLE_RES);
        }

        final TypedArray a =
                ThemeEnforcement.obtainStyledAttributes(
                        context, attrs, R.styleable.AppBarLayout, defStyleAttr, DEF_STYLE_RES);

        ViewCompat.setBackground(this, a.getDrawable(R.styleable.AppBarLayout_android_background));

        if (getBackground() instanceof ColorDrawable) {
            ColorDrawable background = (ColorDrawable) getBackground();
            MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
            materialShapeDrawable.initializeElevationOverlay(context);
            ViewCompat.setBackground(this, materialShapeDrawable);
        }

        if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
            setExpanded(
                    a.getBoolean(R.styleable.AppBarLayout_expanded, false),
                    false, /* animate */
                    false /* force */);
        }

        if (Build.VERSION.SDK_INT >= 21 && a.hasValue(R.styleable.AppBarLayout_elevation)) {
            ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(
                    this, a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // In O+, we have these values set in the style. Since there is no defStyleAttr for
            // AppBarLayout at the AppCompat level, check for these attributes here.
            if (a.hasValue(R.styleable.AppBarLayout_android_keyboardNavigationCluster)) {
                this.setKeyboardNavigationCluster(
                        a.getBoolean(R.styleable.AppBarLayout_android_keyboardNavigationCluster, false));
            }
            if (a.hasValue(R.styleable.AppBarLayout_android_touchscreenBlocksFocus)) {
                this.setTouchscreenBlocksFocus(
                        a.getBoolean(R.styleable.AppBarLayout_android_touchscreenBlocksFocus, false));
            }
        }

        liftOnScroll = a.getBoolean(R.styleable.AppBarLayout_liftOnScroll, false);
        liftOnScrollTargetViewId =
                a.getResourceId(R.styleable.AppBarLayout_liftOnScrollTargetViewId, View.NO_ID);

        setStatusBarForeground(a.getDrawable(R.styleable.AppBarLayout_statusBarForeground));
        a.recycle();

        ViewCompat.setOnApplyWindowInsetsListener(
                this,
                new androidx.core.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        return onWindowInsetChanged(insets);
                    }
                });
    }

    /**
     * Add a listener that will be called when the offset of this {@link AppBarLayout} changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     * @see #removeOnOffsetChangedListener(OnOffsetChangedListener)
     */
    @SuppressWarnings("FunctionalInterfaceClash")
    public void addOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @SuppressWarnings("FunctionalInterfaceClash")
    public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
        addOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
    }

    /**
     * Remove the previously added {@link OnOffsetChangedListener}.
     *
     * @param listener the listener to remove.
     */
    // TODO(b/76413401): change back to removeOnOffsetChangedListener once the widget migration is
    // finished since the shim class needs to implement this method.
    @SuppressWarnings("FunctionalInterfaceClash")
    public void removeOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
        if (listeners != null && listener != null) {
            listeners.remove(listener);
        }
    }

    @SuppressWarnings("FunctionalInterfaceClash")
    public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
        removeOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
    }

    /**
     * Add a {@link LiftOnScrollListener} that will be called when the lift on scroll elevation and
     * background color of this {@link AppBarLayout} change.
     */
    public void addLiftOnScrollListener(@NonNull LiftOnScrollListener liftOnScrollListener) {
        liftOnScrollListeners.add(liftOnScrollListener);
    }

    /**
     * Remove a previously added {@link LiftOnScrollListener}.
     */
    public boolean removeLiftOnScrollListener(@NonNull LiftOnScrollListener liftOnScrollListener) {
        return liftOnScrollListeners.remove(liftOnScrollListener);
    }

    /**
     * Remove all previously added {@link LiftOnScrollListener}s.
     */
    public void clearLiftOnScrollListener() {
        liftOnScrollListeners.clear();
    }

    /**
     * Set the drawable to use for the status bar foreground drawable. Providing null will disable the
     * scrim functionality.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#AppBarLayout_statusBarForeground
     * @see #getStatusBarForeground()
     */
    public void setStatusBarForeground(@Nullable Drawable drawable) {
        if (statusBarForeground != drawable) {
            if (statusBarForeground != null) {
                statusBarForeground.setCallback(null);
            }
            statusBarForeground = drawable != null ? drawable.mutate() : null;
            if (statusBarForeground != null) {
                if (statusBarForeground.isStateful()) {
                    statusBarForeground.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(statusBarForeground, ViewCompat.getLayoutDirection(this));
                statusBarForeground.setVisible(getVisibility() == VISIBLE, false);
                statusBarForeground.setCallback(this);
            }
            updateWillNotDraw();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the status bar foreground.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param color the color to display
     * @attr ref R.styleable#AppBarLayout_statusBarForeground
     * @see #getStatusBarForeground()
     */
    public void setStatusBarForegroundColor(@ColorInt int color) {
        setStatusBarForeground(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the status bar foreground from resources.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#AppBarLayout_statusBarForeground
     * @see #getStatusBarForeground()
     */
    public void setStatusBarForegroundResource(@DrawableRes int resId) {
        setStatusBarForeground(AppCompatResources.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the status bar foreground.
     *
     * @attr ref R.styleable#AppBarLayout_statusBarForeground
     * @see #setStatusBarForeground(Drawable)
     */
    @Nullable
    public Drawable getStatusBarForeground() {
        return statusBarForeground;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        // Draw the status bar foreground drawable if we have a top inset
        if (shouldDrawStatusBarForeground()) {
            int saveCount = canvas.save();
            canvas.translate(0f, -currentOffset);
            statusBarForeground.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();

        Drawable d = statusBarForeground;
        if (d != null && d.isStateful() && d.setState(state)) {
            invalidateDrawable(d);
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == statusBarForeground;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (statusBarForeground != null) {
            statusBarForeground.setVisible(visible, false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // If we're set to handle system windows but our first child is not, we need to add some
        // height to ourselves to pad the first child down below the status bar
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY
                && ViewCompat.getFitsSystemWindows(this)
                && shouldOffsetFirstChild()) {
            int newHeight = getMeasuredHeight();
            switch (heightMode) {
                case MeasureSpec.AT_MOST:
                    // For AT_MOST, we need to clamp our desired height with the max height
                    newHeight =
                            clamp(
                                    getMeasuredHeight() + getTopInset(), 0, MeasureSpec.getSize(heightMeasureSpec));
                    break;
                case MeasureSpec.UNSPECIFIED:
                    // For UNSPECIFIED we can use any height so just add the top inset
                    newHeight += getTopInset();
                    break;
                default: // fall out
            }
            setMeasuredDimension(getMeasuredWidth(), newHeight);
        }

        invalidateScrollRanges();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
            // If we need to offset the first child, we need to offset all of them to make space
            final int topInset = getTopInset();
            for (int z = getChildCount() - 1; z >= 0; z--) {
                ViewCompat.offsetTopAndBottom(getChildAt(z), topInset);
            }
        }

        invalidateScrollRanges();

        haveChildWithInterpolator = false;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
            final Interpolator interpolator = childLp.getScrollInterpolator();

            if (interpolator != null) {
                haveChildWithInterpolator = true;
                break;
            }
        }

        if (statusBarForeground != null) {
            statusBarForeground.setBounds(0, 0, getWidth(), getTopInset());
        }

        // If the user has set liftable manually, don't set liftable state automatically.
        if (!liftableOverride) {
            setLiftableState(liftOnScroll || hasCollapsibleChild());
        }
    }

    private void updateWillNotDraw() {
        setWillNotDraw(!shouldDrawStatusBarForeground());
    }

    private boolean shouldDrawStatusBarForeground() {
        return statusBarForeground != null && getTopInset() > 0;
    }

    private boolean hasCollapsibleChild() {
        for (int i = 0, z = getChildCount(); i < z; i++) {
            if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
                return true;
            }
        }
        return false;
    }

    private void invalidateScrollRanges() {
        // Invalidate the scroll ranges
        totalScrollRange = INVALID_SCROLL_RANGE;
        downPreScrollRange = INVALID_SCROLL_RANGE;
        downScrollRange = INVALID_SCROLL_RANGE;
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "AppBarLayout is always vertical and does not support horizontal orientation");
        }
        super.setOrientation(orientation);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        MaterialShapeUtils.setParentAbsoluteElevation(this);
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);

        MaterialShapeUtils.setElevation(this, elevation);
    }

    /**
     * Sets whether this {@link AppBarLayout} is expanded or not, animating if it has already been
     * laid out.
     *
     * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a direct
     * child of a {@link CoordinatorLayout}.
     *
     * @param expanded true if the layout should be fully expanded, false if it should be fully
     *                 collapsed
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded) {
        setExpanded(expanded, ViewCompat.isLaidOut(this));
    }

    /**
     * Sets whether this {@link AppBarLayout} is expanded or not.
     *
     * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a direct
     * child of a {@link CoordinatorLayout}.
     *
     * @param expanded true if the layout should be fully expanded, false if it should be fully
     *                 collapsed
     * @param animate  Whether to animate to the new state
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded, boolean animate) {
        setExpanded(expanded, animate, true);
    }

    private void setExpanded(boolean expanded, boolean animate, boolean force) {
        pendingAction =
                (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
                        | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0)
                        | (force ? PENDING_ACTION_FORCE : 0);
        requestLayout();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (Build.VERSION.SDK_INT >= 19 && p instanceof LinearLayout.LayoutParams) {
            return new LayoutParams((LinearLayout.LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        clearLiftOnScrollTargetView();
    }

    boolean hasChildWithInterpolator() {
        return haveChildWithInterpolator;
    }

    /**
     * Returns the scroll range of all children.
     *
     * @return the scroll range in px
     */
    public final int getTotalScrollRange() {
        if (totalScrollRange != INVALID_SCROLL_RANGE) {
            return totalScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();
            final int flags = lp.scrollFlags;

            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childHeight + lp.topMargin + lp.bottomMargin;

                if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
                    // If this is the first child and it wants to handle system windows, we need to make
                    // sure we don't scroll it past the inset
                    range -= getTopInset();
                }
                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing scroll, we to take the collapsed height into account.
                    // We also break straight away since later views can't scroll beneath
                    // us
                    range -= ViewCompat.getMinimumHeight(child);
                    break;
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return totalScrollRange = Math.max(0, range);
    }

    boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /**
     * Return the scroll range when scrolling up from a nested pre-scroll.
     */
    int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /**
     * Return the scroll range when scrolling down from a nested pre-scroll.
     */
    int getDownNestedPreScrollRange() {
        if (downPreScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return downPreScrollRange;
        }

        int range = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();
            final int flags = lp.scrollFlags;

            if ((flags & LayoutParams.FLAG_QUICK_RETURN) == LayoutParams.FLAG_QUICK_RETURN) {
                // First take the margin into account
                int childRange = lp.topMargin + lp.bottomMargin;
                // The view has the quick return flag combination...
                if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
                    // If they're set to enter collapsed, use the minimum height
                    childRange += ViewCompat.getMinimumHeight(child);
                } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // Only enter by the amount of the collapsed height
                    childRange += childHeight - ViewCompat.getMinimumHeight(child);
                } else {
                    // Else use the full height
                    childRange += childHeight;
                }
                if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
                    // If this is the first child and it wants to handle system windows, we need to make
                    // sure we don't scroll past the inset
                    childRange = Math.min(childRange, childHeight - getTopInset());
                }
                range += childRange;
            } else if (range > 0) {
                // If we've hit an non-quick return scrollable view, and we've already hit a
                // quick return view, return now
                break;
            }
        }
        return downPreScrollRange = Math.max(0, range);
    }

    /**
     * Return the scroll range when scrolling down from a nested scroll.
     */
    int getDownNestedScrollRange() {
        if (downScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return downScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childHeight = child.getMeasuredHeight();
            childHeight += lp.topMargin + lp.bottomMargin;

            final int flags = lp.scrollFlags;

            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childHeight;

                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing exit scroll, we to take the collapsed height into account.
                    // We also break the range straight away since later views can't scroll
                    // beneath us
                    range -= ViewCompat.getMinimumHeight(child);
                    break;
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return downScrollRange = Math.max(0, range);
    }

    void onOffsetChanged(int offset) {
        currentOffset = offset;

        if (!willNotDraw()) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        // Iterate backwards through the list so that most recently added listeners
        // get the first chance to decide
        if (listeners != null) {
            for (int i = 0, z = listeners.size(); i < z; i++) {
                final BaseOnOffsetChangedListener listener = listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(this, offset);
                }
            }
        }
    }

    public final int getMinimumHeightForVisibleOverlappingContent() {
        final int topInset = getTopInset();
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return (minHeight * 2) + topInset;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = getChildCount();
        final int lastChildMinHeight =
                childCount >= 1 ? ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) : 0;
        if (lastChildMinHeight != 0) {
            return (lastChildMinHeight * 2) + topInset;
        }

        // If we reach here then we don't have a min height explicitly set. Instead we'll take a
        // guess at 1/3 of our height being visible
        return getHeight() / 3;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (tmpStatesArray == null) {
            // Note that we can't allocate this at the class level (in declaration) since some paths in
            // super View constructor are going to call this method before that
            tmpStatesArray = new int[4];
        }
        final int[] extraStates = tmpStatesArray;
        final int[] states = super.onCreateDrawableState(extraSpace + extraStates.length);

        extraStates[0] = liftable ? R.attr.state_liftable : -R.attr.state_liftable;
        extraStates[1] = liftable && lifted ? R.attr.state_lifted : -R.attr.state_lifted;

        // Note that state_collapsible and state_collapsed are deprecated. This is to keep compatibility
        // with existing state list animators that depend on these states.
        extraStates[2] = liftable ? R.attr.state_collapsible : -R.attr.state_collapsible;
        extraStates[3] = liftable && lifted ? R.attr.state_collapsed : -R.attr.state_collapsed;

        return mergeDrawableStates(states, extraStates);
    }

    /**
     * Sets whether the {@link AppBarLayout} is liftable or not.
     *
     * @return true if the liftable state changed
     */
    public boolean setLiftable(boolean liftable) {
        this.liftableOverride = true;
        return setLiftableState(liftable);
    }

    /**
     * Sets whether the {@link AppBarLayout} lifted state corresponding to {@link
     * #setLiftable(boolean)} and {@link #setLifted(boolean)} will be overridden manually.
     *
     * <p>If true, this means that the {@link AppBarLayout} will not manage its own lifted state and
     * it should instead be manually updated via {@link #setLifted(boolean)}. If false, the {@link
     * AppBarLayout} will manage its lifted state based on the scrolling sibling view.
     *
     * <p>Note that calling {@link #setLiftable(boolean)} will result in this liftable override being
     * enabled and set to true by default.
     */
    public void setLiftableOverrideEnabled(boolean enabled) {
        this.liftableOverride = enabled;
    }

    // Internal helper method that updates liftable state without enabling the override.
    private boolean setLiftableState(boolean liftable) {
        if (this.liftable != liftable) {
            this.liftable = liftable;
            refreshDrawableState();
            return true;
        }
        return false;
    }

    /**
     * Sets whether the {@link AppBarLayout} is in a lifted state or not.
     *
     * @return true if the lifted state changed
     */
    public boolean setLifted(boolean lifted) {
        return setLiftedState(lifted, /* force= */ true);
    }

    /**
     * Returns whether the {@link AppBarLayout} is in a lifted state or not.
     */
    public boolean isLifted() {
        return lifted;
    }

    boolean setLiftedState(boolean lifted) {
        return setLiftedState(lifted, /* force= */ !liftableOverride);
    }

    // Internal helper method that updates lifted state.
    boolean setLiftedState(boolean lifted, boolean force) {
        if (force && this.lifted != lifted) {
            this.lifted = lifted;
            refreshDrawableState();
            if (liftOnScroll && getBackground() instanceof MaterialShapeDrawable) {
                startLiftOnScrollElevationOverlayAnimation((MaterialShapeDrawable) getBackground(), lifted);
            }
            return true;
        }
        return false;
    }

    private void startLiftOnScrollElevationOverlayAnimation(
            @NonNull final MaterialShapeDrawable background, boolean lifted) {
        float appBarElevation = getResources().getDimension(R.dimen.design_appbar_elevation);
        float fromElevation = lifted ? 0 : appBarElevation;
        float toElevation = lifted ? appBarElevation : 0;

        if (elevationOverlayAnimator != null) {
            elevationOverlayAnimator.cancel();
        }

        elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation);
        elevationOverlayAnimator.setDuration(
                getResources().getInteger(R.integer.app_bar_elevation_anim_duration));
        elevationOverlayAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
        elevationOverlayAnimator.addUpdateListener(
                new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                        float elevation = (float) valueAnimator.getAnimatedValue();
                        background.setElevation(elevation);
                        if (statusBarForeground instanceof MaterialShapeDrawable) {
                            ((MaterialShapeDrawable) statusBarForeground).setElevation(elevation);
                        }
                        for (LiftOnScrollListener liftOnScrollListener : liftOnScrollListeners) {
                            liftOnScrollListener.onUpdate(elevation, background.getResolvedTintColor());
                        }
                    }
                });
        elevationOverlayAnimator.start();
    }

    /**
     * Sets whether the {@link AppBarLayout} lifts on scroll or not.
     *
     * <p>If set to true, the {@link AppBarLayout} will animate to the lifted, or elevated, state when
     * content is scrolled beneath it. Requires
     * `app:layout_behavior="@string/appbar_scrolling_view_behavior` to be set on the scrolling
     * sibling (e.g., `NestedScrollView`, `RecyclerView`, etc.).
     */
    public void setLiftOnScroll(boolean liftOnScroll) {
        this.liftOnScroll = liftOnScroll;
    }

    /**
     * Returns whether the {@link AppBarLayout} lifts on scroll or not.
     */
    public boolean isLiftOnScroll() {
        return liftOnScroll;
    }

    /**
     * Sets the id of the view that the {@link AppBarLayout} should use to determine whether it should
     * be lifted.
     */
    public void setLiftOnScrollTargetViewId(@IdRes int liftOnScrollTargetViewId) {
        this.liftOnScrollTargetViewId = liftOnScrollTargetViewId;
        // Invalidate cached target view so it will be looked up on next scroll.
        clearLiftOnScrollTargetView();
    }

    /**
     * Returns the id of the view that the {@link AppBarLayout} should use to determine whether it
     * should be lifted.
     */
    @IdRes
    public int getLiftOnScrollTargetViewId() {
        return liftOnScrollTargetViewId;
    }

    boolean shouldLift(@Nullable View defaultScrollingView) {
        View scrollingView = findLiftOnScrollTargetView(defaultScrollingView);
        if (scrollingView == null) {
            scrollingView = defaultScrollingView;
        }
        return scrollingView != null
                && (scrollingView.canScrollVertically(-1) || scrollingView.getScrollY() > 0);
    }

    @Nullable
    private View findLiftOnScrollTargetView(@Nullable View defaultScrollingView) {
        if (liftOnScrollTargetView == null && liftOnScrollTargetViewId != View.NO_ID) {
            View targetView = null;
            if (defaultScrollingView != null) {
                targetView = defaultScrollingView.findViewById(liftOnScrollTargetViewId);
            }
            if (targetView == null && getParent() instanceof ViewGroup) {
                // Assumes the scrolling view is a child of the AppBarLayout's parent,
                // which should be true due to the CoordinatorLayout pattern.
                targetView = ((ViewGroup) getParent()).findViewById(liftOnScrollTargetViewId);
            }
            if (targetView != null) {
                liftOnScrollTargetView = new WeakReference<>(targetView);
            }
        }
        return liftOnScrollTargetView != null ? liftOnScrollTargetView.get() : null;
    }

    private void clearLiftOnScrollTargetView() {
        if (liftOnScrollTargetView != null) {
            liftOnScrollTargetView.clear();
        }
        liftOnScrollTargetView = null;
    }

    /**
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_elevation
     * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
     * a {@link android.animation.StateListAnimator}. If a target elevation is set, either by this
     * method or the {@code app:elevation} attribute, a new state list animator is created which
     * uses the given {@code elevation} value.
     */
    @Deprecated
    public void setTargetElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= 21) {
            ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, elevation);
        }
    }

    /**
     * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
     * a {@link android.animation.StateListAnimator}. This method now always returns 0.
     */
    @Deprecated
    public float getTargetElevation() {
        return 0;
    }

    int getPendingAction() {
        return pendingAction;
    }

    void resetPendingAction() {
        pendingAction = PENDING_ACTION_NONE;
    }

    @VisibleForTesting
    final int getTopInset() {
        return lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
    }

    /**
     * Whether the first child needs to be offset because it does not want to handle the top window
     * inset
     */
    private boolean shouldOffsetFirstChild() {
        if (getChildCount() > 0) {
            final View firstChild = getChildAt(0);
            return firstChild.getVisibility() != GONE && !ViewCompat.getFitsSystemWindows(firstChild);
        }
        return false;
    }

    WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets;
        }

        // If our insets have changed, keep them and trigger a layout...
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets;
            updateWillNotDraw();
            requestLayout();
        }

        return insets;
    }

    /**
     * A {@link ViewGroup.LayoutParams} implementation for {@link AppBarLayout}.
     */
    public static class LayoutParams extends LinearLayout.LayoutParams {

        /**
         * @hide
         */
        @RestrictTo(LIBRARY_GROUP)
        @IntDef(
                flag = true,
                value = {
                        SCROLL_FLAG_NO_SCROLL,
                        SCROLL_FLAG_SCROLL,
                        SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
                        SCROLL_FLAG_ENTER_ALWAYS,
                        SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
                        SCROLL_FLAG_SNAP,
                        SCROLL_FLAG_SNAP_MARGINS,
                })
        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {
        }

        /**
         * Disable scrolling on the view. This flag should not be combined with any of the other scroll
         * flags.
         */
        public static final int SCROLL_FLAG_NO_SCROLL = 0x0;

        /**
         * The view will be scroll in direct relation to scroll events. This flag needs to be set for
         * any of the other flags to take effect. If any sibling views before this one do not have this
         * flag, then this value has no effect.
         */
        public static final int SCROLL_FLAG_SCROLL = 0x1;

        /**
         * When exiting (scrolling off screen) the view will be scrolled until it is 'collapsed'. The
         * collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 1 << 1;

        /**
         * When entering (scrolling on screen) the view will scroll on any downwards scroll event,
         * regardless of whether the scrolling view is also scrolling. This is commonly referred to as
         * the 'quick return' pattern.
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 1 << 2;

        /**
         * An additional flag for 'enterAlways' which modifies the returning view to only initially
         * scroll back to it's collapsed height. Once the scrolling view has reached the end of it's
         * scroll range, the remainder of this view will be scrolled into view. The collapsed height is
         * defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 1 << 3;

        /**
         * Upon a scroll ending, if the view is only partially visible then it will be snapped and
         * scrolled to its closest edge. For example, if the view only has its bottom 25% displayed, it
         * will be scrolled off screen completely. Conversely, if its bottom 75% is visible then it will
         * be scrolled fully into view.
         */
        public static final int SCROLL_FLAG_SNAP = 1 << 4;

        /**
         * An additional flag to be used with 'snap'. If set, the view will be snapped to its top and
         * bottom margins, as opposed to the edges of the view itself.
         */
        public static final int SCROLL_FLAG_SNAP_MARGINS = 1 << 5;

        /**
         * Internal flags which allows quick checking features
         */
        static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;

        static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;
        static final int COLLAPSIBLE_FLAGS =
                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;

        int scrollFlags = SCROLL_FLAG_SCROLL;

        /**
         * No effect should be placed on this view. It will scroll 1:1 with the AppBarLayout/scrolling
         * content.
         */
        private static final int SCROLL_EFFECT_NONE = 0;

        /**
         * An effect that will "compress" this view as it hits the scroll ceiling (typically the top of
         * the screen). This is a parallax effect that masks this view and decreases its scroll ratio
         * in relation to the AppBarLayout's offset.
         */
        private static final int SCROLL_EFFECT_COMPRESS = 1;

        private ChildScrollEffect scrollEffect;

        Interpolator scrollInterpolator;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_Layout);
            scrollFlags = a.getInt(R.styleable.AppBarLayout_Layout_layout_scrollFlags, 0);

            int scrollEffectInt =
                    a.getInt(R.styleable.AppBarLayout_Layout_layout_scrollEffect, SCROLL_EFFECT_NONE);
            setScrollEffect(createScrollEffectFromInt(scrollEffectInt));

            if (a.hasValue(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator)) {
                int resId = a.getResourceId(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator, 0);
                scrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(c, resId);
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(19)
        public LayoutParams(LinearLayout.LayoutParams source) {
            // The copy constructor called here only exists on API 19+.
            super(source);
        }

        @RequiresApi(19)
        public LayoutParams(@NonNull LayoutParams source) {
            // The copy constructor called here only exists on API 19+.
            super(source);
            scrollFlags = source.scrollFlags;
            scrollInterpolator = source.scrollInterpolator;
        }

        /**
         * Set the scrolling flags.
         *
         * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL}, {@link
         *              #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS}, {@link
         *              #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED}, {@link #SCROLL_FLAG_SNAP}, and {@link
         *              #SCROLL_FLAG_SNAP_MARGINS}. Otherwise, use {@link #SCROLL_FLAG_NO_SCROLL} to disable
         *              scrolling.
         * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollFlags
         * @see #getScrollFlags()
         */
        public void setScrollFlags(@ScrollFlags int flags) {
            scrollFlags = flags;
        }

        /**
         * Returns the scrolling flags.
         *
         * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollFlags
         * @see #setScrollFlags(int)
         */
        @ScrollFlags
        public int getScrollFlags() {
            return scrollFlags;
        }

        @Nullable
        private ChildScrollEffect createScrollEffectFromInt(int scrollEffectInt) {
            switch (scrollEffectInt) {
                case SCROLL_EFFECT_COMPRESS:
                    return new CompressChildScrollEffect();
                default:
                    return null;
            }
        }

        /**
         * Get the scroll effect to be applied when the AppBarLayout's offset changes
         */
        @Nullable
        public ChildScrollEffect getScrollEffect() {
            return scrollEffect;
        }

        /**
         * Set the scroll effect to be applied when the AppBarLayout's offset changes.
         *
         * @param scrollEffect An {@code AppBarLayoutChildScrollEffect} implementation. If null is
         *                     passed, the scroll effect will be cleared and no effect will be applied.
         */
        public void setScrollEffect(@Nullable ChildScrollEffect scrollEffect) {
            this.scrollEffect = scrollEffect;
        }

        /**
         * Set the interpolator to when scrolling the view associated with this {@link LayoutParams}.
         *
         * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
         * @attr ref
         * com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
         * @see #getScrollInterpolator()
         */
        public void setScrollInterpolator(Interpolator interpolator) {
            scrollInterpolator = interpolator;
        }

        /**
         * Returns the {@link Interpolator} being used for scrolling the view associated with this
         * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
         *
         * @attr ref
         * com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
         * @see #setScrollInterpolator(Interpolator)
         */
        public Interpolator getScrollInterpolator() {
            return scrollInterpolator;
        }

        /**
         * Returns true if the scroll flags are compatible for 'collapsing'
         */
        boolean isCollapsible() {
            return (scrollFlags & SCROLL_FLAG_SCROLL) == SCROLL_FLAG_SCROLL
                    && (scrollFlags & COLLAPSIBLE_FLAGS) != 0;
        }
    }

    /**
     * An effect class that should be implemented and used by AppBarLayout children to be given
     * effects when the AppBarLayout's offset changes.
     */
    public abstract static class ChildScrollEffect {

        /**
         * Called each time the AppBarLayout's offset changes. Update the {@code child} with any desired
         * effects.
         *
         * @param appBarLayout The parent AppBarLayout
         * @param child        The View to be given any desired effect
         */
        public abstract void onOffsetChanged(
                @NonNull AppBarLayout appBarLayout, @NonNull View child, float offset);
    }

    /**
     * A class which handles updating an AppBarLayout child, if marked with the {@code
     * app:layout_scrollEffect} {@code compress}, at each step in the {@code AppBarLayout}'s offset
     * animation.
     *
     * <p>Only a single {@code AppBarLayout} child should be given a compress effect.
     */
    public static class CompressChildScrollEffect extends ChildScrollEffect {

        // The factor of the child's height by which this child will scroll during compression. Setting
        // this to 0 would keep the child in place and look like the AppBarLayout simply masks the view
        // without offsetting it at all. Setting this to 1 would scroll the child up with the ABL plus
        // translate the child up by its full height. A negative value will translate the child down.
        private static final float COMPRESS_DISTANCE_FACTOR = .3f;

        private final Rect relativeRect = new Rect();
        private final Rect ghostRect = new Rect();

        private static void updateRelativeRect(Rect rect, AppBarLayout appBarLayout, View child) {
            child.getDrawingRect(rect);
            // Get the child's rect relative to its parent ABL
            appBarLayout.offsetDescendantRectToMyCoords(child, rect);
            rect.offset(0, -appBarLayout.getTopInset());
        }

        @Override
        public void onOffsetChanged(
                @NonNull AppBarLayout appBarLayout, @NonNull View child, float offset) {
            updateRelativeRect(relativeRect, appBarLayout, child);
            float distanceFromCeiling = relativeRect.top - abs(offset);
            // If the view is at the ceiling, begin the compress animation.
            if (distanceFromCeiling <= 0F) {
                // The "compressed" progress. When p = 0, the top of the child is at the top of the ceiling
                // (uncompressed). When p = 1, the bottom of the child is at the top of the ceiling
                // (fully compressed).
                float p = clamp(abs(distanceFromCeiling / relativeRect.height()), 0f, 1f);

                // Set offsetY to the full distance from ceiling to keep the child exactly in place.
                float offsetY = -distanceFromCeiling;

                // Decrease the offsetY so the child moves with the app bar parent. Here, it will move a
                // total of the child's height times the compress distance factor but will do so with an
                // eased-out value - moving at a near 1:1 speed with the app bar at first and slowing down
                // as it approaches the ceiling (p = 1).
                float easeOutQuad = 1F - (1F - p) * (1F - p);
                float distance = relativeRect.height() * COMPRESS_DISTANCE_FACTOR;
                offsetY -= distance * easeOutQuad;

                // Translate the view to create a parallax effect, letting the ghost clip when out of
                // bounds.
                child.setTranslationY(offsetY);

                // Use a rect to clip the child by its original bounds before it is given a
                // translation (compress effect). This masks and ensures the child doesn't overlap other
                // children inside the ABL.
                child.getDrawingRect(ghostRect);
                ghostRect.offset(0, (int) -offsetY);
                ViewCompat.setClipBounds(child, ghostRect);
            } else {
                // Reset both the clip bounds and translationY of this view
                ViewCompat.setClipBounds(child, null);
                child.setTranslationY(0);
            }
        }
    }
}