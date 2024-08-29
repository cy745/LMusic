package com.lalilu.lhistory.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.lhistory.R
import com.lalilu.lhistory.repository.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.annotation.Factory
import com.lalilu.component.R as ComponentR

data object HistoryScreen : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.history_screen_title,
        icon = ComponentR.drawable.ic_play_list_fill
    )

    @Composable
    override fun Content() {
        val historySM = getScreenModel<HistoryScreenModel>()

        HistoryScreen(historySM = historySM)
    }
}

@Factory
class HistoryScreenModel(
    historyRepo: HistoryRepository
) : ScreenModel {
    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaIds = historyRepo
        .getHistoriesIdsMapWithLastTime()
        .mapLatest { map ->
            map.toList()
                .sortedByDescending { it.second }
                .map { it.first }
        }
}

@Composable
private fun DynamicScreen.HistoryScreen(
    historySM: HistoryScreenModel
) {
    val mediaIdsState = historySM.mediaIds.collectAsLoadingState()

//    LoadingScaffold(
//        modifier = Modifier.fillMaxSize(),
//        targetState = mediaIdsState
//    ) { mediaIds ->
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
//    }
}