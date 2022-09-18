package com.lalilu.lmusic.screen.library.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.SmartModalBottomSheet
import com.lalilu.lmusic.screen.component.button.TextWithIconButton
import com.lalilu.lmusic.screen.component.card.NetworkDataCard
import com.lalilu.lmusic.screen.component.card.RecommendCard
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.extension.EDGE_BOTTOM
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.edgeTransparent
import com.lalilu.lmusic.viewmodel.NetworkDataViewModel

@Composable
fun SongDetailScreen(
    song: LSong,
    networkDataViewModel: NetworkDataViewModel
) {
    val windowSize = LocalWindowSize.current
    val navController = LocalNavigatorHost.current
    val networkData by networkDataViewModel.getNetworkDataFlowByMediaId(song.id)
        .collectAsState(null)

    LaunchedEffect(song) {
        SmartModalBottomSheet.disableFadeEdge()
        SmartBar.setExtraBar {
            SongDetailActionsBar(
                onAddSongToPlaylist = {
                    navController.navigate("${MainScreenData.SongsAddToPlaylist.name}/${song.id}")
                },
                onSetSongToNext = {
                    LMusicBrowser.addToNext(song.id)
                },
                onPlaySong = {
                    LMusicBrowser.addAndPlay(song.id)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
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
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            columns = GridCells.Fixed(
                if (windowSize.widthSizeClass != WindowWidthSizeClass.Compact) 2 else 1
            ),
            contentPadding = SmartBar.rememberContentPadding()
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
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RecommendCard(
                                width = 125.dp,
                                height = 125.dp,
                                data = { it },
                                getId = { it.id })

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text(text = it.name, style = MaterialTheme.typography.subtitle1)
                                Text(text = it.name, style = MaterialTheme.typography.subtitle2)
                            }
                        }
                    }
                }
            }

            item {
                NetworkDataCard(
                    onClick = {
                        navController.navigate("${MainScreenData.SongsMatchNetworkData.name}/${song.id}")
                    },
                    mediaId = song.id
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
        TextWithIconButton(
            textRes = R.string.text_button_play,
            color = Color(0xFF006E7C),
            onClick = onPlaySong
        )
        TextWithIconButton(
            textRes = R.string.button_set_song_to_next,
            color = Color(0xFF006E7C),
            onClick = onSetSongToNext
        )
        TextWithIconButton(
            textRes = R.string.button_add_song_to_playlist,
            iconRes = R.drawable.ic_play_list_add_line,
            color = Color(0xFF006E7C),
            onClick = onAddSongToPlaylist
        )
    }
}

@Composable
fun EmptySongDetailScreen() {
    Text(text = "无法获取该歌曲信息", modifier = Modifier.padding(20.dp))
}