package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentMainBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.base.showDialog
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.event.GlobalViewModel
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.seekbar.OnSeekBarDragUpToThresholdListener
import com.lalilu.lmusic.ui.seekbar.OnSeekBarListenerAdapter
import com.lalilu.lmusic.utils.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainFragment : DataBindingFragment() {

    @Inject
    lateinit var mGlobal: GlobalViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_main, BR.ev, mEvent)
    }

    override fun onViewCreated() {
        val binding = (mBinding as FragmentMainBinding)
        val seekBar = binding.maSeekBar
        val dialog = NavigatorFragment()

        // 从 metadata 中获取歌曲的总时长传递给 SeekBar
        mGlobal.currentMediaItemLiveData.observe(viewLifecycleOwner) {
            seekBar.setSumDuration(it?.mediaMetadata?.getDuration()?.coerceAtLeast(0) ?: 0)
        }
        mGlobal.currentPositionLiveData.observe(viewLifecycleOwner) {
            seekBar.updatePosition(it)
        }

        seekBar.addDragUpProgressListener(object : OnSeekBarDragUpToThresholdListener() {
            override fun onDragUpToThreshold() {
                showDialog(dialog)
                HapticUtils.haptic(seekBar.rootView, HapticUtils.Strength.HAPTIC_STRONG)
            }
        })

        // 为 SeekBar 添加监听器
        seekBar.onSeekBarListener = object : OnSeekBarListenerAdapter() {
            override fun onPositionUpdate(position: Long) {
                mSongBrowser.browser?.seekTo(position)
                HapticUtils.haptic(seekBar.rootView, HapticUtils.Strength.HAPTIC_WEAK)
            }

            override fun onPlayPause() {
                if (mSongBrowser.browser?.isPlaying == true) {
                    mSongBrowser.browser?.pause()
                } else {
                    mSongBrowser.browser?.play()
                }
                HapticUtils.haptic(seekBar.rootView)
            }

            override fun onPlayNext() {
                mSongBrowser.browser?.seekToNext()
                HapticUtils.doubleHaptic(seekBar.rootView)
            }

            override fun onPlayPrevious() {
                mSongBrowser.browser?.seekToPrevious()
                HapticUtils.doubleHaptic(seekBar.rootView)
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

            override fun onProgressCanceled() {
                HapticUtils.haptic(seekBar.rootView, HapticUtils.Strength.HAPTIC_WEAK)
            }
        }
    }
}