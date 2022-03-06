package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.lmusic.adapter.AlbumsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.ALBUM_ID
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.viewmodel.AlbumsViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
import com.lalilu.lmusic.viewmodel.savePosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AlbumsFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mState: AlbumsViewModel

    @Inject
    lateinit var mAdapter: AlbumsAdapter

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemClick = {
            mAdapter.savePosition(mState)
            findNavController().navigate(
                AlbumsFragmentDirections.toAlbumDetail(
                    albumId = it.mediaId,
                    albumTitle = it.mediaMetadata.albumTitle.toString()
                )
            )
        }
        return DataBindingConfig(R.layout.fragment_list_albums)
            .addParam(BR.albumsAdapter, mAdapter)
    }

    override fun onViewCreated() {
        mState.postData(mediaSource.getChildren(ALBUM_ID) ?: emptyList())
    }
}