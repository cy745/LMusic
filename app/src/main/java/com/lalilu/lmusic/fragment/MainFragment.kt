package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentMainBinding
import com.lalilu.lmusic.adapter.LMusicFragmentStateAdapter
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.state.MainViewModel

class MainFragment : BaseFragment() {
    private lateinit var mState: MainViewModel
    private lateinit var mEvent: SharedViewModel

    private var mPagerAdapter: LMusicFragmentStateAdapter? = null
    override var delayLoadDuration: Long = 100

    override fun initViewModel() {
        mState = getFragmentViewModel(MainViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mPagerAdapter = LMusicFragmentStateAdapter(mActivity!!)
            .addFragment(PlayingFragment())
            .addFragment(PlaylistFragment())

        return DataBindingConfig(R.layout.fragment_main, BR.vm, mState)
            .addParam(BR.pagerAdapter, mPagerAdapter)
    }

    override fun loadInitData() {
        val binding = (mBinding as FragmentMainBinding)
        binding.fmTabLayout.bindToViewPager(binding.fmViewpager)

        mState.nowBgPalette.observe(viewLifecycleOwner) {
            mEvent.nowBgPalette.postValue(it)
        }
        mEvent.nowPlayingRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.nowPlayingMusic.postValue(it) }
        }
        mEvent.pageRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.nowPageInt.postValue(it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = (mBinding as FragmentMainBinding)
        mActivity!!.setSupportActionBar(binding.fmToolbar)

        val child = binding.fmViewpager.getChildAt(0)
        if (child is RecyclerView) child.overScrollMode = View.OVER_SCROLL_NEVER
    }
}