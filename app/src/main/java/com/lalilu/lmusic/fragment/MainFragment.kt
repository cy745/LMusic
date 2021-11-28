package com.lalilu.lmusic.fragment

import android.media.MediaMetadata
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentMainBinding
import com.lalilu.lmusic.adapter.LMusicFragmentStateAdapter
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.service.MSongService
import com.lalilu.lmusic.state.MainViewModel
import com.lalilu.lmusic.ui.seekbar.OnSeekBarChangeListenerAdapter
import com.lalilu.lmusic.utils.HapticUtils

class MainFragment : BaseFragment() {
    private lateinit var mState: MainViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var playerModule: LMusicPlayerModule

    private var mPagerAdapter: LMusicFragmentStateAdapter? = null

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
        mEvent.pageRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.nowPageInt.postValue(it) }
        }
        // mEvent 的背景色和 mState 的背景色进行绑定
        mEvent.nowBgPalette.observe(viewLifecycleOwner) {
            mState.nowBgPalette.postValue(it)
        }
    }

    override fun loadInitView() {
        val binding = (mBinding as FragmentMainBinding)

        val child = binding.fmViewpager?.getChildAt(0)
        if (child is RecyclerView) child.overScrollMode = View.OVER_SCROLL_NEVER

        val seekBar = (mBinding as FragmentMainBinding).maSeekBar
        playerModule = LMusicPlayerModule.getInstance(requireActivity().application)

        // 从 metadata 中获取歌曲的总时长传递给 SeekBar
        playerModule.metadata.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val sum = it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            seekBar.setSumDuration(sum)
        }

        // 从 playbackState 中获取歌曲的播放进度传递给 SeekBar
        playerModule.playBackState.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            seekBar.updatePosition(it)
        }

        // 为 SeekBar 添加监听器
        seekBar.onSeekBarChangeListener = object : OnSeekBarChangeListenerAdapter() {
            override fun onStopTrackingTouch(position: Long) {
                playerModule.mediaController.value?.transportControls?.seekTo(position)
            }

            override fun onClick() {
                playerModule.mediaController.value?.transportControls?.sendCustomAction(
                    MSongService.ACTION_PLAY_PAUSE, null
                )
                HapticUtils.haptic(seekBar.rootView)
            }

            override fun onProgressToMax() {
                HapticUtils.haptic(seekBar.rootView)
            }

            override fun onProgressToMin() {
                HapticUtils.haptic(seekBar.rootView)
            }

            override fun onProgressToMiddle() {
                HapticUtils.haptic(seekBar.rootView)
            }
        }
    }
}