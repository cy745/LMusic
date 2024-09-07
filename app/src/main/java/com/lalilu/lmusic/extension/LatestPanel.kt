package com.lalilu.lmusic.extension

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.common.base.Playable
import com.lalilu.component.LazyGridContent
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import org.koin.compose.koinInject


object LatestPanel : LazyGridContent {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        val libraryVM: LibraryViewModel = koinInject()
        val playingVM: PlayingViewModel = koinInject()
        val items by libraryVM.recentlyAdded

        return fun LazyGridScope.() {
            // 若列表为空，不显示
            if (items.isEmpty()) return

            item(
                key = "latest_header",
                contentType = "latest_header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
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

            item(
                key = "latest",
                contentType = "latest",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                RecommendRow(
                    items = { items },
                    getId = { it.id }
                ) {
                    RecommendCard(
                        item = { it },
                        width = { 100.dp },
                        height = { 100.dp },
                        modifier = Modifier.animateItem(),
                        onClick = {
                            AppRouter.route("/pages/songs/detail")
                                .with("mediaId", it.mediaId)
                                .jump()
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
    }
}