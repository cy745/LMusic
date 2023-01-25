package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import com.blankj.utilcode.util.KeyboardUtils
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.repository.FavoriteRepository
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.InputBar
import com.lalilu.lmusic.compose.component.base.PlaylistsSelectWrapper
import com.lalilu.lmusic.compose.component.card.PlaylistCard
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.LibraryNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.FavouriteScreen
import com.lalilu.lmusic.compose.screen.library.detail.PlaylistDetailScreen
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.LocalPlaylistsVM
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalAnimationApi::class)
object PlaylistsScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.Playlists.name}?isAdding={isAdding}",
            arguments = listOf(navArgument("isAdding") { defaultValue = false })
        ) {
            val isAdding = it.arguments?.getBoolean("isAdding") ?: false

            PlaylistsScreen(
                isAddingSongs = isAdding
            )
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Playlists.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.Playlists.name}?isAdding=$argv"
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
private fun PlaylistsScreen(
    isAddingSongs: Boolean = false,
    playlistsVM: PlaylistsViewModel = LocalPlaylistsVM.current
) {
    val context = LocalContext.current
    val navigator = LocalNavigatorHost.current
    val navToPlaylistAction = PlaylistDetailScreen.navToByArgv()
    val navToFavouriteAction = FavouriteScreen.navTo {
        popUpTo(navigator.graph.findStartDestination().id) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
    val state = rememberReorderableLazyListState(
        onMove = playlistsVM::onMovePlaylist,
        canDragOver = playlistsVM::canDragOver,
        onDragEnd = playlistsVM::onDragEnd
    )
    val selector = rememberSelectState<LPlaylist>(
        defaultState = isAddingSongs,
        onExitSelect = {
            if (isAddingSongs) {
                navigator.navigateUp()
            }
        }
    )

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
            SmartBar.setExtraBar(content = null)
        }
    }

    PlaylistsSelectWrapper(
        isAddingSongs = isAddingSongs,
        recoverTo = LibraryNavigateBar,
        selector = selector
    ) { selectorInside ->
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
                    val icon = if (item._id == FavoriteRepository.FAVORITE_PLAYLIST_ID) {
                        R.drawable.ic_heart_3_fill
                    } else {
                        R.drawable.ic_play_list_fill
                    }
                    val iconTint = if (item._id == FavoriteRepository.FAVORITE_PLAYLIST_ID) {
                        MaterialTheme.colors.primary
                    } else {
                        LocalContentColor.current
                    }

                    PlaylistCard(
                        icon = icon,
                        iconTint = iconTint,
                        onClick = {
                            when {
                                selectorInside.isSelecting.value -> {
                                    selectorInside.onSelected(item)
                                }

                                item._id == FavoriteRepository.FAVORITE_PLAYLIST_ID -> {
                                    navToFavouriteAction()
                                }

                                else -> {
                                    navToPlaylistAction(item.id)
                                }
                            }
                        },
                        dragModifier = Modifier.detectReorder(state),
                        getPlaylist = { item },
                        onLongClick = { selectorInside.onSelected(item) },
                        getIsSelected = { isDragging || selectorInside.selectedItems.any { it._id == item._id } }
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