package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.base.InputBar
import com.lalilu.lmusic.compose.component.card.PlaylistCard
import com.lalilu.lmusic.compose.screen.LibraryNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import okhttp3.internal.toImmutableList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun PlaylistsScreen(
    isAddingSongs: Boolean = false,
    mainVM: MainViewModel,
    playlistsVM: PlaylistsViewModel,
    libraryVM: LibraryViewModel
) {
    val context = LocalContext.current
    val navigator = LocalNavigatorHost.current
    val navToPlaylistAction = ScreenActions.navToPlaylist()
    val state = rememberReorderableLazyListState(
        onMove = playlistsVM::onMovePlaylist,
        canDragOver = playlistsVM::canDragOver,
        onDragEnd = playlistsVM::onDragEnd
    )
    val selectedItems = remember { mutableStateListOf<LPlaylist>() }
    val selector = rememberSelectState(
        defaultState = isAddingSongs,
        selectedItems = selectedItems,
        onExitSelect = {
            if (isAddingSongs) {
                navigator.navigateUp()
            }
        }
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
                    if (isAddingSongs) {
                        Text(text = "${mainVM.tempSongs.size}首歌 -> ${selectedItems.size}歌单")
                        IconTextButton(
                            text = "确认保存",
                            color = Color(0xFF3EA22C),
                            onClick = {
                                playlistsVM.addSongsIntoPlaylists(
                                    playlists = selectedItems.toImmutableList(),
                                    songs = mainVM.tempSongs.toImmutableList()
                                )
                                selector.clear()
                            }
                        )
                    } else {
                        Text(text = "已选择 ${selectedItems.size}")
                        IconTextButton(
                            text = "删除",
                            color = Color(0xFF006E7C),
                            onClick = {
                                playlistsVM.removePlaylists(selectedItems)
                                selector.clear()
                            }
                        )
                    }
                }
            }
        } else {
            if (!isAddingSongs) {
                SmartBar.setMainBar(item = LibraryNavigateBar)
            }
        }
    }

    var creating by remember { mutableStateOf(false) }
    LaunchedEffect(creating) {
        if (creating) {
            SmartBar.setExtraBar {
                createNewPlaylistBar(
                    onCancel = { creating = false },
                    onCommit = {
                        playlistsVM.createNewPlaylist(it)
                        creating = false
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
                onClick = { creating = !creating }
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

                    AnimatedContent(targetState = creating) {
                        Icon(
                            painter = painterResource(if (creating) R.drawable.ic_arrow_down_s_line else R.drawable.ic_arrow_up_s_line),
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
        ) { item ->
            ReorderableItem(
                defaultDraggingModifier = Modifier.animateItemPlacement(),
                state = state,
                key = item._id
            ) { isDragging ->
                if (item._id == 0L) {
                    PlaylistCard(
                        icon = R.drawable.ic_heart_3_fill,
                        iconTint = MaterialTheme.colors.primary,
                        onClick = {
                            if (selector.isSelecting.value) {
                                selector.onSelected(item)
                            } else {
                                navToPlaylistAction(item.id)
                            }
                        },
                        dragModifier = Modifier.detectReorder(state),
                        getPlaylist = { item },
                        onLongClick = { selector.onSelected(item) },
                        getIsSelected = { isDragging || selectedItems.any { it._id == item._id } }
                    )
                } else {
                    PlaylistCard(
                        onClick = {
                            if (selector.isSelecting.value) {
                                selector.onSelected(item)
                            } else {
                                navToPlaylistAction(item.id)
                            }
                        },
                        dragModifier = Modifier.detectReorder(state),
                        getPlaylist = { item },
                        onLongClick = { selector.onSelected(item) },
                        getIsSelected = { isDragging || selectedItems.any { it._id == item._id } }
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