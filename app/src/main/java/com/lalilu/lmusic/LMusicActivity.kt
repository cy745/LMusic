package com.lalilu.lmusic

import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.ToastUtil
import com.lalilu.lmusic.utils.scanner.MSongScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LMusicActivity : DataBindingActivity() {

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    @Inject
    lateinit var songScanner: MSongScanner

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immerseStatusBar(this)
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
                playerModule.mediaController?.transportControls?.play()
            }
            R.id.appbar_pause -> {
                playerModule.mediaController?.transportControls?.pause()
            }
            R.id.appbar_create_playlist -> {
                // todo 创建播放列表的逻辑
            }
            R.id.appbar_scan_song -> {
                songScanner.setScanFailed {
                    ToastUtil.text("[提示信息]: $it").show(this)
                }.setScanFinish {
                    // todo 扫描完成后更新视图
                    ToastUtil.text("[扫描完成]: 共计$it 首歌曲").show(this)
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