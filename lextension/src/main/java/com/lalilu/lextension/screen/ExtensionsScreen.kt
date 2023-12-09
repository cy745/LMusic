package com.lalilu.lextension.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lextension.R
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import com.lalilu.component.R as ComponentR

class ExtensionsScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.extension_screen_title,
        icon = ComponentR.drawable.ic_user_line
    )

    @Composable
    override fun Content() {
        val extensionsSM = getScreenModel<ExtensionsScreenModel>()

        ExtensionsScreen(extensionsSM = extensionsSM)
    }
}

class ExtensionsScreenModel : ScreenModel {
    val extensions = MutableStateFlow<List<String>>(emptyList())
}

@Composable
private fun DynamicScreen.ExtensionsScreen(
    extensionsSM: ExtensionsScreenModel,
    playingVM: IPlayingViewModel = koinInject()
) {
    val navigator = koinInject<GlobalNavigator>()
    val extensionsState = extensionsSM.extensions.collectAsLoadingState()

    LoadingScaffold(
        modifier = Modifier.fillMaxSize(),
        targetState = extensionsState
    ) { extensions ->
        LLazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                NavigatorHeader(
                    modifier = Modifier.statusBarsPadding(),
                    title = stringResource(id = R.string.extension_screen_title),
                    subTitle = stringResource(id = R.string.extension_screen_title)
                )
            }
        }
    }
}