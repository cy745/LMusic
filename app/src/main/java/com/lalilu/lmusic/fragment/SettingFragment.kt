package com.lalilu.lmusic.fragment

import android.content.Context
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.blankj.utilcode.util.RomUtils
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.preference.BasePreferenceFragmentCompat

class SettingFragment : BasePreferenceFragmentCompat() {
    override fun initPreference(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        preferenceManager.sharedPreferencesName = Config.SETTINGS_SP
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        if (RomUtils.isMeizu()) {
            val key = resources.getString(R.string.sp_key_lyric_settings_status_bar_lyric)
            val preference = preferenceScreen.findPreference<SwitchPreferenceCompat>(key)
            preference?.isVisible = true
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == resources.getString(R.string.destination_label_opensource_licence)) {
            findNavController().navigate(R.id.settingsToLicence)
        }
        return super.onPreferenceTreeClick(preference)
    }
}