package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.state.NavigatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavigatorFragment : BaseFragment() {
    private lateinit var mState: NavigatorViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    override fun initViewModel() {
        mState = getFragmentViewModel(NavigatorViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_navigator, BR.vm, mState)
            .addParam(BR.ev, mEvent)
    }
}