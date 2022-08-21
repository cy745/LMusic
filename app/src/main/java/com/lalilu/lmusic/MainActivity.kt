package com.lalilu.lmusic

import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.LocalDataSaver
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.R
import com.lalilu.common.PermissionUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.screen.MainScreen
import com.lalilu.lmusic.screen.ShowScreen
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MySearchView
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var globalDataManager: GlobalDataManager

    @Inject
    lateinit var dataSaverPreferences: DataSaverPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isGuidingOver = SPUtils.getInstance(this.packageName, MODE_PRIVATE)
            .getBoolean(Config.KEY_REMEMBER_IS_GUIDING_OVER)

        if (!isGuidingOver) {
            ActivityUtils.startActivity(GuidingActivity::class.java)
            finish()
            return
        }
        SystemUiUtil.immerseNavigationBar(this)
        PermissionUtils.requestPermission(this, onSuccess = {
            lifecycle.addObserver(mSongBrowser)
            launch {
                Indexer.index(this@MainActivity)
            }
        }, onFailed = {
            ToastUtils.showShort("无外部存储读取权限，无法读取歌曲")
        })

        setContent {
            LMusicTheme {
                @OptIn(
                    ExperimentalMaterial3WindowSizeClassApi::class,
                    ExperimentalAnimationApi::class
                )
                CompositionLocalProvider(
                    LocalDataSaver provides dataSaverPreferences,
                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
                    LocalNavigatorHost provides rememberAnimatedNavController()
                ) {
                    MainScreen()
                    ShowScreen()
                }
            }
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        val searchView = menu.findItem(R.id.appbar_search)
            .actionView as MySearchView
        searchView.bind(globalDataManager::searchFor)
        return super.onCreateOptionsMenu(menu)
    }
}

