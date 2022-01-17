package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.Navigation
import com.lalilu.R
import com.lalilu.databinding.FragmentNavigatorBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.event.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavigatorFragment : DataBindingFragment() {

    @Inject
    lateinit var mEvent: SharedViewModel

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_navigator)
            .addParam(BR.ev, mEvent)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentNavigatorBinding
        val toolbar2 = binding.toolbar2

        toolbar2.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.navigator_star -> {
                }
                R.id.navigator_playlist -> Navigation.findNavController(binding.navigator)
                    .navigate(R.id.playlistsFragment)
                R.id.navigator_album -> Navigation.findNavController(binding.navigator)
                    .navigate(R.id.albumsFragment)
                R.id.navigator_songs -> {
                }
                else -> {
                }
            }
            true
        }
    }
}