package com.lalilu.lmusic.fragment

import com.lalilu.BR
import com.lalilu.R
import com.lalilu.lmusic.adapter.ShareAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.service.AblyService
import com.lalilu.lmusic.viewmodel.ShareViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareFragment : DataBindingFragment() {

    @Inject
    lateinit var ablyService: AblyService

    @Inject
    lateinit var mAdapter: ShareAdapter

    @Inject
    lateinit var mState: ShareViewModel

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)

        return DataBindingConfig(R.layout.fragment_share)
            .addParam(BR.shareAdapter, mAdapter)
    }

    override fun onViewCreated() {
        ablyService.otherHistoryLiveData.observe(viewLifecycleOwner) {
            it ?: return@observe
            mState.postData(it.values.toList())
        }
    }
}