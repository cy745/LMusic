package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.state.NavigatorViewModel

class NavigatorFragment : BaseFragment() {
    private lateinit var mState: NavigatorViewModel
    private lateinit var mEvent: SharedViewModel

    override fun initViewModel() {
        mState = getFragmentViewModel(NavigatorViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_navigator, BR.vm, mState)
    }

    override fun loadInitData() {
        
    }
}