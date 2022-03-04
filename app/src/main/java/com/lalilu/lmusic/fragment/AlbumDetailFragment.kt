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
import com.lalilu.lmusic.fragment.viewmodel.AlbumDetailViewModel
import com.lalilu.lmusic.service.MSongBrowser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
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
        mAdapter.onItemClick = { item ->
            mSongBrowser.browser?.apply {
                clearMediaItems()
                setMediaItems(mAdapter.data)
                seekToDefaultPosition(mAdapter.data.indexOfFirst { it.mediaId == item.mediaId })
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                play()
            }
        }
        mAdapter.onItemLongClick = {
            findNavController().navigate(
                AlbumDetailFragmentDirections.albumToSongDetail(it.mediaId)
            )
        }
        return DataBindingConfig(R.layout.fragment_detail_album)
            .addParam(BR.adapter, mAdapter)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        mState.album.observe(viewLifecycleOwner) { item ->
            item ?: return@observe
            val list = mediaSource.getChildren(ALBUM_PREFIX + item.mediaId)
            launch(Dispatchers.Main) {
                mAdapter.setDiffNewData(list?.toMutableList())
            }
        }
        mState._album.postValue(
            mediaSource.getItemById(ALBUM_PREFIX + args.albumId)
        )
    }
}