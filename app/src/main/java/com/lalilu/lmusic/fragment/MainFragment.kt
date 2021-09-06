package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.dirror.lyricviewx.OnPlayClickListener
import com.lalilu.R
import com.lalilu.databinding.FragmentMainBinding
import com.lalilu.lmusic.adapter.LMusicFragmentStateAdapter
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.MainViewModel
import com.lalilu.lmusic.utils.Mathf.Companion.getPositionFromPlaybackStateCompat
import java.util.*
import kotlin.concurrent.schedule

class MainFragment : BaseFragment() {
    private lateinit var mState: MainViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var playerModule: LMusicPlayerModule
    private var positionTimer: Timer? = null


    private var mPagerAdapter: LMusicFragmentStateAdapter? = null
    override var delayLoadDuration: Long = 100

    override fun initViewModel() {
        mState = getFragmentViewModel(MainViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
        playerModule = LMusicPlayerModule.getInstance(requireActivity().application)
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
        binding.fmLyricViewX.setLabel("暂无歌词")

        mState.nowBgPalette.observe(viewLifecycleOwner) {
            mEvent.nowBgPalette.postValue(it)
        }
        mEvent.nowPlayingRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.nowPlayingMusic.postValue(it) }
        }
        mEvent.pageRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.nowPageInt.postValue(it) }
        }
        mState.nowPlayingMusic.observe(viewLifecycleOwner) {
            val lyric = it.mLocalInfo?.mLyric
            binding.fmLyricViewX.loadLyric(lyric)
        }
        playerModule.playBackState.observe(viewLifecycleOwner) {
            it ?: return@observe
            var currentDuration = getPositionFromPlaybackStateCompat(it)

            positionTimer?.cancel()
            if (it.state == PlaybackStateCompat.STATE_PLAYING)
                positionTimer = Timer().apply {
                    this.schedule(0, 1000) {
                        binding.fmLyricViewX.updateTime(currentDuration)
                        currentDuration += 1000
                    }
                }
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