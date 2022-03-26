package com.lalilu.ui

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntRange
import com.lalilu.common.SystemUiUtil


open class StateSwitcher @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mStateStaticLayout: List<StaticLayout> = emptyList()

    var mStateText: List<String> = emptyList()
        set(value) {
            if (value == field) return
            field = value
            initStaticLayout()
        }

    var textSize: Float = 35f
        set(value) {
            if (value == field) return
            field = value
            initStaticLayout()
        }

    var bgColor = Color.argb(50, 100, 100, 100)
        set(value) {
            field = value
            invalidate()
        }

    /**
     *  圆角半径
     */
    var radius: Float = 30f
        set(value) {
            field = value
            updatePath()
            invalidate()
        }


    var padding: Float = 0f
        set(value) {
            field = value
            updatePath()
            invalidate()
        }

    /**
     * 当前值文字颜色
     */
    var nowTextDarkModeColor: Int? = null
    var nowTextColor: Int = Color.WHITE
        get() {
            if (isDarkModeNow()) return nowTextDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            textPaint.color = value
            invalidate()
        }

    /**
     * 上层滑块颜色
     */
    var thumbDarkModeColor: Int? = null
    var thumbColor: Int = Color.DKGRAY
        get() {
            if (isDarkModeNow()) return thumbDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            thumbPaint.color = value
            invalidate()
        }

    /**
     * 外部框背景颜色
     * 绘制时将忽略该值的透明度
     * 由 [outSideAlpha] 控制其透明度
     */
    var outSideDarkModeColor: Int? = Color.DKGRAY
    var outSideColor: Int = Color.WHITE
        get() {
            if (isDarkModeNow()) return outSideDarkModeColor ?: field
            return field
        }
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 外部框背景透明度
     */
    @IntRange(from = 0, to = 255)
    var outSideAlpha: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    val minThumbOffset = 0f
    val maxThumbOffset
        get() = availableWidth - singleWidth
    var thumbOffset: Float = 0f
        set(value) {
            if (field == value) return
            field = value.coerceIn(minThumbOffset, maxThumbOffset)
            invalidate()
        }
    private var thumbWidth: Float = 0f
    private var pathInside = Path()
    private var pathOutside = Path()
    private var rect = RectF()

    private var thumbPaint: Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.DKGRAY
        }
    private var textPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.textSize = textSize
            it.isSubpixelText = true
        }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        textPaint.color = nowTextColor
        thumbPaint.color = thumbColor
        thumbWidth = availableWidth / mStateText.size

        // 截取外部框范围
        canvas.clipPath(pathOutside)

        // 绘制外部框背景
        canvas.drawARGB(
            outSideAlpha,
            Color.red(outSideColor),
            Color.green(outSideColor),
            Color.blue(outSideColor)
        )

        // 只保留圆角矩形path部分
        canvas.clipPath(pathInside)

        // 绘制背景
        canvas.drawColor(bgColor)


        // 绘制进度条滑动块
        canvas.drawRoundRect(
            padding + thumbOffset,
            padding,
            thumbOffset + thumbWidth,
            height - padding,
            radius,
            radius,
            thumbPaint
        )

        var x = paddingStart.toFloat()
        mStateStaticLayout.forEach {
            canvas.save()
            canvas.translate(x, (height - it.height) / 2f)
            it.draw(canvas)
            canvas.restore()
            x += it.width
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initStaticLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updatePath()
    }

    /**
     *  判断当前是否处于深色模式
     */
    open fun isDarkModeNow(): Boolean {
        return SystemUiUtil.isDarkMode(context)
    }


    open fun updateOffset(offset: Float, fromUser: Boolean) {
        thumbOffset = offset
    }

    open fun updatePath() {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        pathOutside.reset()
        pathOutside.addRoundRect(rect, radius, radius, Path.Direction.CW)

        rect.set(padding, padding, width - padding, height - padding)
        pathInside.reset()
        pathInside.addRoundRect(rect, radius, radius, Path.Direction.CW)
    }

    private fun initStaticLayout() {
        textPaint.textSize = textSize
        mStateStaticLayout = mStateText.map {
            StaticLayout.Builder
                .obtain(it, 0, it.length, textPaint, singleWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(1)
                .build()
        }
    }

    val availableWidth: Float
        get() = (width - paddingStart - paddingEnd).toFloat()

    val singleWidth
        get() = availableWidth / mStateText.size

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.StateSwitcher)
        radius = attr.getDimension(R.styleable.StateSwitcher_st_radius, 30f)
        attr.recycle()
    }
}