package com.lalilu.lmusic.screen.component

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.screen.LMusicNavGraph
import com.lalilu.lmusic.screen.component.SmartBar.SmartBarContent

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun NavigateLibrary() {
    val configuration = LocalConfiguration.current
    val maxHeightDp = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding() + configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeightDp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LMusicNavGraph()
            SmartBarContent()
        }
    }
}

//@Composable
//fun RowScope.NavigateLibraryForPad(
//    onExpendModal: () -> Unit
//) {
//    val navController = LocalNavigatorHost.current
//    val windowSize = LocalWindowSize.current
//    val currentWindowSize = remember(windowSize.widthSizeClass) {
//        windowSize.widthSizeClass
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .weight(1f)
//    ) {
//        StatusBarShadow(enable = currentWindowSize == WindowWidthSizeClass.Expanded)
//
//        Row {
//            if (currentWindowSize == WindowWidthSizeClass.Expanded) {
//                NavigatorRailBar(navController)
//            }
//
//            Box(modifier = Modifier.fillMaxSize()) {
//                @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
//                LMusicNavGraph(
//                    onExpendModal = onExpendModal,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .statusBarsPadding()
//                )
//                SmartBarContent()
//            }
//        }
//    }
//}

@Composable
fun BoxScope.StatusBarShadow(enable: Boolean) {
    if (enable && MaterialTheme.colors.isLight) {
        Spacer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1A1A).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
