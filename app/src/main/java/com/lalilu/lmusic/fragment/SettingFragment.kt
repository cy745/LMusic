package com.lalilu.lmusic.fragment

import android.os.Bundle
import com.lalilu.R
import com.lalilu.preference.BasePreferenceFragmentCompat

class SettingFragment : BasePreferenceFragmentCompat() {
    override fun initPreference(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}