package com.lalilu.lmusic.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
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
import com.lalilu.common.DeviceUtils
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.screen.component.NavigatorFooter
import com.lalilu.lmusic.service.MSongBrowser
import kotlinx.coroutines.launch

@Composable
@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun MainScreen(
    mSongBrowser: MSongBrowser,
    mediaSource: BaseMediaSource,
    onMoveTaskToBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val scaffoldState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = SpringSpec(
            stiffness = 800f
        )
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
                        if (navController.previousBackStackEntry == null) {
                            scope.launch { scaffoldState.hide() }
                        } else {
                            navController.navigateUp()
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
                onMoveTaskToBack()
            }
        }
    )
}