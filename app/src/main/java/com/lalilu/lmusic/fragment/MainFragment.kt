package com.lalilu.lmusic.fragment

import android.media.MediaMetadata
import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentMainBinding
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.base.showDialog
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.ui.seekbar.OnSeekBarDragUpToThresholdListener
import com.lalilu.lmusic.ui.seekbar.OnSeekBarListenerAdapter
import com.lalilu.lmusic.utils.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : DataBindingFragment() {

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var playerModule: PlayerModule

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_main, BR.ev, mEvent)
    }

    override fun onViewCreated() {
        val binding = (mBinding as FragmentMainBinding)
        val seekBar = binding.maSeekBar
        val dialog = AlbumsFragment()

        // 从 metadata 中获取歌曲的总时长传递给 SeekBar
        playerModule.metadataLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val sum = it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            seekBar.setSumDuration(sum)
        }

        dataModule.songPosition.observe(viewLifecycleOwner) {
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
                playerModule.mediaController?.transportControls?.seekTo(position)
                HapticUtils.haptic(seekBar.rootView, HapticUtils.Strength.HAPTIC_WEAK)
            }

            override fun onPlayPause() {
                playerModule.mediaController?.transportControls?.sendCustomAction(
                    Config.ACTION_PLAY_PAUSE, null
                )
                HapticUtils.haptic(seekBar.rootView)
            }

            override fun onPlayNext() {
                playerModule.mediaController?.transportControls?.skipToNext()
                HapticUtils.doubleHaptic(seekBar.rootView)
            }

            override fun onPlayPrevious() {
                playerModule.mediaController?.transportControls?.skipToPrevious()
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