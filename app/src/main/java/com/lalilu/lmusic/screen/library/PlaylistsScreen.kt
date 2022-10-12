package com.lalilu.lmusic.screen.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmusic.screen.LibraryNavigateBar
import com.lalilu.lmusic.screen.ScreenActions
import com.lalilu.lmusic.screen.component.InputBar
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.button.TextWithIconButton
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import okhttp3.internal.toImmutableList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun PlaylistsScreen(
    mainViewModel: MainViewModel,
    playlistsViewModel: PlaylistsViewModel,
    libraryViewModel: LibraryViewModel
) {
    val context = LocalContext.current
    val playlistSelectHelper = mainViewModel.playlistSelectHelper
    val playlists by playlistsViewModel.playlists

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        println("from: $from to: $to stateList: ${playlists.size}")
        playlistsViewModel.movePlaylist(from, to)
//        val fromItem = playlists.getOrNull(from.index) ?: return@rememberReorderableLazyListState
//        val toItem = playlists.getOrNull(to.index) ?: return@rememberReorderableLazyListState
//        playlistsViewModel.swapTwoPlaylists(fromItem, toItem)
    })

    var creatingNewPlaylist by remember { mutableStateOf(false) }
    val navToPlaylistAction = ScreenActions.navToPlaylist()

    val onSelectPlaylist = playlistSelectHelper.onSelected {
        navToPlaylistAction.invoke(it.id)
    }

    playlistSelectHelper.registerBackHandler()
    playlistSelectHelper.listenIsSelectingChange {
        if (it) {
            SmartBar.setMainBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TextWithIconButton(
                        text = "取消",
                        color = Color(0xFF006E7C),
                        onClick = playlistSelectHelper.clear
                    )
                    Text(text = "已选择: ${playlistSelectHelper.selectedItem.size}")
                    TextWithIconButton(
                        text = "删除",
                        color = Color(0xFF006E7C),
                        onClick = {
                            val items = playlistSelectHelper.selectedItem.toImmutableList()
                            playlistsViewModel.removePlaylists(items)
                            playlistSelectHelper.clear()
                        }
                    )
                }
            }
        } else {
            SmartBar.setMainBar(item = LibraryNavigateBar)
        }

//        if (selectingAction == 0 && !it) {
//            navController.navigateUp()
//        }
    }

    LaunchedEffect(creatingNewPlaylist) {
        if (creatingNewPlaylist) {
            SmartBar.setExtraBar {
                createNewPlaylistBar(
                    onCancel = { creatingNewPlaylist = false },
                    onCommit = {
                        playlistsViewModel.createNewPlaylist(it)
                        creatingNewPlaylist = false
                    }
                )
            }
        } else {
            context.getActivity()?.let { KeyboardUtils.hideSoftInput(it) }
            SmartBar.setExtraBar(item = null)
        }
    }

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(state)
            .detectReorderAfterLongPress(state),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
//        item(key = "CREATE_PLAYLIST_BTN", contentType = "CREATE_PLAYLIST_BTN") {
//            Surface(
//                modifier = Modifier
//                    .padding(horizontal = 10.dp, vertical = 15.dp)
//                    .animateItemPlacement(),
//                color = Color.Transparent,
//                shape = RoundedCornerShape(10.dp),
//                border = BorderStroke(1.dp, dayNightTextColor(0.1f)),
//                onClick = { creatingNewPlaylist = !creatingNewPlaylist }
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 15.dp, vertical = 20.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(15.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_add_line),
//                        contentDescription = ""
//                    )
//
//                    Text(modifier = Modifier.weight(1f), text = "新建歌单")
//
//                    AnimatedContent(targetState = creatingNewPlaylist) {
//                        Icon(
//                            painter = painterResource(if (creatingNewPlaylist) R.drawable.ic_arrow_down_s_line else R.drawable.ic_arrow_up_s_line),
//                            contentDescription = ""
//                        )
//                    }
//                }
//            }
//        }
//
//        playlists.find { it._id == 0L }?.let {
//            item(key = it._id, contentType = LPlaylist::class) {
//                PlaylistCard(
//                    modifier = Modifier.animateItemPlacement(),
//                    getPlaylist = { it },
//                    icon = R.drawable.ic_heart_3_fill,
//                    iconTint = MaterialTheme.colors.primary,
//                    getIsSelected = playlistSelectHelper.isSelected,
//                    onClick = onSelectPlaylist,
//                    onLongClick = playlistSelectHelper.onSelected,
//                )
//            }
//        }

        itemsIndexed(
            items = playlists,
            key = { _, item -> item._id },
            contentType = { _, _ -> LPlaylist::class }
        ) { index, item ->
            ReorderableItem(
                state = state,
                key = item._id,
                index = index
            ) { isDragging ->
                PlaylistCard(
                    getPlaylist = { item },
                    getIsSelected = { isDragging },
                    onClick = onSelectPlaylist,
                    onLongClick = playlistSelectHelper.onSelected,
                )
            }
        }
    }
}

@Composable
fun createNewPlaylistBar(
    onCancel: () -> Unit = {},
    onCommit: (String) -> Unit = {}
) {
    val text = remember { mutableStateOf("") }
    val isCommitEnable by remember(text.value) { derivedStateOf { text.value.isNotEmpty() } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        InputBar(
            hint = "新建歌单",
            value = text,
            onCommit = {
                onCommit(it)
                text.value = ""
            }
        )
        IconButton(onClick = onCancel) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_line),
                contentDescription = "取消按钮"
            )
        }
        IconButton(
            onClick = {
                onCommit(text.value)
                text.value = ""
            }, enabled = isCommitEnable
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_line),
                contentDescription = "确认按钮"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.ic_play_list_fill,
    iconTint: Color = LocalContentColor.current,
    getPlaylist: () -> LPlaylist,
    getIsSelected: (LPlaylist) -> Boolean = { false },
    onClick: (LPlaylist) -> Unit = {},
    onLongClick: (LPlaylist) -> Unit = {}
) {
    val playlist = remember { getPlaylist() }
    val bgColor by animateColorAsState(if (getIsSelected(playlist)) dayNightTextColor(0.15f) else Color.Transparent)

    Surface(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp))
//            .combinedClickable(
//                onClick = { onClick(playlist) },
//                onLongClick = { onLongClick(playlist) }
//            )
        ,
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "",
                tint = iconTint.copy(alpha = 0.7f)
            )
            Text(
                modifier = Modifier.weight(1f),
                text = playlist.name,
                color = dayNightTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = "${playlist.songs.size} 首歌曲",
                color = dayNightTextColor(0.5f),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}
