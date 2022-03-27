package com.lalilu.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lalilu.ui.MultiStateSwitcher
import com.lalilu.ui.OnStateChangeListener

@SuppressLint("PrivateResource")
class MultiStatePreference constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs, R.attr.seekBarPreferenceStyle, 0),
    OnStateChangeListener {
    private var multiStateSwitcher: MultiStateSwitcher? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        multiStateSwitcher = holder.findViewById(R.id.multi_state_button) as MultiStateSwitcher
        multiStateSwitcher ?: throw NullPointerException()
        multiStateSwitcher!!.let { switcher ->
            mStateText?.let { switcher.mStateText = it }
            switcher.stateChangeListeners.add(this)
            if (mState != null) {
                switcher.readyHelper.whenReady {
                    if (!it) return@whenReady
                    switcher.snapAnimateStateTo(mState!!, true)
                    mState = null
                }
            }
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        mState = if (defaultValue != null) {
            if (defaultValue !is Number) throw ClassCastException("defaultValue should be Number.")
            defaultValue.toInt()
        } else {
            getPersistedInt(0)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onStateChange(state: Int, fromUser: Boolean) {
        multiStateSwitcher?.post {
            setValue(state)
        }
    }

    fun setValue(value: Number) {
        val intValue = value.toInt()
        if (callChangeListener(intValue)) {
            persistInt(intValue)
            notifyChanged()
        }
    }

    var mState: Int? = null
    var mStateText: List<String>? = null

    init {
        layoutResource = R.layout.my_multi_state_preference
        val attr = context.obtainStyledAttributes(attrs, R.styleable.MultiStatePreference)
        val textArrayId = attr.getResourceId(R.styleable.MultiStatePreference_mt_text_array, -1)
        if (textArrayId != -1) {
            mStateText = context.resources.getStringArray(textArrayId).toList()
        }
        attr.recycle()
    }
}