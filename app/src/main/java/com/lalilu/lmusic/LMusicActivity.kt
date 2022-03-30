package com.lalilu.lmusic

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.lalilu.R
import com.lalilu.common.PermissionUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmusic.base.DataBindingActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.event.GlobalViewModel
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MySearchView
import com.lalilu.lmusic.ui.bind
import com.lalilu.lmusic.viewmodel.AblyService
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
    lateinit var ablyService: AblyService

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUiUtil.immerseNavigationBar(this)
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
        lifecycle.addObserver(ablyService)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println(
            """
                [LMusicActivity]#onNewIntent
                action: ${intent?.action}
                category: ${intent?.categories}
                data: ${intent?.data}
                author: ${intent?.data?.authority}
                path: ${intent?.data?.path}
            """.trimIndent()
        )
        // TODO: 处理外部传入的intent，需要记录传入的Uri
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