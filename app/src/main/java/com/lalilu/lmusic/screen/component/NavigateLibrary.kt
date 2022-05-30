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
import androidx.navigation.NavHostController
import com.lalilu.lmusic.screen.LMusicNavGraph
import com.lalilu.lmusic.utils.WindowSize

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
fun RowScope.NavigateLibrary(
    currentWindowSize: WindowSize,
    navController: NavHostController,
    onExpendModal: () -> Unit,
    onPopUp: () -> Unit,
    onClose: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val navBarHeightDp = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val maxHeightDp = navBarHeightDp + configuration.screenHeightDp.dp

    Box(
        modifier = if (currentWindowSize != WindowSize.Compact) {
            Modifier
                .fillMaxSize()
                .weight(1f)
        } else {
            Modifier
                .fillMaxWidth()
                .height(maxHeightDp)
        }
    ) {
        StatusBarShadow(currentWindowSize = currentWindowSize)
        Row(modifier = Modifier.fillMaxSize()) {
            if (currentWindowSize == WindowSize.Expanded) {
                NavigatorRailBar(navController)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LMusicNavGraph(
                    navController = navController,
                    onExpendModal = onExpendModal,
                    currentWindowSize = currentWindowSize,
                    contentPaddingForFooter = navBarHeightDp + 64.dp,
                    modifier = if (currentWindowSize != WindowSize.Compact) {
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    } else {
                        Modifier.fillMaxSize()
                    }
                )
                NavigatorFooter(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    navController = navController,
                    popUp = onPopUp,
                    close = onClose
                )
            }
        }
    }
}

@Composable
fun BoxScope.StatusBarShadow(currentWindowSize: WindowSize) {
    if (currentWindowSize != WindowSize.Compact && MaterialTheme.colors.isLight) {
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
