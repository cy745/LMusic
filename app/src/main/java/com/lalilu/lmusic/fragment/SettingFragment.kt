package com.lalilu.lmusic.fragment

import android.content.Context
import android.os.Bundle
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.preference.BasePreferenceFragmentCompat

class SettingFragment : BasePreferenceFragmentCompat() {
    override fun initPreference(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        preferenceManager.sharedPreferencesName = Config.SETTINGS_SP
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}