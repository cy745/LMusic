package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.common.base.Playable
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.StickyHeaderOffsetHelper
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SongsScreenContent(
    songsSM: SongsSM,
    isSelecting: () -> Boolean = { false },
    isSelected: (Playable) -> Boolean = { false },
    onSelect: (Playable) -> Unit = {},
    onClickGroup: (GroupIdentity) -> Unit = {}
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val listState: LazyListState = rememberLazyListState()
    val statusBar = WindowInsets.statusBars
    val statusBarHeight = remember(statusBar, density) { statusBar.getTop(density) }
    val songs by songsSM.songs

    LaunchedEffect(Unit) {
        songsSM.event().collectLatest {
            when (it) {
                is SongsScreenEvent.ScrollToItem -> {
                    listState.scrollToItem(
                        index = it.index,
                        scrollOffset = -statusBarHeight
                    )
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        startRecord(songsSM.recorder) {
            itemWithRecord(key = "全部歌曲") {
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
                stickyHeaderWithRecord(
                    key = group,
                    contentType = "group"
                ) {
                    StickyHeaderOffsetHelper(
                        key = group,
                        listState = listState,
                        minOffset = statusBarHeight
                    ) { modifier, isFloating ->
                        Text(
                            modifier = modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .widthIn(min = 64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onClickGroup(group) }
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colors.onBackground.copy(0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(color = MaterialTheme.colors.background)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            text = group.text
                        )
                    }
                }

                itemsWithRecord(
                    items = list,
                    key = { it.mediaId },
                    contentType = { it::class.java }
                ) {
                    SongCard(
                        song = { it },
                        isSelected = { isSelected(it) },
                        onClick = {
                            if (isSelecting()) {
                                onSelect(it)
                            } else {
                                PlayerAction.PlayById(it.mediaId).action()
                            }
                        },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                            if (isSelecting()) {
                                onSelect(it)
                            } else {
                                AppRouter.route("/pages/songs/detail")
                                    .with("mediaId", it.mediaId)
                                    .jump()
                            }
                        },
                        onEnterSelect = { onSelect(it) }
                    )
                }
            }
        }

        smartBarPadding()
    }
}