package com.lalilu.lmusic.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.lalilu.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}