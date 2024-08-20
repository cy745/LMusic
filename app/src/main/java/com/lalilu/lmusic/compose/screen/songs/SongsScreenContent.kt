package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.card.SongCard
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun SongsScreenContent(
    songsSM: SongsSM
) {
    val hapticFeedback = LocalHapticFeedback.current
    val listState: LazyListState = rememberLazyListState()
    val songs by songsSM.songs

    LaunchedEffect(Unit) {
        songsSM.event().collectLatest {
            when (it) {
                is SongsScreenEvent.ScrollToItem -> {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Text(text = "全部歌曲")

                val count = remember(songs) { songs.values.flatten().size }
                Text(text = "$count 首歌曲")
            }
        }

        songs.forEach { (group, list) ->
            item(
                key = group,
                contentType = "group"
            ) {
                Text(text = "$group")
            }

            items(
                items = list,
                key = { it.mediaId },
                contentType = { it::class.java }
            ) {
                SongCard(
                    song = { it },
                    onClick = { PlayerAction.PlayById(it.mediaId).action() },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                        AppRouter.route("/pages/songs/detail")
                            .with("mediaId", it.mediaId)
                            .jump()
                    },
                )
            }
        }

        smartBarPadding()
    }
}