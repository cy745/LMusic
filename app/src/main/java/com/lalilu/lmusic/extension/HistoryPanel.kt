package com.lalilu.lmusic.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.common.base.Playable
import com.lalilu.component.card.SongCard
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
fun LazyListScope.historyPanel(
    historyVM: HistoryViewModel,
    playingVM: PlayingViewModel
) {
    item {
        RecommendTitle(
            title = "最近播放",
            onClick = { }
        ) {
            Chip(
                onClick = {
                    // navigator.navigate(HistoryScreenDestination)
                },
            ) {
                Text(style = MaterialTheme.typography.caption, text = "历史记录")
            }
        }
    }

    items(
        items = historyVM.historyState.value.take(5),
        key = { it.id },
        contentType = { LSong::class }
    ) { item ->
        val haptic = LocalHapticFeedback.current

        SongCard(
            modifier = Modifier
                .animateItemPlacement()
                .padding(bottom = 5.dp),
            song = { item },
            fixedHeight = { true },
            isSelected = { false },
            onEnterSelect = { },
            isPlaying = { playingVM.isItemPlaying { it.mediaId == item.id } },
            onClick = {
                historyVM.requiteHistoryList {
                    playingVM.play(
                        mediaId = item.mediaId,
                        mediaIds = it.map(Playable::mediaId),
                        playOrPause = true
                    )
                }
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                GlobalNavigatorImpl.goToDetailOf(mediaId = item.id)
            }
        )
    }
}
