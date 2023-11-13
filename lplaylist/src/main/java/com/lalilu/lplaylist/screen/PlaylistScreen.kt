package com.lalilu.lplaylist.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lalilu.component.LLazyColumn
import com.lalilu.component.R
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.TabScreen
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistSp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.compose.koinInject


data object PlaylistScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = com.lalilu.lmedia.R.string.sort_rule_title,
        icon = R.drawable.ic_play_list_fill
    )

    @Composable
    override fun Content() {
        val sp: PlaylistSp = koinInject()

        PlaylistScreen(sp = sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DynamicScreen.PlaylistScreen(
    sp: PlaylistSp = koinInject()
) {
    val playlist = sp.obtainList<LPlaylist>("Playlist", autoSave = false)
    val state = rememberReorderableLazyListState(
        maxScrollPerFrame = 200.dp,
        onMove = { from, to ->
            val toIndex = playlist.value.indexOfFirst { it.id == to.key }
            val fromIndex = playlist.value.indexOfFirst { it.id == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyListState

            playlist.value = playlist.value.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        },
        canDragOver = { draggedOver, _ ->
            playlist.value.any { it.id == draggedOver.key }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            playlist.save()
        }
    }

    LLazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        item {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Playlist")

                IconButton(
                    onClick = {
                        val id = System.currentTimeMillis().toString()
                        playlist.add(
                            index = 0,
                            item = LPlaylist(
                                id = System.currentTimeMillis().toString(),
                                title = "Playlist_$id",
                                subTitle = "",
                                coverUri = "",
                                mediaIds = emptyList()
                            )
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_line),
                        contentDescription = null
                    )
                }
            }
        }
        items(
            items = playlist.value,
            key = { it.id },
            contentType = { LPlaylist::class.java }
        ) {
            ReorderableItem(
                defaultDraggingModifier = Modifier.animateItemPlacement(),
                state = state,
                key = it.id
            ) { isDragging ->
                PlaylistCard(
                    playlist = it,
                    isDragging = { isDragging }
                )
            }
        }
    }
}