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
import com.lalilu.lmusic.ui.seekbar.*
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
            seekBar.sumValue = (it?.mediaMetadata?.getDuration()?.coerceAtLeast(0) ?: 0f).toFloat()
        }
        mGlobal.currentPositionLiveData.observe(viewLifecycleOwner) {
            seekBar.updateValue(it.toFloat())
        }
        seekBar.clickListeners.add(object : OnSeekBarClickListener {
            override fun onClick(@ClickPart clickPart: Int, action: Int) {
                haptic()
                when (mSongBrowser.browser?.isPlaying) {
                    true -> mSongBrowser.browser?.pause()
                    false -> mSongBrowser.browser?.play()
                    else -> {}
                }
            }

            override fun onDoubleClick(@ClickPart clickPart: Int, action: Int) {
                doubleHaptic()
                when (clickPart) {
                    CLICK_PART_LEFT -> mSongBrowser.browser?.seekToPrevious()
                    CLICK_PART_RIGHT -> mSongBrowser.browser?.seekToNext()
                }
            }

            override fun onLongClick(@ClickPart clickPart: Int, action: Int) {
                println("onLongClick: $clickPart action: $action")
                haptic()
            }
        })
        seekBar.seekToListeners.add(
            object : OnSeekBarSeekToListener {
                override fun onSeekTo(value: Float) {
                    mSongBrowser.browser?.seekTo(value.toLong())
                }
            }
        )
        seekBar.scrollListeners.add(
            object : OnSeekBarScrollToThresholdListener(300f) {
                override fun onScrollToThreshold() {
                    showDialog(dialog)
                    haptic()
                }
            }
        )
        seekBar.cancelListeners.add(
            object : OnSeekBarCancelListener {
                override fun onCancel() {
                    haptic()
                }
            }
        )
    }

    fun haptic() {
        HapticUtils.haptic(this.requireView(), HapticUtils.Strength.HAPTIC_STRONG)
    }

    fun doubleHaptic() {
        HapticUtils.doubleHaptic(this.requireView())
    }
}