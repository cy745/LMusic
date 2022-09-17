package com.lalilu.lmusic

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.LocalDataSaver
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.common.PermissionUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.repository.SettingsDataStore
import com.lalilu.lmusic.screen.LMusicNavGraph
import com.lalilu.lmusic.screen.PlayingScreen
import com.lalilu.lmusic.screen.ShowScreen
import com.lalilu.lmusic.screen.component.SmartBar.SmartBarContent
import com.lalilu.lmusic.screen.component.SmartModalBottomSheet
import com.lalilu.lmusic.service.LMusicBrowser
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
    lateinit var dataSaver: DataSaverInterface

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 判断是否已完成初次启动时的用户引导
        val isGuidingOver = settingsDataStore.run { isGuidingOver.get() }
        if (isGuidingOver != true) {
            ActivityUtils.startActivity(GuidingActivity::class.java)
            finish()
            return
        }

        SystemUiUtil.immerseNavigationBar(this)
        PermissionUtils.requestPermission(this, onSuccess = {
            launch { Indexer.index(this@MainActivity) }
            lifecycle.addObserver(LMusicBrowser)
        }, onFailed = {
            ToastUtils.showShort("无外部存储读取权限，无法读取歌曲")
        })

        // 注册返回键事件回调
        onBackPressedDispatcher.addCallback {
            this@MainActivity.moveTaskToBack(false)
        }

        @OptIn(
            ExperimentalMaterial3WindowSizeClassApi::class,
            ExperimentalAnimationApi::class,
            ExperimentalMaterialApi::class
        )
        setContent {
            LMusicTheme {
                val navHostController = rememberAnimatedNavController()

                CompositionLocalProvider(
                    LocalDataSaver provides dataSaver,
                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
                    LocalNavigatorHost provides navHostController
                ) {
                    SmartModalBottomSheet.SmartModalBottomSheetContent(
                        navController = navHostController,
                        sheetContent = {
                            LMusicNavGraph()
                            SmartBarContent(
                                translationY = -SmartModalBottomSheet.offset,
                                alpha = SmartModalBottomSheet.offsetHalfPercent
                            )
                        },
                        content = { PlayingScreen() }
                    )
                    ShowScreen()
                }
            }
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
    }
}

