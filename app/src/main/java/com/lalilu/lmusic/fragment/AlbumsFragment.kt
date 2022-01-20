package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.lmusic.adapter.MSongAlbumsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.event.DataModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AlbumsFragment : DataBindingFragment() {

    @Inject
    lateinit var mAdapter: MSongAlbumsAdapter

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val album = adapter.data[position] as MAlbum

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
        dataModule.allAlbum.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it?.toMutableList())
        }
    }
}