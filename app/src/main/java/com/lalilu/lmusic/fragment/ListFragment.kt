package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.adapter.MSongListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.LMusicPlayerModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ListFragment : DataBindingFragment() {

    @Inject
    lateinit var mAdapter: MSongListAdapter

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
        // 添加 item 被选中时的处理逻辑
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val data = adapter.data[position] as FullSongInfo? ?: return@setOnItemClickListener

            playerModule.mediaController?.transportControls
                ?.playFromMediaId(data.song.songId.toString(), null)
        }
        return DataBindingConfig(R.layout.fragment_list)
            .addParam(BR.listAdapter, mAdapter)
    }

    override fun onViewCreated() {
        dataModule.nowListLiveData.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it?.toMutableList())
        }
    }
}