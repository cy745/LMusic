package com.lalilu.ldictionary.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.ldictionary.R
import kotlinx.coroutines.flow.MutableStateFlow
import com.lalilu.component.R as ComponentR

object DictionaryScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.dictionary_screen_title,
        icon = ComponentR.drawable.ic_disc_line
    )

    @Composable
    override fun Content() {
        val historySM = getScreenModel<DictionaryScreenModel>()

        HistoryScreen(dictionarySM = historySM)
    }
}

class DictionaryScreenModel(
) : ScreenModel {
    val dictionary = MutableStateFlow("")
}

@Composable
private fun DynamicScreen.HistoryScreen(
    dictionarySM: DictionaryScreenModel
) {
    val dictionaryState = dictionarySM.dictionary.collectAsLoadingState()

    LoadingScaffold(
        modifier = Modifier.fillMaxSize(),
        targetState = dictionaryState
    ) { mediaIds ->
//        Songs(
//            modifier = Modifier.fillMaxSize(),
//            mediaIds = mediaIds,
//            supportListAction = { listOf() },
//            headerContent = {
//                item {
//                    NavigatorHeader(title = stringResource(id = R.string.history_screen_title))
//                }
//            },
//            footerContent = {}
//        )
    }
}