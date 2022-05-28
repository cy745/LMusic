package com.lalilu.lmusic.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blankj.utilcode.util.ToastUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.IconResButton
import com.lalilu.lmusic.screen.component.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SortToggleButton
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel

@Composable
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
fun PlaylistsScreen(
    navigateUp: () -> Unit = {},
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp,
    viewModel: PlaylistsViewModel = hiltViewModel(),
    isAddingSongToPlaylist: Boolean = false,
    mediaIds: List<String> = emptyList()
) {
    val showDialog = remember { mutableStateOf(false) }
    val playlists by viewModel.playlists.observeAsState(initial = emptyList())

    var sortByState by rememberDataSaverState("KEY_SORT_BY_PlaylistsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_PlaylistsScreen", true)
    val selectedItems = remember { emptyList<MPlaylist>().toMutableStateList() }
    val sortedItems = remember(sortByState, sortDesc, playlists) {
        sort(sortByState, sortDesc, playlists.toMutableStateList(),
            getTextField = { it.playlistTitle },
            getTimeField = { it.playlistCreateTime.time }
        )
    }

    Column {
        NavigatorHeaderWithButtons(
            route = if (isAddingSongToPlaylist) MainScreenData.SongsAddToPlaylist else MainScreenData.Playlists
        ) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
            }
            IconResButton(
                iconRes = R.drawable.ic_play_list_add_line,
                alpha = 0.8f,
                onClick = { showDialog.value = true }
            )
            if (!isAddingSongToPlaylist) {
                IconResButton(
                    iconRes = R.drawable.ic_file_copy_2_line,
                    alpha = 0.8f,
                    onClick = { viewModel.copyCurrentPlayingPlaylist() }
                )
            } else {
                IconResButton(
                    iconRes = R.drawable.ic_check_line,
                    alpha = 0.8f,
                    onClick = {
                        if (selectedItems.isEmpty()) {
                            ToastUtils.showShort("未选择歌单")
                            return@IconResButton
                        }
                        viewModel.addSongsIntoPlaylists(
                            mediaIds,
                            selectedItems.map { it.playlistId })
                        ToastUtils.showShort("添加到歌单成功")
                        navigateUp()
                    }
                )
            }
        }
        PlaylistsColumns(
            selectedItems = selectedItems,
            sortedItems = sortedItems,
            contentPaddingForFooter = contentPaddingForFooter,
            onLongClickItem = {
                if (selectedItems.contains(it)) {
                    selectedItems.remove(it)
                }
                viewModel.removePlaylist(it)
            },
            onClickItem = {
                if (isAddingSongToPlaylist) {
                    if (selectedItems.contains(it))
                        selectedItems.remove(it)
                    else selectedItems.add(it)
                } else {
                    navigateTo("${MainScreenData.PlaylistsDetail.name}/${it.playlistId}")
                }
            }
        )
    }
    if (showDialog.value) {
        var playlistTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    if (playlistTitle.isNotEmpty()) {
                        viewModel.createNewPlaylist(playlistTitle)
                        showDialog.value = false
                    } else {
                        ToastUtils.showShort("歌单名不可为空")
                    }
                }) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = "取消")
                }
            },
            title = {
                Text(text = "新建歌单")
            },
            text = {
                BasicTextField(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .fillMaxWidth()
                        .background(color = Color.LightGray)
                        .padding(vertical = 10.dp, horizontal = 15.dp),
                    textStyle = MaterialTheme.typography.body2,
                    value = playlistTitle,
                    onValueChange = {
                        playlistTitle = it
                    })
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.PlaylistsColumns(
    sortedItems: List<MPlaylist>,
    selectedItems: List<MPlaylist> = emptyList(),
    contentPaddingForFooter: Dp = 0.dp,
    onClickItem: (MPlaylist) -> Unit,
    onLongClickItem: (MPlaylist) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f),
        contentPadding = PaddingValues(bottom = contentPaddingForFooter)
    ) {
        items(sortedItems) {
            PlaylistCard(
                title = it.playlistTitle,
                modifier = Modifier.animateItemPlacement(),
                onClick = { onClickItem(it) },
                onLongClick = { onLongClickItem(it) },
                selected = selectedItems.contains(it)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    val color: Color by animateColorAsState(
        if (selected) contentColorFor(
            backgroundColor = MaterialTheme.colors.background
        ).copy(0.2f) else Color.Transparent
    )

    Surface(color = color) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(20.dp)
        ) {
            Text(
                text = title,
                color = textColor
            )
        }
    }
}
