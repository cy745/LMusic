package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.FavoriteRepository
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.card.SongsSelectWrapper
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.LibraryDetailNavigateBar
import com.lalilu.lmusic.compose.screen.LibraryNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.viewmodel.LocalPlayingVM
import com.lalilu.lmusic.viewmodel.LocalPlaylistDetailVM
import com.lalilu.lmusic.viewmodel.LocalPlaylistsVM
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistDetailViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import okhttp3.internal.toImmutableList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalAnimationApi::class)
object PlaylistDetailScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.PlaylistsDetail.name}/{playlistId}",
            arguments = listOf(navArgument("playlistId") {
                type = NavType.LongType
                defaultValue = FavoriteRepository.FAVORITE_PLAYLIST_ID
            })
        ) {
            val playlistId = it.arguments?.getLong("playlistId")
                ?: FavoriteRepository.FAVORITE_PLAYLIST_ID

            PlaylistDetailScreen(playlistId = playlistId)
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.PlaylistsDetail.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.PlaylistsDetail.name}/$argv"
    }
}


@OptIn(ExperimentalAnimationApi::class)
object FavouriteScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = ScreenData.Favourite.name
        ) {
            PlaylistDetailScreen(playlistId = FavoriteRepository.FAVORITE_PLAYLIST_ID)
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Favourite.name
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistDetailScreen(
    playlistId: Long,
    playingVM: PlayingViewModel = LocalPlayingVM.current,
    playlistsVM: PlaylistsViewModel = LocalPlaylistsVM.current,
    playlistDetailVM: PlaylistDetailViewModel = LocalPlaylistDetailVM.current
) {
    val navToSongAction = SongDetailScreen.navToByArgv(hapticType = HapticFeedbackType.LongPress)
    val playlist by playlistDetailVM.getPlaylistFlow(playlistId).collectAsState(initial = null)

    LaunchedEffect(playlistId) {
        playlistDetailVM.getPlaylistDetailById(playlistId, this)
    }

    val state = rememberReorderableLazyListState(
        onMove = playlistDetailVM::onMoveItem,
        canDragOver = playlistDetailVM::canDragOver,
        onDragEnd = playlistDetailVM::onDragEnd
    )

    SongsSelectWrapper(
        recoverTo = if (playlistId == FavoriteRepository.FAVORITE_PLAYLIST_ID) LibraryNavigateBar else LibraryDetailNavigateBar,
        extraActionsContent = { selector ->
            IconTextButton(
                text = "删除",
                color = Color(0xFF006E7C),
                onClick = {
                    playlist?.let {
                        playlistsVM.removeSongsFromPlaylist(
                            songs = selector.selectedItems.toImmutableList(),
                            playlist = it
                        )
                    }
                    selector.clear()
                }
            )
        }
    ) { selector ->
        SmartContainer.LazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item(key = "PLAYLIST_HEADER", contentType = LPlaylist::class) {
                NavigatorHeader(
                    title = playlist?.name ?: "未知歌单",
                    subTitle = "共 ${playlistDetailVM.songs.size} 首歌曲"
                )
            }
            items(
                items = playlistDetailVM.songs,
                key = { item -> item.id },
                contentType = { LSong::class }
            ) { item ->
                ReorderableItem(
                    defaultDraggingModifier = Modifier.animateItemPlacement(),
                    state = state,
                    key = item.id
                ) { isDragging ->
                    SongCard(
                        song = { item },
                        lyricRepository = playingVM.lyricRepository,
                        onClick = {
                            if (selector.isSelecting.value) {
                                selector.onSelected(item)
                            } else {
                                playingVM.playSongWithPlaylist(playlistDetailVM.songs, item)
                            }
                        },
                        dragModifier = Modifier.detectReorder(state),
                        onLongClick = { navToSongAction(item.id) },
                        onEnterSelect = { selector.onSelected(item) },
                        isSelected = { isDragging || selector.selectedItems.any { it.id == item.id } }
                    )
                }
            }
        }
    }
}