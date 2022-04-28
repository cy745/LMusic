package com.lalilu.lmusic

import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.blankj.utilcode.util.BarUtils
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.DataSaverPreferences.Companion.setContext
import com.funny.data_saver.core.LocalDataSaver
import com.lalilu.R
import com.lalilu.common.DeviceUtils
import com.lalilu.common.PermissionUtils
import com.lalilu.common.SystemUiUtil
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.screen.ComposeNavigator
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.PlayingScreen
import com.lalilu.lmusic.screen.clearBackStack
import com.lalilu.lmusic.screen.component.NavigatorFooter
import com.lalilu.lmusic.service.GlobalData
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MySearchView
import com.lalilu.lmusic.ui.bind
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalMaterialApi
@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUiUtil.immerseNavigationBar(this)
        PermissionUtils.requestPermission(this, onSuccess = {
            mediaSource.whenReady { mSongBrowser.recoverLastPlayedItem() }
            mediaSource.loadSync()
        }, onFailed = {
            Toast.makeText(this, "无外部存储读取权限，无法读取歌曲", Toast.LENGTH_SHORT).show()
        })
        volumeControlStream = AudioManager.STREAM_MUSIC
        lifecycle.addObserver(mSongBrowser)
        val dataSaverPreferences = DataSaverPreferences().apply {
            setContext(context = applicationContext)
        }
        setContent {
            CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences) {
                LMusicTheme {
                    MainScreen(
                        mSongBrowser = mSongBrowser,
                        mediaSource = mediaSource,
                        activity = this
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        val searchView = menu.findItem(R.id.appbar_search)
            .actionView as MySearchView
        searchView.bind(GlobalData::searchFor)
        return super.onCreateOptionsMenu(menu)
    }
}

@Composable
@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun MainScreen(
    mSongBrowser: MSongBrowser,
    mediaSource: BaseMediaSource,
    activity: AppCompatActivity
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val scaffoldState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    val screenHeight = remember(configuration.screenHeightDp) {
        DeviceUtils.getHeight(context)
    }
    val screenHeightDp = density.run { screenHeight.toDp() }
    val statusBarHeightDp = density.run { BarUtils.getStatusBarHeight().toDp() }
    val navBarHeightDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isVisible = { scaffoldState.offset.value < screenHeight }

    ModalBottomSheetLayout(
        sheetState = scaffoldState,
        sheetBackgroundColor = MaterialTheme.colors.background,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetContent = {
            Box(
                modifier = Modifier
                    .height(screenHeightDp - statusBarHeightDp)
            ) {
                ComposeNavigator(
                    scope = scope,
                    navController = navController,
                    mediaSource = mediaSource,
                    scaffoldState = scaffoldState,
                    contentPaddingForFooter = navBarHeightDp + 64.dp,
                    modifier = Modifier
                        .padding()
                        .fillMaxSize()
                )
                NavigatorFooter(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.background.copy(alpha = 0.9f))
                        .navigationBarsPadding(),
                    navController = navController,
                    popUp = {
                        if (!navController.navigateUp()) {
                            scope.launch { scaffoldState.hide() }
                        }
                    },
                    close = { scope.launch { scaffoldState.hide() } }
                )
            }
        }
    ) {
        PlayingScreen(
            scope = scope,
            scaffoldHide = scaffoldState::hide,
            scaffoldShow = {
                navController.clearBackStack()
                navController.navigate(route = MainScreenData.Library.name)
                scaffoldState.show()
            },
            onSongSelected = { mSongBrowser.playById(it.mediaId, true) },
            onSongShowDetail = {
                navController.clearBackStack()
                navController.navigate("${MainScreenData.SongDetail.name}/${it.mediaId}")
                scaffoldState.show()
            },
            onSeekToPosition = { mSongBrowser.browser?.seekTo(it.toLong()) },
            onPlayNext = { mSongBrowser.browser?.seekToNext() },
            onPlayPrevious = { mSongBrowser.browser?.seekToPrevious() },
            onPlayPause = { mSongBrowser.togglePlay() }
        )
    }

    BackHandler(
        enabled = true,
        onBack = {
            if (isVisible()) {
                scope.launch { scaffoldState.hide() }
            } else {
                activity.moveTaskToBack(false)
            }
        }
    )
}