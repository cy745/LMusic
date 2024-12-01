package com.lalilu.lplaylist.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.RemixIcon
import com.lalilu.component.LLazyColumn
import com.lalilu.component.LongClickableTextButton
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import com.lalilu.lplaylist.screen.create.PlaylistCreateOrEditScreen
import com.lalilu.lplaylist.screen.detail.PlaylistDetailScreen
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.playListFill
import com.zhangke.krouter.annotation.Destination
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.lalilu.component.R as ComponentR

class PlaylistScreenModel : ScreenModel {
    val isSelecting = mutableStateOf(false)
    val selectedItems = mutableStateOf<List<Any>>(emptyList())
}

@Destination("/pages/playlist")
data object PlaylistScreen : TabScreen, ScreenBarFactory {
    private fun readResolve(): Any = PlaylistScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.playlist_screen_title) },
            icon = RemixIcon.Media.playListFill,
        )
    }

    @Composable
    override fun Content() {
        PlaylistScreen()
    }
}

@Composable
private fun Screen.PlaylistScreen(
    playlistSM: PlaylistScreenModel = rememberScreenModel { PlaylistScreenModel() },
    playlistRepo: PlaylistRepository = koinInject(),
) {
    val listState = rememberLazyListState()
    val playlists by remember { derivedStateOf { playlistRepo.getPlaylists() } }
    val playlistState = remember(playlists) { playlists.toMutableStateList() }

    val reorderableState = rememberReorderableLazyColumnState(listState) { from, to ->
        playlistState.toMutableList().apply {
            val toIndex = indexOfFirst { it.id == to.key }
            val fromIndex = indexOfFirst { it.id == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyColumnState

            add(toIndex, removeAt(fromIndex))
            playlistState.clear()
            playlistState.addAll(this)
        }
    }

    LaunchedEffect(Unit) {
        playlistRepo.checkFavouriteExist()
    }

    val selectHelper = rememberItemSelectHelper(
        isSelecting = playlistSM.isSelecting,
        selected = playlistSM.selectedItems
    )
    val selectActions = remember {
        listOf<SelectAction>()
    }

    if (this is ScreenBarFactory) {
        RegisterContent(
            isVisible = { selectHelper.isSelecting.value },
            onDismiss = { selectHelper.isSelecting.value = false },
            onBackPressed = { selectHelper.clear() }
        ) {
            Row(
                modifier = Modifier
                    .clickable(enabled = false) {}
                    .height(52.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    modifier = Modifier.fillMaxHeight(),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color(0x2F006E7C),
                        contentColor = Color(0xFF006E7C)
                    ),
                    onClick = { selectHelper.clear() }
                ) {
                    Image(
                        painter = painterResource(id = com.lalilu.component.R.drawable.ic_close_line),
                        contentDescription = "cancelButton",
                        colorFilter = ColorFilter.tint(color = Color(0xFF006E7C))
                    )
                    Text(
                        text = "取消 [${selectHelper.selected.value.size}]",
                        fontSize = 14.sp
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End
                ) {
                    items(items = selectActions) {
                        if (it is SelectAction.ComposeAction) {
                            it.content.invoke(selectHelper)
                            return@items
                        }

                        if (it is SelectAction.StaticAction) {
                            LongClickableTextButton(
                                modifier = Modifier.fillMaxHeight(),
                                shape = RectangleShape,
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    backgroundColor = it.color.copy(alpha = 0.15f),
                                    contentColor = it.color
                                ),
                                enableLongClickMask = it.forLongClick,
                                onLongClick = { if (it.forLongClick) it.onAction(selectHelper) },
                                onClick = {
                                    if (it.forLongClick) {
                                        ToastUtils.showShort("请长按此按钮以继续")
                                    } else {
                                        it.onAction(selectHelper)
                                    }
                                },
                            ) {
                                it.icon?.let { icon ->
                                    Image(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(id = icon),
                                        contentDescription = stringResource(id = it.title),
                                        colorFilter = ColorFilter.tint(color = it.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = stringResource(id = it.title),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LLazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            NavigatorHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                title = stringResource(id = R.string.playlist_screen_title)
            ) {
                IconButton(
                    onClick = {
                        AppRouter.intent(
                            NavIntent.Push(
                                PlaylistCreateOrEditScreen()
                            )
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(ComponentR.drawable.ic_add_line),
                        contentDescription = null
                    )
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
                        onDragStopped = { playlistRepo.setPlaylists(playlistState) }
                    ),
                    isDragging = { isDragging },
                    isSelected = { selectHelper.isSelected(playlist) },
                    isSelecting = { selectHelper.isSelecting.value },
                    onClick = {
                        if (selectHelper.isSelecting()) {
                            selectHelper.onSelect(playlist)
                        } else {
                            AppRouter.intent(
                                NavIntent.Push(
                                    PlaylistDetailScreen(playlistId = playlist.id)
                                )
                            )
                        }
                    },
                    onLongClick = { selectHelper.onSelect(playlist) }
                )
            }
        }
    }
}