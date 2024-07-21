package com.lalilu.lmusic.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.common.base.Playable
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.zhangke.krouter.KRouter


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
fun LazyListScope.latestPanel(
    libraryVM: LibraryViewModel,
    playingVM: PlayingViewModel
) {
    item {
        RecommendTitle(
            title = "最近添加",
            onClick = { }
        ) {
            Chip(onClick = { }) {
                Text(
                    style = MaterialTheme.typography.caption,
                    text = "所有歌曲"
                )
            }
        }
    }
    item {
        RecommendRow(
            items = { libraryVM.recentlyAdded.value },
            getId = { it.id }
        ) {
            RecommendCard(
                item = { it },
                width = { 100.dp },
                height = { 100.dp },
                modifier = Modifier.animateItem(),
                onClick = {
                    AppRouter.intent {
                        KRouter.route<Screen>("/song/detail") {
                            with("mediaId", it.mediaId)
                        }?.let(NavIntent::Push)
                    }
                },
                isPlaying = { playingVM.isItemPlaying(it.id, Playable::mediaId) },
                onClickButton = {
                    playingVM.play(
                        mediaId = it.id,
                        playOrPause = true,
                        addToNext = true
                    )
                }
            )
        }
    }
}
