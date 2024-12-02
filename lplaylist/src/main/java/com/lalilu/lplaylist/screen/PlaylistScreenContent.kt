package com.lalilu.lplaylist.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lalilu.RemixIcon
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.addLargeLine
import com.lalilu.remixicon.system.search2Line
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun PlaylistScreenContent(
    modifier: Modifier = Modifier,
    isSearching: () -> Boolean = { true },
    onStartSearch: () -> Unit = {},
    isSelecting: () -> Boolean = { true },
    isSelected: (LPlaylist) -> Boolean = { false },
    playlists: () -> List<LPlaylist> = { emptyList() },
    onUpdatePlaylist: (List<LPlaylist>) -> Unit = {},
    onClickPlaylist: (LPlaylist) -> Unit = {},
    onLongClickPlaylist: (LPlaylist) -> Unit = {}
) {
    val listState: LazyListState = rememberLazyListState()
    val playlistState = remember(playlists()) {
        playlists().toMutableStateList()
    }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState
    ) { from, to ->
        playlistState.toMutableList().apply {
            val toIndex = indexOfFirst { it.id == to.key }
            val fromIndex = indexOfFirst { it.id == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyListState

            add(toIndex, removeAt(fromIndex))
            playlistState.clear()
            playlistState.addAll(this)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "HEADER") {
            NavigatorHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                rowExtraSpace = 8.dp,
                paddingValues = PaddingValues(
                    top = 26.dp,
                    bottom = 20.dp,
                    start = 20.dp,
                    end = 12.dp
                ),
                title = stringResource(id = R.string.playlist_screen_title)
            ) {
                IconButton(onClick = { AppRouter.route("/pages/playlist/create_or_edit").push() }) {
                    Icon(
                        imageVector = RemixIcon.System.addLargeLine,
                        contentDescription = null
                    )
                }

                Box {
                    IconButton(onClick = onStartSearch) {
                        Icon(
                            imageVector = RemixIcon.System.search2Line,
                            contentDescription = null
                        )
                    }

                    this@NavigatorHeader.AnimatedVisibility(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(8.dp, 8.dp),
                        enter = fadeIn(),
                        exit = fadeOut(),
                        visible = isSearching()
                    ) {
                        Spacer(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color = Color.Red)
                                .size(8.dp)
                        )
                    }
                }
            }
        }

        items(
            items = playlistState,
            key = { it.id },
            contentType = { LPlaylist::class.java }
        ) { playlist ->
            ReorderableItem(
                state = reorderableState,
                key = playlist.id
            ) { isDragging ->
                PlaylistCard(
                    playlist = playlist,
                    draggingModifier = Modifier.draggableHandle(
                        onDragStopped = { onUpdatePlaylist(playlistState) }
                    ),
                    isDragging = { isDragging },
                    isSelected = { isSelected(playlist) },
                    isSelecting = isSelecting,
                    onClick = onClickPlaylist,
                    onLongClick = onLongClickPlaylist
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistScreenContentPreview() {
    MaterialTheme {
        PlaylistScreenContent(
            playlists = {
                buildList<LPlaylist> {
                    repeat(10) {
                        add(
                            LPlaylist(
                                id = "$it",
                                title = "Playlist $it",
                                subTitle = "Subtitle $it",
                                coverUri = "",
                                mediaIds = listOf("", "")
                            )
                        )
                    }
                }
            }
        )
    }
}