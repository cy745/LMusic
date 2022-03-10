package com.lalilu.lmusic.fragment

import android.annotation.SuppressLint
import androidx.databinding.library.baseAdapters.BR
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lalilu.R
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.ALBUM_PREFIX
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.viewmodel.AlbumDetailViewModel
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
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@SuppressLint("UnsafeOptInUsageError")
class AlbumDetailFragment : DataBindingFragment(), CoroutineScope {
    private val args: AlbumDetailFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: AlbumDetailViewModel

    @Inject
    lateinit var mAdapter: ListAdapter

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemClick = { item, position ->
            mSongBrowser.browser?.apply {
                clearMediaItems()
                setMediaItems(mAdapter.data)
                seekToDefaultPosition(position)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                play()
            }
        }
        mAdapter.onItemLongClick = { item, position ->
            mAdapter.savePosition(mState)
            findNavController().navigate(
                AlbumDetailFragmentDirections.albumToSongDetail(item.mediaId)
            )
        }
        return DataBindingConfig(R.layout.fragment_detail_album)
            .addParam(BR.adapter, mAdapter)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        mState.album.postValue(mediaSource.getItemById(ALBUM_PREFIX + args.albumId))
        mState.postData(
            mediaSource.getChildren(ALBUM_PREFIX + args.albumId)
                ?: emptyList()
        )
    }
}