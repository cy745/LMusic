package com.lalilu.lmusic

import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MySearchBar
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import com.lalilu.lmusic.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LMusicActivity : DataBindingActivity() {

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immerseStatusBar(this)
        PermissionUtils.requestPermission(this, onSuccess = {
            mediaSource.loadSync()
        }, onFailed = {
            toastUtil.show("无外部存储读取权限，无法读取歌曲")
        })
        volumeControlStream = AudioManager.STREAM_MUSIC
        lifecycle.addObserver(mSongBrowser)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_search -> mEvent.collapseAppbarLayout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        MySearchBar(menu.findItem(R.id.appbar_search)) {
            mSongBrowser.searchFor(it)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }
}