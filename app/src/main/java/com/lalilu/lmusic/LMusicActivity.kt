package com.lalilu.lmusic

import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.event.GlobalViewModel
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MySearchView
import com.lalilu.lmusic.ui.bind
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.utils.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LMusicActivity : DataBindingActivity() {

    @Inject
    lateinit var mGlobal: GlobalViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immerseStatusBar(this)
        PermissionUtils.requestPermission(this, onSuccess = {
            mediaSource.whenReady {
                mSongBrowser.recoverLastPlayedItem()
            }
            mediaSource.loadSync()
        }, onFailed = {
            Toast.makeText(this, "无外部存储读取权限，无法读取歌曲", Toast.LENGTH_SHORT).show()
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
        val searchView = menu.findItem(R.id.appbar_search)
            .actionView as MySearchView
        searchView.bind {
            mGlobal.searchFor(it)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }
}