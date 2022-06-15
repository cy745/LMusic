package com.lalilu.lmusic.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.lmusic.screen.component.NavigateLibrary
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.utils.WindowSizeClass
import com.lalilu.lmusic.utils.rememberWindowSizeClass
import com.lalilu.lmusic.utils.safeLaunch
import com.lalilu.lmusic.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun MainScreen(
    currentWindowSizeClass: WindowSizeClass = rememberWindowSizeClass(),
    navController: NavHostController = rememberAnimatedNavController(),
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val scaffoldState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val currentWindowSize = currentWindowSizeClass.windowSize

    val onPopUp: () -> Unit = {
        if (navController.previousBackStackEntry == null) {
            scope.safeLaunch { scaffoldState.hide() }
        } else {
            navController.navigateUp()
        }
    }

    val onClose: () -> Unit = {
        scope.safeLaunch { scaffoldState.hide() }
    }

    val onExpendModal: () -> Unit = {
        scope.safeLaunch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    }

    Row {
        if (currentWindowSize != WindowSize.Compact) {
            NavigateLibrary(
                currentWindowSize = currentWindowSize,
                navController = navController,
                onExpendModal = onExpendModal,
                onPopUp = onPopUp,
                onClose = onClose
            )
        }
        MainScreenForCompat(
            scope = scope,
            currentWindowSizeClass = currentWindowSizeClass,
            navController = navController,
            scaffoldState = scaffoldState,
            bottomSheetContent = {
                if (currentWindowSize == WindowSize.Compact) {
                    NavigateLibrary(
                        currentWindowSize = currentWindowSize,
                        navController = navController,
                        onExpendModal = onExpendModal,
                        onPopUp = onPopUp,
                        onClose = onClose
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
fun MainScreenForCompat(
    currentWindowSizeClass: WindowSizeClass,
    navController: NavHostController,
    scaffoldState: ModalBottomSheetState,
    scope: CoroutineScope = rememberCoroutineScope(),
    viewModel: MainViewModel = hiltViewModel(),
    bottomSheetContent: @Composable () -> Unit = {},
) {
    val mSongBrowser = viewModel.mediaBrowser
    val isEnableBottomSheet = currentWindowSizeClass.windowSize == WindowSize.Compact
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()
    val screenHeight = LocalDensity.current.run { screenHeightDp.toPx() }
    val isVisible = { scaffoldState.offset.value < screenHeight }

    val backgroundColor = MaterialTheme.colors.background
    val elevation = if (!isEnableBottomSheet) 5.dp else 0.dp
    val color = if (!isEnableBottomSheet) backgroundColor else Color.Transparent

    val showScaffold: suspend () -> Unit = {
        /**
         * 在直接从Composable的参数传入的情况下，
         * 不知为何不能在此函数内获取[currentWindowSizeClass]的最新值，
         * 故直接从单例中获取该值
         */
        if (WindowSizeClass.instance?.windowSize == WindowSize.Compact)
            scope.safeLaunch { scaffoldState.show() }
    }
    val scaffoldShow: suspend () -> Unit = {
        navController.navigate(
            from = MainScreenData.Library.name,
            to = MainScreenData.Library.name,
            clearAllBefore = isEnableBottomSheet
        )
        showScaffold()
    }
    val onSongShowDetail: suspend (MediaItem) -> Unit = {
        navController.navigate(
            from = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
            to = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
            clearAllBefore = isEnableBottomSheet
        )
        showScaffold()
    }

    ModalBottomSheetLayout(
        modifier = when (currentWindowSizeClass.windowSize) {
            WindowSize.Compact -> Modifier.fillMaxWidth()
            WindowSize.Medium -> Modifier.fillMaxWidth(0.5f)
            WindowSize.Expanded -> Modifier.width(screenHeightDp / 2f)
        },
        sheetState = scaffoldState,
        sheetBackgroundColor = backgroundColor,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetContent = { bottomSheetContent() }
    ) {
        Surface(
            elevation = elevation,
            color = color
        ) {
            PlayingScreen(
                scope = scope,
                onSongShowDetail = onSongShowDetail,
                onExpendBottomSheet = scaffoldShow,
                onCollapseBottomSheet = scaffoldState::hide,
                onSongSelected = { mSongBrowser.playById(it.mediaId, true) },
                onPlayPause = { mSongBrowser.togglePlay() },
                onPlayNext = { mSongBrowser.browser?.seekToNext() },
                onPlayPrevious = { mSongBrowser.browser?.seekToPrevious() },
                onSongMoveToNext = { mSongBrowser.addToNext(it.mediaId) },
                onSongRemoved = { mSongBrowser.removeById(it.mediaId) },
                onSeekToPosition = { mSongBrowser.browser?.seekTo(it.toLong()) }
            )
        }
    }

    val context = LocalContext.current
    BackHandlerWithNavigator(
        navController = navController,
        onBack = {
            if (isVisible() && isEnableBottomSheet) {
                scope.safeLaunch { scaffoldState.hide() }
            } else {
                context.getActivity()?.moveTaskToBack(false)
            }
        }
    )
}

@Composable
fun BackHandlerWithNavigator(navController: NavController, onBack: () -> Unit) {
    val enable = remember(navController.currentBackStackEntryAsState().value) {
        navController.previousBackStackEntry == null
    }

    BackHandler(
        enabled = enable,
        onBack = onBack
    )
}