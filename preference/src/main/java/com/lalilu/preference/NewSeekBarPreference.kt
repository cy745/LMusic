package com.lalilu.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lalilu.common.HapticUtils
import com.lalilu.ui.NewSeekBar
import com.lalilu.ui.OnProgressToListener
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarSeekToListener
import kotlin.math.roundToInt

@SuppressLint("PrivateResource")
class NewSeekBarPreference constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs, R.attr.seekBarPreferenceStyle, 0),
    OnSeekBarSeekToListener, OnProgressToListener, OnSeekBarCancelListener {
    private var seekBar: NewSeekBar? = null

    var mValue = 0f
        set(value) {
            if (field == value) return
            seekBar?.updateProgress(value, false)
            setValue(value)
            field = value
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
        layoutResource = R.layout.my_seekbar_preference
        val attr = context.obtainStyledAttributes(
            attrs, R.styleable.SeekBarPreference, R.attr.seekBarPreferenceStyle, 0
        )
        mMin = attr.getInt(
            R.styleable.SeekBarPreference_min,
            0
        ).toFloat()
        mMax = attr.getInt(
            R.styleable.SeekBarPreference_android_max,
            100
        ).toFloat()
        mIncrement = attr.getInt(
            R.styleable.SeekBarPreference_seekBarIncrement,
            0
        )
        mShowValue = attr.getBoolean(
            R.styleable.SeekBarPreference_showSeekBarValue,
            true
        )
        attr.recycle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        seekBar = holder.findViewById(R.id.seekbar) as NewSeekBar
        seekBar ?: throw NullPointerException()
        seekBar?.let {
            it.seekToListeners.add(this)
            it.cancelListeners.add(this)
            it.progressToListener.add(this)
            it.valueToText = { value -> value.roundToInt().toString() }
            it.maxValue = mMax
            it.minValue = mMin
            it.nowValue = mValue
            it.isEnabled = isEnabled
            it.thumbDarkModeColor = Color.LTGRAY
        }
    }

    fun setValue(value: Number) {
        val intValue = value.toInt()
        if (callChangeListener(intValue)) {
            persistInt(intValue)
            notifyChanged()
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        mValue = if (defaultValue != null) {
            if (defaultValue !is Number) throw ClassCastException("defaultValue should be Number.")
            defaultValue.toFloat()
        } else {
            getPersistedInt(mMin.toInt()).toFloat()
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSeekTo(value: Float) {
        mValue = value
    }

    override fun onProgressToMin(value: Float, fromUser: Boolean) {
        if (fromUser) {
            seekBar?.let { HapticUtils.haptic(it) }
        }
    }

    override fun onProgressToMax(value: Float, fromUser: Boolean) {
        if (fromUser) {
            seekBar?.let { HapticUtils.haptic(it) }
        }
    }

    override fun onCancel() {
        seekBar?.let { HapticUtils.haptic(it) }
    }
}