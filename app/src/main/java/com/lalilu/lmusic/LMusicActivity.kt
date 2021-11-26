package com.lalilu.lmusic

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.base.BaseActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.fragment.MainFragment
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.MainActivityViewModel
import com.lalilu.lmusic.utils.DeviceUtil
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

        requestedOrientation = if (DeviceUtil.isPad(this)) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val manager = supportFragmentManager
        val oldOne = manager.findFragmentById(R.id.fragment_main)

        oldOne.let {
            manager.beginTransaction()
                .remove(it!!)
                .add(R.id.fragment_main, MainFragment())
                .commit()
        }
    }
}