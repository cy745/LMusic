package com.lalilu.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lalilu.ui.MultiStateSwitcher

class MultiStatePreference constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs, R.attr.seekBarPreferenceStyle, 0) {
    private var multiStateSwitcher: MultiStateSwitcher? = null

    var mValue: Int = 0

    init {
        layoutResource = R.layout.my_multi_state_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        multiStateSwitcher = holder.findViewById(R.id.multi_state_button) as MultiStateSwitcher
        multiStateSwitcher ?: throw NullPointerException()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue != null) {
            if (defaultValue !is Number) throw ClassCastException("defaultValue should be Number.")
            mValue = defaultValue.toInt()
            return
        }
        mValue = getPersistedInt(0)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }
}