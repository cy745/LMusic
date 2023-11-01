package com.lalilu.lmusic.compose.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.compose.DynamicScreen
import com.lalilu.lmusic.compose.ScreenInfo
import com.lalilu.lmusic.compose.TabScreen
import com.lalilu.lmusic.compose.component.LLazyColumn
import com.lalilu.lmusic.compose.new_screen.SelectPanelWrapper
import com.lalilu.lmusic.datastore.TempSp
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lplaylist.LPlaylist
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.compose.koinInject

data object PlaylistScreen : DynamicScreen(), TabScreen {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_playlist,
        icon = R.drawable.ic_play_list_fill
    )

    @Composable
    override fun Content() {
        PlaylistScreen()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DynamicScreen.PlaylistScreen(
    tempSp: TempSp = koinInject()
) {
    val playlist = tempSp.obtainList<LPlaylist>("Playlist", autoSave = false)
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        playlist.value = playlist.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })

    DisposableEffect(Unit) {
        onDispose {
            playlist.save()
        }
    }

    SelectPanelWrapper {
        LLazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
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
                    val elevation =
                        animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")

                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .height(56.dp)
                            .background(MaterialTheme.colors.surface)
                    ) {
                        Text(
                            text = "${it.title}-${it.subTitle}",
                            color = dayNightTextColor()
                        )
                    }
                }
            }
        }
    }
}