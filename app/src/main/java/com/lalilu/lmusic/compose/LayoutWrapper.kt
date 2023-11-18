package com.lalilu.lmusic.compose

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.compositionUniqueId
import com.lalilu.component.base.HiddenBottomSheetScreen
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.extension.rememberIsPad
import com.lalilu.lmusic.compose.screen.ShowScreen

object LayoutWrapper {

    @OptIn(InternalVoyagerApi::class)
    @Composable
    fun Content(windowSize: WindowSizeClass = LocalWindowSize.current) {
        val configuration = LocalConfiguration.current
        val isPad by windowSize.rememberIsPad()
        val isLandscape by remember(configuration.orientation) {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }
        val defaultScreen = remember { HiddenBottomSheetScreen }

        DialogWrapper.Content {
            // 共用Navigator避免切换时导致导航栈丢失
            Navigator(
                defaultScreen,
                onBackPressed = null,
                key = compositionUniqueId()
            ) { navigator ->
                DrawerWrapper.Content(
                    isPad = { isPad },
                    isLandscape = { isLandscape },
                    mainContent = {
                        Playing.Content()
                    },
                    secondContent = {
                        NavigationWrapper.Content(
                            navigator = navigator,
                            defaultScreen = defaultScreen,
                            forPad = { isPad && isLandscape }
                        )
                    }
                )
            }
        }

        if (isLandscape) {
            ShowScreen()
        }
    }
}