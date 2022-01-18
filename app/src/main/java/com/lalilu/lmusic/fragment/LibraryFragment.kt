package com.lalilu.lmusic.fragment

import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.databinding.FragmentLibraryBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment

class LibraryFragment : DataBindingFragment() {

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_library)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentLibraryBinding
        val toAlbums = binding.toAlbums
        val toPlaylists = binding.toPlaylists

        toAlbums.setOnClickListener {
            findNavController().navigate(R.id.to_albums)
        }

        toPlaylists.setOnClickListener {
            findNavController().navigate(R.id.to_playlists)
        }
    }
}