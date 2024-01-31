package com.lalilu.lmusic.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.common.base.Playable
import com.lalilu.extension_core.Content
import com.lalilu.extension_core.Ext
import com.lalilu.extension_core.Extension
import com.lalilu.lmusic.GlobalNavigatorImpl
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Ext
class ExtLatest : Extension {
    override fun getContentMap(): Map<String, @Composable (Map<String, String>) -> Unit> = mapOf(
        Content.COMPONENT_HOME to { HomeContent() }
    )

    @Composable
    private fun HomeContent(
        libraryVM: LibraryViewModel = singleViewModel(),
        playingVM: PlayingViewModel = singleViewModel()
    ) {
        Column {
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

            RecommendRow(
                items = { libraryVM.recentlyAdded.value },
                getId = { it.id }
            ) {
                RecommendCard(
                    item = { it },
                    width = { 100.dp },
                    height = { 100.dp },
                    modifier = Modifier.animateItemPlacement(),
                    onClick = { GlobalNavigatorImpl.goToDetailOf(mediaId = it.id) },
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