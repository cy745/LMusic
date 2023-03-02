package com.lalilu.lmusic.compose.new_screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.repository.FavoriteRepository
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.component.base.PlaylistsSelectWrapper
import com.lalilu.lmusic.compose.component.card.NewPlaylistBar
import com.lalilu.lmusic.compose.component.card.PlaylistCard
import com.lalilu.lmusic.compose.new_screen.destinations.FavouriteScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistDetailScreenDestination
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.androidx.compose.get

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@PlaylistNavGraph(start = true)
@Destination
@Composable
fun PlaylistsScreen(
    idsText: String? = null,
    playlistsVM: PlaylistsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val idsToAdd = idsText.getIds()

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val creatingPlaylist = remember { mutableStateOf(false) }
    val state = rememberReorderableLazyListState(
        onMove = playlistsVM::onMovePlaylist,
        canDragOver = playlistsVM::canDragOver,
        onDragEnd = playlistsVM::onDragEnd
    )
    val selector = rememberSelectState<LPlaylist>(
        defaultState = idsToAdd.isNotEmpty(),
        onExitSelect = {
            if (idsToAdd.isNotEmpty()) {
                navigator.navigateUp()
            }
        }
    )

    SmartBar.RegisterExtraBarContent(showState = creatingPlaylist) {
        LaunchedEffect(creatingPlaylist.value) {
            if (!creatingPlaylist.value) {
                context.getActivity()?.let { KeyboardUtils.hideSoftInput(it) }
            }
        }

        NewPlaylistBar(
            onCancel = { creatingPlaylist.value = false },
            onCommit = {
                playlistsVM.createNewPlaylist(it)
                creatingPlaylist.value = false
            }
        )

        BackHandler(creatingPlaylist.value && SmartModalBottomSheet.isVisible.value) {
            creatingPlaylist.value = false
        }
    }

    PlaylistsSelectWrapper(
        isAddingSongs = idsToAdd.isNotEmpty(),
        songsToAdd = idsToAdd,
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
                    onClick = { creatingPlaylist.value = !creatingPlaylist.value }
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

                        AnimatedContent(targetState = creatingPlaylist.value) {
                            Icon(
                                painter = painterResource(if (it) R.drawable.ic_arrow_down_s_line else R.drawable.ic_arrow_up_s_line),
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
                                    navigator.navigate(FavouriteScreenDestination)
                                }

                                else -> {
                                    navigator.navigate(PlaylistDetailScreenDestination(item._id))
                                }
                            }
                        },
                        dragModifier = Modifier.detectReorder(state),
                        getPlaylist = { item },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectorInside.onSelected(item)
                        },
                        getIsSelected = { isDragging || selectorInside.selectedItems.any { it._id == item._id } }
                    )
                }
            }
        }
    }
}