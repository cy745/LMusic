package com.lalilu.lmusic.extension

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.lalilu.common.base.Playable
import com.lalilu.extension_core.Content
import com.lalilu.extension_core.Ext
import com.lalilu.extension_core.Extension
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.GlobalNavigator
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.utils.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Ext
class ExtHistory : Extension {
    override fun getContentMap(): Map<String, @Composable (Map<String, String>) -> Unit> = mapOf(
        Content.COMPONENT_HOME to { HomeContent() }
    )

    @Composable
    private fun HomeContent(
        playingVM: PlayingViewModel = singleViewModel(),
        historyVM: HistoryViewModel = singleViewModel()
    ) {
        val haptic = LocalHapticFeedback.current
        val itemsCount =
            remember { derivedStateOf { historyVM.historyState.value.size.coerceIn(0, 5) } }
        val itemsHeight = animateDpAsState(itemsCount.value * 85.dp, label = "")

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

        LazyColumn(
            modifier = Modifier
                .height(itemsHeight.value)
                .animateContentSize()
                .fillMaxWidth()
        ) {
            items(
                items = historyVM.historyState.value.take(5),
                key = { it.id },
                contentType = { LSong::class }
            ) { item ->
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
                        GlobalNavigator.goToDetailOf(mediaId = item.id)
                    }
                )
            }
        }
    }
}