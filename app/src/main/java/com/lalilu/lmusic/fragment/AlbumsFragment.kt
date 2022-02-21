package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.lmusic.adapter.AlbumsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.database.MediaSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AlbumsFragment : DataBindingFragment() {

    @Inject
    lateinit var mAdapter: AlbumsAdapter

    @Inject
    lateinit var mediaSource: MediaSource

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClick = {
            findNavController().navigate(
                AlbumsFragmentDirections.toAlbumDetail(
                    albumId = it.albumId,
                    albumTitle = it.albumTitle
                )
            )
        }
        return DataBindingConfig(R.layout.fragment_list_albums)
            .addParam(BR.albumsAdapter, mAdapter)
    }

    override fun onViewCreated() {
        mediaSource.albums.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it?.toMutableList())
        }
    }
}