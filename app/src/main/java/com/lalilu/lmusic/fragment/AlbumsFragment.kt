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
        mAdapter.onItemClickListener = { album ->
            findNavController().navigate(
                AlbumsFragmentDirections.albumDetail(
                    albumId = album.albumId,
                    title = album.albumTitle
                )
            )
        }
        return DataBindingConfig(R.layout.fragment_albums)
            .addParam(BR.albumsAdapter, mAdapter)
    }

    override fun onViewCreated() {
        mediaSource.albums.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it?.toMutableList())
        }
    }
}