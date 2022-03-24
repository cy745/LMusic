package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.adapter.GenresAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.GENRE_ID
import com.lalilu.lmusic.viewmodel.GenresViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
import com.lalilu.lmusic.viewmodel.savePosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class GenresFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mAdapter: GenresAdapter

    @Inject
    lateinit var mState: GenresViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemClick = { item, position ->
            mAdapter.savePosition(mState)
//            findNavController().navigate(
//                PlaylistsFragmentDirections.toPlaylistDetail(
//                    playlistId = item.playlistId,
//                    playlistTitle = item.playlistTitle
//                )
//            )
        }
        return DataBindingConfig(R.layout.fragment_list_genres)
            .addParam(BR.genresAdapter, mAdapter)
    }

    override fun onViewCreated() {
        mState.postData(mediaSource.getChildren(GENRE_ID) ?: emptyList())
    }
}