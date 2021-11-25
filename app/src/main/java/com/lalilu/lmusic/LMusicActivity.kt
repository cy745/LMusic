package com.lalilu.lmusic

import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.databinding.ActivityNowBinding
import com.lalilu.lmusic.base.BaseActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.service.LMusicService
import com.lalilu.lmusic.state.MainActivityViewModel
import com.lalilu.lmusic.ui.seekbar.OnSeekBarChangeListenerAdapter
import com.lalilu.lmusic.utils.HapticUtils
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.scanner.MediaScanner
import com.tencent.mmkv.MMKV
import java.util.*

class LMusicActivity : BaseActivity() {
    private lateinit var mState: MainActivityViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var playerModule: LMusicPlayerModule

    override fun initViewModel() {
        mState = getActivityViewModel(MainActivityViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_now, BR.vm, mState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immerseStatusBar(this)
        playerModule = LMusicPlayerModule.getInstance(application)
        playerModule.initMusicBrowser(this)
        PermissionUtils.requestPermission(this)

        val seekBar = (mBinding as ActivityNowBinding).maSeekBar

        // 从 metadata 中获取歌曲的总时长传递给 SeekBar
        playerModule.metadata.observe(this) {
            if (it == null) return@observe
            val sum = it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            seekBar.setSumDuration(sum)
        }

        // 从 playbackState 中获取歌曲的播放进度传递给 SeekBar
        playerModule.playBackState.observe(this) {
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
                    LMusicService.ACTION_PLAY_PAUSE, null
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

        // mEvent 的背景色和 mState 的背景色进行绑定
        mEvent.nowBgPalette.observe(this) {
            mState.nowBgPalette.postValue(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                playerModule.mediaController.value?.transportControls?.play()
            }
            R.id.appbar_pause -> {
                playerModule.mediaController.value?.transportControls?.pause()
            }
            R.id.appbar_create_playlist -> {
                MMKV.defaultMMKV().decodeLong(SharedViewModel.LAST_PLAYLIST_ID, 0)
                runOnUiThread {
                    Toast.makeText(
                        this@LMusicActivity,
                        "${MMKV.defaultMMKV().decodeLong(SharedViewModel.LAST_PLAYLIST_ID, 0)}," +
                                " ${
                                    MMKV.defaultMMKV().decodeLong(SharedViewModel.LAST_MUSIC_ID, 0)
                                }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.appbar_scan_song -> {
                (application as LMusicApp).lmusicScanner.setOnScanCallback(
                    object : MediaScanner.OnScanCallback<LSong>() {
                        override fun onScanFinish(totalCount: Int) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@LMusicActivity,
                                    "scan done, sum: $totalCount",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            mEvent.allPlaylistRequest.requireData()
                            mEvent.nowPlaylistRequest.requireData()
                        }

                        override fun onScanStart(totalCount: Int) {
                            (application as LMusicApp).playlistMMKV.deleteAllLocalSong()
                        }

                        override fun onScanProgress(nowCount: Int, item: LSong) {
                            (application as LMusicApp).playlistMMKV.saveSongToLocalPlaylist(item)
                        }

                        override fun onScanException(msg: String?) {
                            runOnUiThread {
                                Toast.makeText(this@LMusicActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ).scanStart(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        playerModule.connect()
        super.onStart()
    }

    override fun onStop() {
        playerModule.disconnect()
        super.onStop()
    }

    override fun onResume() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}