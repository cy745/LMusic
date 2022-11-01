package com.lalilu.lmusic.compose.screen.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.LibraryNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.buildScrollToItemAction
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel

@Composable
fun SongsScreen(
    mainVM: MainViewModel,
    libraryVM: LibraryViewModel,
) {
    val windowSize = LocalWindowSize.current
    val gridState = rememberLazyGridState()

    val navToSongAction = ScreenActions.navToSong(hapticType = HapticFeedbackType.LongPress)
    val navToAddToPlaylist = mainVM.navToAddToPlaylist()

    val songs by libraryVM.songs.observeAsState(emptyList())
    val currentPlaying by LMusicRuntime.playingLiveData.observeAsState()

    val scrollAction = buildScrollToItemAction(
        target = currentPlaying,
        getIndex = { songs.indexOfFirst { it.id == currentPlaying!!.id } },
        state = gridState
    )

    val selectedItems = remember { mutableStateListOf<LSong>() }
    val selector = rememberSelectState(
        defaultState = false,
        selectedItems = selectedItems
    )

    LaunchedEffect(selector.isSelecting.value) {
        if (selector.isSelecting.value) {
            SmartBar.setMainBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconTextButton(
                        text = "取消",
                        color = Color(0xFF006E7C),
                        onClick = { selector.clear() }
                    )
                    Text(text = "已选择: ${selectedItems.size}")
                    IconTextButton(
                        text = "添加到歌单",
                        color = Color(0xFF006E7C),
                        onClick = { navToAddToPlaylist(selectedItems) }
                    )
                }
            }
        } else {
            SmartBar.setMainBar(item = LibraryNavigateBar)
        }
    }

    SmartContainer.LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val now = System.currentTimeMillis()
        songs.groupBy { song ->
            song.dateAdded?.let { it - (it % 60) }?.times(1000L) ?: 0L
        }.forEach { (dateAdded, list) ->
            item(
                key = dateAdded,
                contentType = LSong::dateAdded,
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Text(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 10.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    style = MaterialTheme.typography.h6,
                    text = when {
                        now - dateAdded < 300000 -> "刚刚"
                        now - dateAdded < 3600000 -> "${(now - dateAdded) / 60000}分钟前"
                        now - dateAdded < 86400000 -> "${(now - dateAdded) / 3600000}小时前"
                        else -> TimeUtils.millis2String(dateAdded, "M月d日 HH:mm")
                    }
                )
            }
            itemsIndexed(
                items = list,
                key = { _, item -> item.id },
                contentType = { _, _ -> LSong::class }
            ) { index, item ->
                SongCard(
                    song = { item },
                    onClick = {
                        if (selector.isSelecting.value) {
                            selector.onSelected(item)
                        } else {
                            mainVM.playSongWithPlaylist(songs, item)
                        }
                    },
                    onLongClick = { navToSongAction(item.id) },
                    onEnterSelect = { selector.onSelected(item) },
                    isSelected = { selectedItems.any { it.id == item.id } }
                )
            }
        }
    }
}