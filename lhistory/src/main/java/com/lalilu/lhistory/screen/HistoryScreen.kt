package com.lalilu.lhistory.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lhistory.component.HistoryItemCard
import com.lalilu.lhistory.entity.LHistory
import com.lalilu.lhistory.viewmodel.HistoryVM
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.remixicon.Editor
import com.lalilu.remixicon.editor.draggable
import org.koin.compose.koinInject

data object HistoryScreen : Screen, ScreenInfoFactory {
    private fun readResolve(): Any = HistoryScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo {
        return remember {
            ScreenInfo(
                title = { "History" },
                icon = RemixIcon.Editor.draggable
            )
        }
    }

    @Composable
    override fun Content() {
        val historyVM = koinInject<HistoryVM>()
        val items = historyVM.pager.collectAsLazyPagingItems()

        HistoryScreenContent(
            items = items
        )
    }
}

@Composable
private fun HistoryScreenContent(
    items: LazyPagingItems<LHistory>
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item(key = "历史记录") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "历史记录",
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    text = "播放过的歌曲记录",
                    color = MaterialTheme.colors.onBackground.copy(0.6f),
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                )
            }
        }

        items(
            count = items.itemCount,
            key = items.itemKey { it.id }
        ) { index ->
            val item = items[index]

            HistoryItemCard(
                modifier = Modifier.animateItem(),
                imageData = { LMedia.get<LSong>(item?.contentId) },
                title = { item?.contentTitle ?: "" },
                startTime = { item?.startTime ?: System.currentTimeMillis() },
                duration = { item?.duration ?: 0 },
                repeatCount = { item?.repeatCount ?: 0 },
                onClick = {
                    AppRouter.route("/pages/songs/detail")
                        .with("mediaId", item?.contentId)
                        .jump()
                }
            )
        }

        if (items.loadState.append == LoadState.Loading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        smartBarPadding()
    }
}