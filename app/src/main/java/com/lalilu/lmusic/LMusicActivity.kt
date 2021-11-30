package com.lalilu.lmusic

import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.base.BaseActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.MainActivityViewModel
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.ToastUtil
import com.lalilu.lmusic.utils.scanner.MSongScanner

class LMusicActivity : BaseActivity() {
    private lateinit var mState: MainActivityViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var playerModule: LMusicPlayerModule

    override fun initViewModel() {
        mState = getActivityViewModel(MainActivityViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main, BR.vm, mState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immerseStatusBar(this)
        playerModule = LMusicPlayerModule.getInstance(application)
        playerModule.initMusicBrowser(this)
        PermissionUtils.requestPermission(this)

//        println("isPad: ${DeviceUtil.isPad(this)}")
//        requestedOrientation = if (DeviceUtil.isPad(this)) {
//            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        } else {
//            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        }
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
                mEvent.nowPlaylistWithSongsRequest.requireData()
                mEvent.nowMSongRequest.requireData()
                mEvent.allPlaylistRequest.requireData()
            }
            R.id.appbar_scan_song -> {
                MSongScanner.setScanFailed {
                    ToastUtil.text("提示信息: $it").show(this)
                }.setScanFinish {
                    mEvent.nowPlaylistWithSongsRequest.requireData(0L)
                    ToastUtil.text("扫描共计: $it").show(this)
                }.scanStart(this)
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
        val refreshList = listOf(
            R.id.fragment_main
        )

        val trans = manager.beginTransaction()
        refreshList.forEach { id ->
            manager.findFragmentById(id)?.let {
                trans.remove(it).add(id, it.javaClass.newInstance())
            }
        }
        trans.commit()
    }
}