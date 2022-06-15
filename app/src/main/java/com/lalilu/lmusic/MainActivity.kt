package com.lalilu.lmusic

import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import com.blankj.utilcode.util.ToastUtils
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.DataSaverPreferences.Companion.setContext
import com.funny.data_saver.core.LocalDataSaver
import com.lalilu.R
import com.lalilu.common.PermissionUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmusic.datasource.MMediaSource
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.screen.AgreementDialog
import com.lalilu.lmusic.screen.MainScreen
import com.lalilu.lmusic.screen.ShowScreen
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MySearchView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalMaterialApi
@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var mediaSource: MMediaSource

    @Inject
    lateinit var globalDataManager: GlobalDataManager

    private val dataSaverPreferences by lazy {
        DataSaverPreferences().apply {
            setContext(context = applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUiUtil.immerseNavigationBar(this)
        PermissionUtils.requestPermission(this, onSuccess = {
            mediaSource.start()
            lifecycle.addObserver(mSongBrowser)
        }, onFailed = {
            ToastUtils.showShort("无外部存储读取权限，无法读取歌曲")
        })

        volumeControlStream = AudioManager.STREAM_MUSIC
        setContent {
            LMusicTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences) {
                    MainScreen()
                    ShowScreen()
                    AgreementDialog()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        val searchView = menu.findItem(R.id.appbar_search)
            .actionView as MySearchView
        searchView.bind(globalDataManager::searchFor)
        return super.onCreateOptionsMenu(menu)
    }
}