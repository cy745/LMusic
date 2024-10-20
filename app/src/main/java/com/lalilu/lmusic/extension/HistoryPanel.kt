package com.lalilu.lmusic.extension

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.component.LazyGridContent
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.card.SongCard
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.MPlayer
import org.koin.compose.koinInject

object HistoryPanel : LazyGridContent {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        val historyVM: HistoryViewModel = koinInject()
        val playingVM: PlayingViewModel = koinInject()
        val widthSizeClass = LocalWindowSize.current.widthSizeClass
        val haptic = LocalHapticFeedback.current
        val items = historyVM.historyState.value

        return fun LazyGridScope.() {
            // 若列表为空，则不显示
            if (items.isEmpty()) return

            item(span = { GridItemSpan(maxLineSpan) }) {
                RecommendTitle(
                    title = "最近播放",
                    onClick = { }
                ) {
                    Chip(
                        onClick = { // navigator.navigate(HistoryScreenDestination)
                        },
                    ) {
                        Text(style = MaterialTheme.typography.caption, text = "历史记录")
                    }
                }
            }

            items(
                items = items,
                key = { it.id },
                contentType = { "History_item" },
                span = {
                    if (widthSizeClass == WindowWidthSizeClass.Expanded) GridItemSpan(maxLineSpan / 2)
                    else GridItemSpan(maxLineSpan)
                }
            ) { item ->
                SongCard(
                    modifier = Modifier
                        .animateItem()
                        .padding(bottom = 5.dp),
                    song = { item },
                    isPlaying = { MPlayer.isItemPlaying(item.id) },
                    onClick = {
                        playingVM.play(
                            mediaId = item.id,
                            mediaIds = items.map(LSong::id),
                            playOrPause = true
                        )
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        AppRouter.route("/pages/songs/detail")
                            .with("mediaId", item.id)
                            .jump()
                    }
                )
            }
        }
    }
}