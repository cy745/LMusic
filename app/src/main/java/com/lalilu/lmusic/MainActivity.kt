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
import com.blankj.utilcode.util.ActivityUtils
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmedia.LMedia
import com.lalilu.lmusic.Config.REQUIRE_PERMISSIONS
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartBar.SmartBarContent
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.screen.LMusicNavGraph
import com.lalilu.lmusic.compose.screen.PlayingScreen
import com.lalilu.lmusic.compose.screen.ShowScreen
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val lMusicSp: LMusicSp by inject()
    private val browser: LMusicBrowser by inject()

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

        LMedia.index()
        SystemUiUtil.immerseNavigationBar(this)
        lifecycle.addObserver(browser)

        // 注册返回键事件回调
        onBackPressedDispatcher.addCallback {
            this@MainActivity.moveTaskToBack(false)
        }

        setContent {
            LMusicTheme {
                CompositionLocalProvider(
                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
                    LocalNavigatorHost provides rememberAnimatedNavController(),
                ) {
                    Box {
                        SmartModalBottomSheet.SmartModalBottomSheetContent(
                            sheetContent = {
                                LMusicNavGraph()
                                SmartBarContent(
                                    modifier = Modifier.graphicsLayer {
                                        translationY = -SmartModalBottomSheet.offset
                                        alpha = SmartModalBottomSheet.offsetHalfPercent
                                    }
                                )
                            },
                            content = { PlayingScreen() }
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

