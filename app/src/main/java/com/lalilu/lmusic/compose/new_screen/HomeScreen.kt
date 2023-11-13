package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lalilu.R
import com.lalilu.extension_core.Content
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.component.LLazyColumn
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel

object HomeScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_home,
        icon = R.drawable.ic_loader_line
    )

    @Composable
    override fun Content() {
        HomeScreen()
    }
}

@Composable
private fun HomeScreen(
    vm: LibraryViewModel = singleViewModel(),
) {
    val extensionResult by vm.extensionResult

    LaunchedEffect(Unit) {
        vm.checkOrUpdateToday()
    }

    LLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = extensionResult) {
            it.Place(contentKey = Content.COMPONENT_HOME)
        }
    }
}
