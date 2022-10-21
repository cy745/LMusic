package com.lalilu.lmusic.screen.library.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.IconTextButton
import com.lalilu.lmusic.compose.component.NetworkPairCard
import com.lalilu.lmusic.compose.component.RecommendCardForAlbum
import com.lalilu.lmusic.screen.ScreenActions
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.SmartContainer
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.extension.EDGE_BOTTOM
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.edgeTransparent
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.NetworkDataViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongDetailScreen(
    song: LSong,
    mainViewModel: MainViewModel,
    networkDataViewModel: NetworkDataViewModel
) {
    val windowSize = LocalWindowSize.current
    val navToAlbumAction = ScreenActions.navToAlbum()
    val navToNetworkMatchAction = ScreenActions.navToNetworkMatch()
    val navToAddToPlaylistAction = ScreenActions.navToAddToPlaylist()
    val networkData by networkDataViewModel.getNetworkDataFlowByMediaId(song.id)
        .collectAsState(null)

    LaunchedEffect(song) {
        SmartBar.setExtraBar {
            SongDetailActionsBar(
                onAddSongToPlaylist = {
                    // TODO 完善此处跳转以及传递数据的逻辑
                    navToAddToPlaylistAction()
                },
                onSetSongToNext = { LMusicBrowser.addToNext(song.id) },
                onPlaySong = { LMusicBrowser.addAndPlay(song.id) })
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .edgeTransparent(position = EDGE_BOTTOM, percent = 1.5f),
            model = ImageRequest.Builder(LocalContext.current)
                .data(networkData?.requireCoverUri() ?: song)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = ""
        )
        SmartContainer.LazyVerticalGrid(
            modifier = Modifier.padding(top = 100.dp),
            columns = GridCells.Fixed(
                if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1
            )
        ) {
            item {
                NavigatorHeader(title = song.name, subTitle = song._artist) {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fullscreen_line),
                            contentDescription = "查看图片"
                        )
                    }
                }
            }

            song.album?.let {
                item {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 20.dp)
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
                            RecommendCardForAlbum(
                                width = { 125.dp },
                                height = { 125.dp },
                                item = { it }
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
                        networkDataViewModel.saveCoverIntoNetworkData(networkData?.songId, song.id)
                    },
                    onDownloadLyric = {
                        networkDataViewModel.saveLyricIntoNetworkData(
                            networkData?.songId, song.id, networkData?.platform
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SongDetailActionsBar(
    onPlaySong: () -> Unit = {},
    onSetSongToNext: () -> Unit = {},
    onAddSongToPlaylist: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
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
    }
}

@Composable
fun EmptySongDetailScreen() {
    Text(text = "无法获取该歌曲信息", modifier = Modifier.padding(20.dp))
}