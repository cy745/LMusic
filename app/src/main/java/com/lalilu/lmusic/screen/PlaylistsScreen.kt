package com.lalilu.lmusic.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.bean.sort
import com.lalilu.lmusic.screen.component.IconResButton
import com.lalilu.lmusic.screen.component.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SortToggleButton
import com.lalilu.lmusic.screen.viewmodel.PlaylistsViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PlaylistsScreen(
    navigateTo: (destination: String) -> Unit = {},
    contentPaddingForFooter: Dp = 0.dp,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    var sortByState by rememberDataSaverState("KEY_SORT_BY_PlaylistsScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_PlaylistsScreen", false)
    val playlists by viewModel.playlists.observeAsState(initial = emptyList())
    val sortedItems = remember(playlists) { playlists.toMutableStateList() }

    val sort = {
        sort(sortByState, sortDesc, sortedItems,
            getTextField = { it.playlistTitle },
            getTimeField = { it.playlistCreateTime.time }
        )
    }

    Column {
        NavigatorHeaderWithButtons(route = MainScreenData.Playlists) {
            LazyListSortToggleButton(sortByState = sortByState) {
                sortByState = next(sortByState)
                sort()
            }
            SortToggleButton(sortDesc = sortDesc) {
                sortDesc = !sortDesc
                sort()
            }
            IconResButton(
                iconRes = R.drawable.ic_play_list_add_line,
                alpha = 0.8f,
                onClick = { viewModel.createNewPlaylist() }
            )
            IconResButton(
                iconRes = R.drawable.ic_file_copy_2_line,
                alpha = 0.8f,
                onClick = { viewModel.copyCurrentPlayingPlaylist() }
            )
        }
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
                    onClick = { navigateTo("${MainScreenData.PlaylistDetail.name}/${it.playlistId}") },
                    onLongClick = { viewModel.removePlaylist(it) }
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(20.dp)
    ) {
        Text(text = title)
    }
}
