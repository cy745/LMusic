package com.lalilu.lmusic

import android.annotation.SuppressLint
import android.content.Intent
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
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmedia.indexer.FilterType
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.scanner.AudioTaggerScanner
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartBar.SmartBarContent
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.new_screen.LMusicNavHost
import com.lalilu.lmusic.compose.screen.PlayingScreen
import com.lalilu.lmusic.compose.screen.ShowScreen
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.OnBackPressHelper
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import kotlinx.coroutines.flow.combine
import org.koin.android.ext.android.inject

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
class MainActivity : AppCompatActivity() {
    private val lMusicSp: LMusicSp by inject()
    private val browser: LMusicBrowser by inject()

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val start = System.currentTimeMillis()
        AudioTaggerScanner.read(this, intent.data) {
            LogUtils.i(
                "[onNewIntent]: 解析完成,耗时：${System.currentTimeMillis() - start}ms",
                it
            )
            it?.let { browser.addAndPlay(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 判断是否已完成初次启动时的用户引导
        val isGuidingOver = lMusicSp.isGuidingOver.get()
        val isPermissionsGranted = ActivityCompat.checkSelfPermission(this, REQUIRE_PERMISSIONS)
        if (!isGuidingOver || isPermissionsGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityUtils.startActivity(GuidingActivity::class.java)
            finish()
            return
        }

        /**
         * 在LMedia初始化完成前，设置元素筛选器逻辑
         */
        Indexer.setFilterPipe {
            lMusicSp.blockedPaths.flow(true)
                .combine(lMusicSp.enableUnknownFilter.flow(true)) { paths, hideUnknown ->
                    val list = mutableListOf<FilterType>()
                    if (paths != null) {
                        list.add(FilterType.Path(paths))
                    }
                    if (hideUnknown == true) {
                        list.add(FilterType.UnknownArtist)
                    }
                    return@combine list
                }
        }

        Indexer.startListen()
        lifecycle.addObserver(browser)
        SystemUiUtil.immerseNavigationBar(this)
        SystemUiUtil.immersiveCutout(window)

        val backPressHelper = OnBackPressHelper()
        // 注册返回键事件回调
        onBackPressedDispatcher.addCallback { this@MainActivity.moveTaskToBack(false) }
        onBackPressedDispatcher.addCallback(backPressHelper)

        setContent {
            LMusicTheme {
                CompositionLocalProvider(
                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
                    LocalNavigatorHost provides rememberAnimatedNavController(),
                ) {
                    Box {
                        SmartModalBottomSheet.SmartModalBottomSheetContent(
                            sheetContent = {
                                LMusicNavHost()
                                SmartBarContent(
                                    modifier = Modifier.graphicsLayer {
                                        translationY = -SmartModalBottomSheet.offset
                                        alpha = SmartModalBottomSheet.offsetHalfPercent
                                    }
                                )
                            },
                            content = { PlayingScreen(onBackPressHelper = backPressHelper) }
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

