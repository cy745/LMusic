package com.lalilu.lmusic.ui.appbar

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.lalilu.material.appbar.AppBarLayout
import com.lalilu.material.appbar.MyAppbarBehavior

class SquareAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {

    override fun getBehavior(): CoordinatorLayout.Behavior<AppBarLayout> {
        return MyAppbarBehavior(context, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
//    override val rect = Rect(0, 0, 0, 0)
//    override val interceptSize = 100

//    lateinit var helper: AppBarStatusHelper
//    private val zoomBehavior = AppBarZoomBehavior(helper, context, null)

//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        super.onLayout(changed, l, t, r, b)
//        if (changed && helper.currentState == STATE_FULLY_EXPENDED) {
//            this.layout(l, t, r, helper.lastHeight)
//        }
//    }

//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        updateInterceptRect(height, height - interceptSize)
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        return if (checkTouchEvent(event)) true else super.onTouchEvent(event)
//    }

//    override fun whenToIntercept(): Boolean = helper.currentState == STATE_FULLY_EXPENDED
}