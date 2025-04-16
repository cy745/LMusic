package com.lalilu.lmusic.compose.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.loaderLine
import com.zhangke.krouter.annotation.Destination

@Destination("/pages/home")
object HomeScreen : TabScreen, Screen {
    private fun readResolve(): Any = HomeScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.screen_title_home) },
            icon = RemixIcon.System.loaderLine,
        )
    }

    @Composable
    override fun Content() {
        HomeScreenContent()
    }
}
