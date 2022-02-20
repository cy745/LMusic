package com.lalilu.lmusic.fragment

import com.lalilu.R
import com.lalilu.databinding.FragmentLibraryBinding
import com.lalilu.lmusic.base.BaseBottomSheetFragment
import com.lalilu.lmusic.base.DataBindingConfig

class LibraryFragment : BaseBottomSheetFragment<Any, FragmentLibraryBinding>() {

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_library)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentLibraryBinding
        val toAlbums = binding.toAlbums
        val toPlaylists = binding.toPlaylists
    }
}