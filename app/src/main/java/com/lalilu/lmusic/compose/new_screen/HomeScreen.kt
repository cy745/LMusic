package com.lalilu.lmusic.compose.new_screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.lalilu.R
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lextension.component.ExtensionList
import com.lalilu.lmusic.viewmodel.LibraryViewModel

object HomeScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_home,
        icon = R.drawable.ic_loader_line
    )

    @Composable
    override fun Content() {
        val vm: LibraryViewModel = singleViewModel()

        LaunchedEffect(Unit) {
            vm.checkOrUpdateToday()
        }

        ExtensionList()
    }
}