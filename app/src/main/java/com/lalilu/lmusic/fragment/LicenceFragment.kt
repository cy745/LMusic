package com.lalilu.lmusic.fragment

import com.blankj.utilcode.util.ResourceUtils
import com.lalilu.R
import com.lalilu.databinding.FragmentLicenceBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment

class LicenceFragment : DataBindingFragment() {
    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_licence)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentLicenceBinding
        binding.licenceContent.text = ResourceUtils.readRaw2String(R.raw.licence_third_part)
    }
}