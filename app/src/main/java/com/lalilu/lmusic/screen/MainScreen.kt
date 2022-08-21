package com.lalilu.lmusic.screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lalilu.lmusic.screen.component.NavigateLibrary
import com.lalilu.lmusic.screen.component.SmartModalBottomSheet
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun MainScreen() {
    SmartModalBottomSheet.SmartModalBottomSheetContent(
        context = LocalContext.current,
        navController = LocalNavigatorHost.current,
        sheetContent = { NavigateLibrary() },
        content = { PlayingScreen() }
    )
}

//@Composable
//@OptIn(ExperimentalMaterialApi::class)
//fun MainScreenForCompat(
//    currentWindowSizeClass: WindowSizeClass,
//    navController: NavHostController,
//    scaffoldState: ModalBottomSheetState,
//    scope: CoroutineScope = rememberCoroutineScope(),
//    bottomSheetContent: @Composable () -> Unit = {},
//) {
//    val isEnableBottomSheet = currentWindowSizeClass.windowSize == Compact
//    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
//            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
//            WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()
//    val screenHeight = LocalDensity.current.run { screenHeightDp.toPx() }
//    val isVisible = { scaffoldState.offset.value < screenHeight }
//
//    val backgroundColor = MaterialTheme.colors.background
//    val elevation = if (!isEnableBottomSheet) 5.dp else 0.dp
//    val color = if (!isEnableBottomSheet) backgroundColor else Color.Transparent
//
//    val showScaffold: suspend () -> Unit = {
//        /**
//         * 在直接从Composable的参数传入的情况下，
//         * 不知为何不能在此函数内获取[currentWindowSizeClass]的最新值，
//         * 故直接从单例中获取该值
//         */
//        if (WindowSizeClass.instance?.windowSize == Compact)
//            scope.safeLaunch { scaffoldState.show() }
//    }
//    val scaffoldShow: suspend () -> Unit = {
//        navController.navigate(
//            from = MainScreenData.Library.name,
//            to = MainScreenData.Library.name,
//            clearAllBefore = isEnableBottomSheet
//        )
//        showScaffold()
//    }
//    val onSongShowDetail: suspend (MediaItem) -> Unit = {
//        navController.navigate(
//            from = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
//            to = "${MainScreenData.SongsDetail.name}/${it.mediaId}",
//            clearAllBefore = isEnableBottomSheet
//        )
//        showScaffold()
//    }
//
//    ModalBottomSheetLayout(
//        modifier = when (currentWindowSizeClass.deviceType) {
//            Phone -> when (currentWindowSizeClass.windowSize) {
//                Compact -> Modifier.fillMaxWidth()
//                Medium -> Modifier.fillMaxWidth(0.5f)
//                Expanded -> Modifier.fillMaxWidth(0.5f)
//            }
//            Pad -> when (currentWindowSizeClass.windowSize) {
//                Compact -> Modifier.fillMaxWidth(0.5f)
//                Medium -> Modifier.fillMaxWidth(0.5f)
//                Expanded -> Modifier.width(screenHeightDp / 2f)
//            }
//        },
//        sheetState = scaffoldState,
//        sheetBackgroundColor = backgroundColor,
//        scrimColor = Color.Black.copy(alpha = 0.5f),
//        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
//        sheetContent = { bottomSheetContent() }
//    ) {
//        Surface(
//            elevation = elevation,
//            color = color
//        ) {
//            PlayingScreen(
//                scope = scope,
//                onSongShowDetail = onSongShowDetail,
//                onExpendBottomSheet = scaffoldShow,
//                onCollapseBottomSheet = scaffoldState::hide,
//            )
//        }
//    }
//
//    val context = LocalContext.current
//    BackHandlerWithNavigator(
//        navController = navController,
//        onBack = {
//            if (isVisible() && isEnableBottomSheet) {
//                scope.safeLaunch { scaffoldState.hide() }
//            } else {
//                context.getActivity()?.moveTaskToBack(false)
//            }
//        }
//    )
//}

