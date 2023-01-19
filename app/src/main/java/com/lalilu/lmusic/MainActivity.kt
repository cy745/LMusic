package com.lalilu.lmusic

import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.blankj.utilcode.util.ActivityUtils
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.LocalDataSaver
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartBar.SmartBarContent
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.screen.LMusicNavGraph
import com.lalilu.lmusic.compose.screen.PlayingScreen
import com.lalilu.lmusic.compose.screen.ShowScreen
import com.lalilu.lmusic.datastore.SettingsDataStore
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.OnBackPressedHelper
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.LocalLibraryVM
import com.lalilu.lmusic.viewmodel.LocalMainVM
import com.lalilu.lmusic.viewmodel.LocalPlayingVM
import com.lalilu.lmusic.viewmodel.LocalPlaylistDetailVM
import com.lalilu.lmusic.viewmodel.LocalPlaylistsVM
import com.lalilu.lmusic.viewmodel.LocalSearchVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var dataSaver: DataSaverInterface

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var browser: LMusicBrowser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 判断是否已完成初次启动时的用户引导
        val isGuidingOver = settingsDataStore.run { isGuidingOver.get() }
        val isPermissionsGranted = ActivityCompat.checkSelfPermission(this, REQUIRE_PERMISSIONS)
        if (isGuidingOver != true || isPermissionsGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityUtils.startActivity(GuidingActivity::class.java)
            finish()
            return
        }

        SystemUiUtil.immerseNavigationBar(this)
        launch { Indexer.index(this@MainActivity) }
        lifecycle.addObserver(browser)

        // 注册返回键事件回调
        onBackPressedDispatcher.addCallback {
            this@MainActivity.moveTaskToBack(false)
        }

        val backPressedHelper = OnBackPressedHelper()
        onBackPressedDispatcher.addCallback(backPressedHelper)

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
                    LocalNavigatorHost provides navHostController,
                    LocalLibraryVM provides hiltViewModel(),
                    LocalPlayingVM provides hiltViewModel(),
                    LocalPlaylistsVM provides hiltViewModel(),
                    LocalMainVM provides hiltViewModel(),
                    LocalPlaylistDetailVM provides hiltViewModel(),
                    LocalSearchVM provides hiltViewModel()
                ) {
                    Box {
                        SmartModalBottomSheet.SmartModalBottomSheetContent(
                            navController = navHostController,
                            sheetContent = {
                                LMusicNavGraph()
                                SmartBarContent(
                                    modifier = Modifier.graphicsLayer {
                                        translationY = -SmartModalBottomSheet.offset
                                        alpha = SmartModalBottomSheet.offsetHalfPercent
                                    }
                                )
                            },
                            content = { PlayingScreen(backPressedHelper) }
                        )
                        ShowScreen()
                        DynamicTips.Content(modifier = Modifier.align(Alignment.TopCenter))
                    }
                }
            }
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
    }
}

