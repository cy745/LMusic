package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.PlayerModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ListFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mAdapter: ListAdapter

    @Inject
    lateinit var playerModule: PlayerModule

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
        // 添加 item 被选中时的处理逻辑
        mAdapter.onItemClickListener = {
            playerModule.mediaController?.transportControls
                ?.playFromMediaId(it.songId.toString(), null)
        }
        return DataBindingConfig(R.layout.fragment_list)
            .addParam(BR.listAdapter, mAdapter)
    }

    override fun onViewCreated() {
//        dataModule.library.observe(viewLifecycleOwner) {
//            mAdapter.setDiffNewData(it?.toMutableList())
//        }
    }
}