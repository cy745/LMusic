package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.event.PlayerModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AllSongFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mAdapter: ListAdapter

    @Inject
    lateinit var playerModule: PlayerModule

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun getDataBindingConfig(): DataBindingConfig {
        // 添加 item 被选中时的处理逻辑
        mAdapter.onItemClick = {
            playerModule.mediaController?.transportControls
                ?.playFromMediaId(it.songId.toString(), null)
        }
        mAdapter.onItemLongClick = {
            findNavController().navigate(
                AllSongFragmentDirections.allSongToSongDetail(it.songId)
            )
        }
        return DataBindingConfig(R.layout.fragment_detail_all_song)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        mediaSource.songs.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it.toMutableList())
        }
    }
}