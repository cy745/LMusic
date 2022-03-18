package com.lalilu.lmusic.ui.seekbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.R
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.lmusic.utils.HapticUtils
import kotlin.math.roundToInt

@SuppressLint("PrivateResource")
class NewSeekBarPreference constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs, R.attr.seekBarPreferenceStyle, 0),
    OnSeekBarSeekToListener, OnProgressToListener {
    private var seekBar: NewSeekBar? = null

    var mValue = 0f
        set(value) {
            field = value
            seekBar?.nowValue = value
            callChangeListener(value)
        }

    var mMin = 0f
        set(value) {
            field = value
            seekBar?.minValue = value
        }

    var mMax = 100f
        set(value) {
            field = value
            seekBar?.maxValue = value
        }

    var mIncrement = 0
    var mShowValue = true

    init {
        layoutResource = com.lalilu.R.layout.my_seekbar_preference
        val attr = context.obtainStyledAttributes(
            attrs, R.styleable.SeekBarPreference, R.attr.seekBarPreferenceStyle, 0
        )
        mMin = attr.getInt(R.styleable.SeekBarPreference_min, 0).toFloat()
        mMax = attr.getInt(R.styleable.SeekBarPreference_android_max, 100).toFloat()
        mIncrement = attr.getInt(R.styleable.SeekBarPreference_seekBarIncrement, 0)
        mShowValue = attr.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, true)
        attr.recycle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        seekBar = holder.findViewById(R.id.seekbar) as NewSeekBar
        seekBar ?: throw NullPointerException()
        seekBar?.let {
            it.seekToListeners.add(this)
            it.progressToListener.add(this)
            it.valueToText = { value -> value.roundToInt().toString() }
            it.radius = SizeUtils.dp2px(25f).toFloat()
            it.maxValue = mMax
            it.minValue = mMin
            it.nowValue = mValue
            it.isEnabled = isEnabled
            it.thumbDarkModeColor = Color.LTGRAY
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue == null) {
            mValue = 0f
            return
        }
        mValue = (defaultValue as Number).toFloat()
        println("onSetInitialValue: $mValue")
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSeekTo(value: Float) {
        mValue = value
    }

    override fun onProgressToMin(value: Float) {
        seekBar?.let { HapticUtils.haptic(it) }
    }

    override fun onProgressToMax(value: Float) {
        seekBar?.let { HapticUtils.haptic(it) }
    }
}