package com.lalilu.preference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView

abstract class BasePreferenceFragmentCompat : PreferenceFragmentCompat() {
    abstract fun initPreference(savedInstanceState: Bundle?, rootKey: String?)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        initPreference(savedInstanceState, rootKey)
        preferenceScreen.forEach(this::setCustomView)
    }

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState)
            .apply {
                overScrollMode = View.OVER_SCROLL_NEVER
            }
    }

    private fun setCustomView(preference: Preference) {
        if (preference is PreferenceCategory) {
            preference.forEach(this::setCustomView)
        }
        preference.layoutResource = if (preference.icon != null) {
            when (preference) {
                is PreferenceCategory -> R.layout.my_preference_category_layout_with_icon
                is SwitchPreferenceCompat -> R.layout.my_preference_layout_with_icon
                is SeekBarPreference -> R.layout.my_preference_seekbar_layout_with_icon
                else -> preference.layoutResource
            }
        } else {
            when (preference) {
                is PreferenceCategory -> R.layout.my_preference_category_layout
                is SwitchPreferenceCompat -> R.layout.my_preference_layout
                is SeekBarPreference -> R.layout.my_preference_seekbar_layout
                else -> preference.layoutResource
            }
        }
    }
}