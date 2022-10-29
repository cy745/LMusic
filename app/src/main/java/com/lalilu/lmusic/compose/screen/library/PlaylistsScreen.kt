package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.base.InputBar
import com.lalilu.lmusic.compose.screen.LibraryNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import okhttp3.internal.toImmutableList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun PlaylistsScreen(
    mainViewModel: MainViewModel,
    playlistsVM: PlaylistsViewModel,
    libraryViewModel: LibraryViewModel
) {
    val context = LocalContext.current
    val playlistSelectHelper = mainViewModel.playlistSelectHelper

    val state = rememberReorderableLazyListState(
        onMove = playlistsVM::onMovePlaylist,
        canDragOver = playlistsVM::canDragOver,
        onDragEnd = playlistsVM::onDragEnd
    )

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
                    IconTextButton(
                        text = "取消",
                        color = Color(0xFF006E7C),
                        onClick = playlistSelectHelper.clear
                    )
                    Text(text = "已选择: ${playlistSelectHelper.selectedItem.size}")
                    IconTextButton(
                        text = "删除",
                        color = Color(0xFF006E7C),
                        onClick = {
                            val items = playlistSelectHelper.selectedItem.toImmutableList()
                            playlistsVM.removePlaylists(items)
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
                        playlistsVM.createNewPlaylist(it)
                        creatingNewPlaylist = false
                    }
                )
            }
        } else {
            context.getActivity()?.let { KeyboardUtils.hideSoftInput(it) }
            SmartBar.setExtraBar(item = null)
        }
    }

    SmartContainer.LazyColumn(
        state = state.listState,
        modifier = Modifier
            .recomposeHighlighter()
            .fillMaxSize()
            .reorderable(state),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "CREATE_PLAYLIST_BTN", contentType = "CREATE_PLAYLIST_BTN") {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 15.dp)
                    .animateItemPlacement(),
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, dayNightTextColor(0.1f)),
                onClick = { creatingNewPlaylist = !creatingNewPlaylist }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_line),
                        contentDescription = ""
                    )

                    Text(modifier = Modifier.weight(1f), text = "新建歌单")

                    AnimatedContent(targetState = creatingNewPlaylist) {
                        Icon(
                            painter = painterResource(if (creatingNewPlaylist) R.drawable.ic_arrow_down_s_line else R.drawable.ic_arrow_up_s_line),
                            contentDescription = ""
                        )
                    }
                }
            }
        }

        items(
            items = playlistsVM.playlists,
            key = { it._id },
            contentType = { LPlaylist::class }
        ) {
            ReorderableItem(
                defaultDraggingModifier = Modifier.animateItemPlacement(),
                state = state,
                key = it._id
            ) { isDragging ->
                if (it._id == 0L) {
                    PlaylistCard(
                        dragModifier = Modifier.detectReorderAfterLongPress(state),
                        icon = R.drawable.ic_heart_3_fill,
                        iconTint = MaterialTheme.colors.primary,
                        getPlaylist = { it },
                        getIsSelected = { isDragging || playlistSelectHelper.isSelected(it) },
                        onClick = { onSelectPlaylist(it) },
                        onLongClick = { playlistSelectHelper.onSelected(it) },
                    )
                } else {
                    PlaylistCard(
                        dragModifier = Modifier.detectReorderAfterLongPress(state),
                        getPlaylist = { it },
                        getIsSelected = { isDragging || playlistSelectHelper.isSelected(it) },
                        onClick = { onSelectPlaylist(it) },
                        onLongClick = { playlistSelectHelper.onSelected(it) },
                    )
                }
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
            modifier = Modifier.weight(1f),
            hint = "新建歌单",
            value = text,
            onSubmit = {
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
    dragModifier: Modifier = Modifier,
    icon: Int = R.drawable.ic_play_list_fill,
    iconTint: Color = LocalContentColor.current,
    getPlaylist: () -> LPlaylist,
    getIsSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val playlist = remember { getPlaylist() }
    val bgColor by animateColorAsState(if (getIsSelected()) dayNightTextColor(0.15f) else Color.Transparent)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 15.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Icon(
            modifier = dragModifier,
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
            modifier = dragModifier.padding(start = 20.dp),
            text = "${playlist.songs.size} 首歌曲",
            color = dayNightTextColor(0.5f),
            style = MaterialTheme.typography.subtitle2
        )
    }
}
