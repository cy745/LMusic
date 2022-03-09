package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.ALL_ID
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.viewmodel.AllSongViewModel
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
class AllSongFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: AllSongViewModel

    @Inject
    lateinit var mAdapter: ListAdapter

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        // 添加 item 被选中时的处理逻辑
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
            mAdapter.savePosition(mState)
            findNavController().navigate(
                AllSongFragmentDirections.allSongToSongDetail(it.mediaId)
            )
        }
        return DataBindingConfig(R.layout.fragment_detail_all_song)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        mState.postData(mediaSource.getChildren(ALL_ID) ?: emptyList())
    }
}