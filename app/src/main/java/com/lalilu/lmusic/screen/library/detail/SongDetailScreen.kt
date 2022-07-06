package com.lalilu.lmusic.screen.library.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.R
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.button.TextWithIconButton
import com.lalilu.lmusic.screen.component.card.NetworkDataCard
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.utils.rememberCoverWithFlow
import com.lalilu.lmusic.viewmodel.MainViewModel

@Composable
fun SongDetailScreen(
    mediaItem: MediaItem,
    currentWindowSize: WindowSize,
    navigateTo: (destination: String) -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val mediaBrowser = mainViewModel.mediaBrowser
    val imagePainter = rememberCoverWithFlow(mediaItem = mediaItem) {
        size(SizeUtils.dp2px(128f))
        crossfade(true)
    }
    val title = mediaItem.mediaMetadata.title?.toString()
        ?: stringResource(id = MainScreenData.SongsDetail.title)
    val subTitle = "${mediaItem.mediaMetadata.artist}\n\n${mediaItem.mediaMetadata.albumTitle}"

    SongDetailScreen(
        title = title,
        subTitle = subTitle,
        mediaId = mediaItem.mediaId,
        imagePainter = imagePainter,
        currentWindowSize = currentWindowSize,
        onMatchNetworkData = {
            navigateTo("${MainScreenData.SongsMatchNetworkData.name}/${mediaItem.mediaId}")
        },
        onAddSongToPlaylist = {
            navigateTo("${MainScreenData.SongsAddToPlaylist.name}/${mediaItem.mediaId}")
        },
        onSetSongToNext = {
            mediaBrowser.addToNext(mediaItem.mediaId)
        },
        onPlaySong = {
            mediaBrowser.playById(mediaId = mediaItem.mediaId, playWhenReady = true)
        }
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SongDetailScreen(
    title: String,
    subTitle: String,
    mediaId: String,
    currentWindowSize: WindowSize,
    imagePainter: ImagePainter,
    onPlaySong: () -> Unit = {},
    onSetSongToNext: () -> Unit = {},
    onAddSongToPlaylist: () -> Unit = {},
    onMatchNetworkData: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigatorHeader(title = title, subTitle = subTitle) {
            Surface(
                elevation = 4.dp,
                shape = RoundedCornerShape(2.dp)
            ) {
                if (imagePainter.state.painter != null) {
                    Image(
                        painter = imagePainter, contentDescription = "CoverImage",
                        modifier = Modifier
                            .sizeIn(
                                minHeight = 64.dp,
                                maxHeight = 128.dp,
                                minWidth = 64.dp,
                                maxWidth = 144.dp
                            )
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_line),
                        contentDescription = "",
                        contentScale = FixedScale(2.5f),
                        colorFilter = ColorFilter.tint(color = Color.LightGray),
                        modifier = Modifier
                            .size(128.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            columns = GridCells.Fixed(
                if (currentWindowSize != WindowSize.Compact) 2 else 1
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                NetworkDataCard(
                    onClick = onMatchNetworkData,
                    mediaId = mediaId
                )
            }
            item {
                Row(
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
        }
    }
}

@Composable
fun EmptySongDetailScreen() {
    Text(text = "无法获取该歌曲信息", modifier = Modifier.padding(20.dp))
}