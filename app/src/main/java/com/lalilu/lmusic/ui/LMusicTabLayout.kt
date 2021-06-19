package com.lalilu.lmusic.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lalilu.R
import com.lalilu.databinding.ItemTabLayoutBinding

class LMusicTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr), TabLayout.OnTabSelectedListener {
    private val maxPx = 45f
    private val minPx = 40f
    private val letterSpacing = 0.1f
    private val offsetPx = maxPx - minPx
    private val textPaint = TextPaint().also {
        it.textSize = maxPx
        it.isSubpixelText = true
    }

    fun bindToViewPager(viewPager: ViewPager2) {
        TabLayoutMediator(this, viewPager) { tab, position ->
            if (position == 0) tab.select()

            val view = inflate(context, R.layout.item_tab_layout, null)
            val textView = ItemTabLayoutBinding.bind(view).tabText
            val text = if (tab.isSelected) "正在播放" else "歌单"
            val textSize = if (tab.isSelected) maxPx else minPx
            val alpha = if (tab.isSelected) 1f else 0.3f

            textView.text = text
            textView.letterSpacing = letterSpacing
            println(textPaint.measureText(text).toInt())
            textView.layoutParams.width =
                (textPaint.measureText(text) * (1 + letterSpacing)).toInt()
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            textView.alpha = alpha
            textView.setTextColor(Color.WHITE)
            tab.customView = view
            tab.select()
        }.attach()
        this.addOnTabSelectedListener(this)
        this.setSelectedTabIndicatorGravity(INDICATOR_GRAVITY_CENTER)
        this.setSelectedTabIndicator(null)
    }

    override fun onTabSelected(tab: Tab?) {
        tab ?: return
        val bd = ItemTabLayoutBinding.bind(tab.customView!!)
        ValueAnimator.ofFloat(0f, 1f).also {
            it.duration = 100
            it.addUpdateListener { value ->
                val percent = value.animatedValue as Float
                val size = minPx + offsetPx * percent
                bd.tabText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
                bd.tabText.alpha = 0.3f + 0.7f * percent
            }
        }.start()
    }

    override fun onTabUnselected(tab: Tab?) {
        tab ?: return
        val bd = ItemTabLayoutBinding.bind(tab.customView!!)
        ValueAnimator.ofFloat(0f, 1f).also {
            it.duration = 100
            it.addUpdateListener { value ->
                val percent = value.animatedValue as Float
                val size = maxPx - offsetPx * percent
                bd.tabText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
                bd.tabText.alpha = 0.3f + 0.7f * (1 - percent)
            }
        }.start()
    }

    override fun onTabReselected(tab: Tab?) {}
}