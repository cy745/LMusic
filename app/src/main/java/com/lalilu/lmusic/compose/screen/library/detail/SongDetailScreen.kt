package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconCheckButton
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.NetworkPairCard
import com.lalilu.lmusic.compose.component.card.RecommendCardCover
import com.lalilu.lmusic.compose.component.card.SongInformationCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.MatchNetworkDataScreen
import com.lalilu.lmusic.utils.extension.EDGE_BOTTOM
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.edgeTransparent
import com.lalilu.lmusic.utils.extension.rememberScrollPosition
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.NetworkDataViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
object SongDetailScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.SongsDetail.name}?mediaId={mediaId}",
            arguments = listOf(navArgument("mediaId") {})
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")

            LMedia.getSongOrNull(mediaId)?.let {
                SongDetailScreen(song = it)
            } ?: EmptySongDetailScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.SongsDetail.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.SongsDetail.name}?mediaId=$argv"
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongDetailScreen(
    song: LSong,
    mainVM: MainViewModel = getViewModel(),
    playingVM: PlayingViewModel = getViewModel(),
    playlistsVM: PlaylistsViewModel = getViewModel(),
    networkDataVM: NetworkDataViewModel = getViewModel()
) {
    val navToArtistAction = ArtistDetailScreen.navToByArgv()
    val navToAlbumAction = AlbumDetailScreen.navToByArgv()
    val navToNetworkMatchAction = MatchNetworkDataScreen.navToByArgv()
    val navToAddToPlaylist = mainVM.navToAddToPlaylist()

    val networkData by networkDataVM.getNetworkDataFlowByMediaId(song.id)
        .collectAsState(null)
    val isLiked by playlistsVM.checkIsFavorite(song).collectAsState(initial = false)
    val gridState = rememberLazyGridState()
    val scrollPosition = rememberScrollPosition(state = gridState)
    val bgAlpha = remember {
        derivedStateOf {
            return@derivedStateOf 1f - (scrollPosition.value / 500f)
                .coerceIn(0f, 0.8f)
        }
    }

    LaunchedEffect(song) {
        SmartBar.setExtraBar {
            SongDetailActionsBar(
                getIsLiked = { isLiked },
                onIsLikedChange = {
                    if (it) {
                        playlistsVM.addToFavorite(song)
                    } else {
                        playlistsVM.removeFromFavorite(song)
                    }
                },
                onAddSongToPlaylist = {
                    navToAddToPlaylist(listOf(song))
                },
                onSetSongToNext = {
                    playingVM.browser.addToNext(song.id)
                    DynamicTips.push(
                        title = song.name,
                        subTitle = "下一首播放",
                        imageData = song
                    )
                },
                onPlaySong = { playingVM.browser.addAndPlay(song.id) })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .recomposeHighlighter(),
        contentAlignment = Alignment.TopCenter
    ) {
        AsyncImage(
            modifier = Modifier
                .recomposeHighlighter()
                .fillMaxWidth()
                .edgeTransparent(position = EDGE_BOTTOM, percent = 1.5f)
                .graphicsLayer {
                    alpha = bgAlpha.value
                },
            model = ImageRequest.Builder(LocalContext.current)
                .data(networkData?.requireCoverUri() ?: song)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = ""
        )
        SmartContainer.LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 }
        ) {
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }

            item {
                NavigatorHeader(
                    title = song.name,
                    columnExtraContent = {
                        FlowRow(mainAxisSpacing = 8.dp) {
                            song.artists.forEach {
                                Chip(
                                    onClick = {
                                        println("navToArtistAction: [${it.name}]")
                                        navToArtistAction(it.name)
                                    },
                                    colors = ChipDefaults.outlinedChipColors(),
                                ) {
                                    Text(
                                        text = it.name,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                                            .copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                ) {
//                    IconButton(onClick = { }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_fullscreen_line),
//                            contentDescription = "查看图片"
//                        )
//                    }
                }
            }

            song.album?.let {
                item {
                    Surface(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                            .width(intrinsicSize = IntrinsicSize.Min),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { navToAlbumAction.invoke(it.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RecommendCardCover(
                                width = { 125.dp },
                                height = { 125.dp },
                                imageData = { it }
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.subtitle1,
                                    color = dayNightTextColor()
                                )
                                it.artistName?.let { it1 ->
                                    Text(
                                        text = it1,
                                        style = MaterialTheme.typography.subtitle2,
                                        color = dayNightTextColor(0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                NetworkPairCard(
                    item = { networkData },
                    onClick = {
                        navToNetworkMatchAction.invoke(song.id)
                    },
                    onDownloadCover = {
                        networkDataVM.saveCoverIntoNetworkData(networkData?.netId, song.id)
                    },
                    onDownloadLyric = {
                        networkDataVM.saveLyricIntoNetworkData(
                            networkData?.netId, song.id, networkData?.platform
                        )
                    }
                )
            }

            item {
                SongInformationCard(song = song)
            }
        }
    }
}

@Composable
fun SongDetailActionsBar(
    getIsLiked: () -> Boolean = { false },
    onIsLikedChange: (Boolean) -> Unit = {},
    onPlaySong: () -> Unit = {},
    onSetSongToNext: () -> Unit = {},
    onAddSongToPlaylist: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        IconTextButton(
            text = stringResource(id = R.string.text_button_play),
            color = Color(0xFF006E7C),
            onClick = onPlaySong
        )
        IconTextButton(
            text = stringResource(id = R.string.button_set_song_to_next),
            color = Color(0xFF006E7C),
            onClick = onSetSongToNext
        )
        IconTextButton(
            color = Color(0xFF006E7C),
            text = stringResource(id = R.string.button_add_song_to_playlist),
            iconPainter = painterResource(id = R.drawable.ic_play_list_add_line),
            onClick = onAddSongToPlaylist
        )
        IconCheckButton(
            getIsChecked = getIsLiked,
            onCheckedChange = onIsLikedChange,
            checkedColor = MaterialTheme.colors.primary,
            checkedIconRes = R.drawable.ic_heart_3_fill,
            normalIconRes = R.drawable.ic_heart_3_line
        )
    }
}

@Composable
fun EmptySongDetailScreen() {
    Text(text = "无法获取该歌曲信息", modifier = Modifier.padding(20.dp))
}