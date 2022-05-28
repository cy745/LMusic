package com.lalilu.lmusic.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lalilu.common.DeviceUtils
import com.lalilu.lmusic.screen.component.NavigateLibrary
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.utils.rememberWindowSizeClass
import com.lalilu.lmusic.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun MainScreen(
    currentWindowSize: WindowSize = rememberWindowSizeClass(),
    navController: NavHostController = rememberNavController(),
    scope: CoroutineScope = rememberCoroutineScope(),
    onMoveTaskToBack: () -> Unit = {}
) {
    val scaffoldState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    fun onPopUp() {
        if (navController.previousBackStackEntry == null) {
            scope.launch { scaffoldState.hide() }
        } else {
            navController.navigateUp()
        }
    }

    fun onClose() {
        scope.launch { scaffoldState.hide() }
    }

    fun onExpendModal() {
        scope.launch { scaffoldState.animateTo(ModalBottomSheetValue.Expanded) }
    }

    Row {
        NavigateLibrary(
            currentWindowSize = currentWindowSize,
            navController = navController,
            onExpendModal = { onExpendModal() },
            onPopUp = { onPopUp() },
            onClose = { onClose() }
        )
        Surface(
            elevation = if (currentWindowSize != WindowSize.Compact) 5.dp else 0.dp,
            color = if (currentWindowSize != WindowSize.Compact) MaterialTheme.colors.background else Color.Transparent
        ) {
            MainScreenForCompat(
                modifier = when (currentWindowSize) {
                    WindowSize.Compact -> Modifier.fillMaxWidth()
                    WindowSize.Medium -> Modifier.fillMaxWidth(0.5f)
                    WindowSize.Expanded -> Modifier.widthIn(max = LocalDensity.current.run {
                        DeviceUtils.getHeight(LocalContext.current).toDp() / 2f
                    })
                },
                scope = scope,
                enableBottomSheet = currentWindowSize == WindowSize.Compact,
                navController = navController,
                scaffoldState = scaffoldState,
                onMoveTaskToBack = onMoveTaskToBack,
                bottomSheetContent = {
                    NavigateLibrary(
                        isForCompact = true,
                        currentWindowSize = currentWindowSize,
                        navController = navController,
                        onExpendModal = { onExpendModal() },
                        onPopUp = { onPopUp() },
                        onClose = { onClose() }
                    )
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
fun MainScreenForCompat(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    scaffoldState: ModalBottomSheetState,
    enableBottomSheet: Boolean = true,
    scope: CoroutineScope = rememberCoroutineScope(),
    viewModel: MainViewModel = hiltViewModel(),
    onMoveTaskToBack: () -> Unit = {},
    bottomSheetContent: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val mSongBrowser = viewModel.mediaBrowser
    val screenHeight = remember(configuration.screenHeightDp) {
        DeviceUtils.getHeight(context)
    }
    val isVisible = { scaffoldState.offset.value < screenHeight }

    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = scaffoldState,
        sheetBackgroundColor = MaterialTheme.colors.background,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetContent = { bottomSheetContent() }
    ) {
        PlayingScreen(
            scope = scope,
            scaffoldHide = scaffoldState::hide,
            scaffoldShow = {
                if (enableBottomSheet) navController.clearBackStack()
                navController.navigateTo(MainScreenData.Library.name)
                if (enableBottomSheet) scaffoldState.show()
            },
            onSongSelected = { mSongBrowser.playById(it.mediaId, true) },
            onSongShowDetail = {
                if (enableBottomSheet) navController.clearBackStack()
                navController.navigateSingle("${MainScreenData.SongsDetail.name}/${it.mediaId}")
                if (enableBottomSheet) scaffoldState.show()
            },
            onPlayPause = { mSongBrowser.togglePlay() },
            onPlayNext = { mSongBrowser.browser?.seekToNext() },
            onPlayPrevious = { mSongBrowser.browser?.seekToPrevious() },
            onSongMoveToNext = { mSongBrowser.addToNext(it.mediaId) },
            onSongRemoved = { mSongBrowser.removeById(it.mediaId) },
            onSeekToPosition = { mSongBrowser.browser?.seekTo(it.toLong()) }
        )
    }

    BackHandler(
        enabled = enableBottomSheet,
        onBack = {
            if (isVisible()) {
                scope.launch { scaffoldState.hide() }
            } else {
                onMoveTaskToBack()
            }
        }
    )
}