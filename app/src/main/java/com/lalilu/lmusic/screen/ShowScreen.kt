package com.lalilu.lmusic.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.blankj.utilcode.util.SizeUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.BlurTransformation
import com.lalilu.lmusic.utils.LocalWindowSize
import com.lalilu.lmusic.utils.RepeatMode
import com.lalilu.lmusic.viewmodel.MainViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel

@Composable
fun ShowScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    playingViewModel: PlayingViewModel = hiltViewModel()
) {
    val windowSize = LocalWindowSize.current
    val visible = remember(windowSize.widthSizeClass) {
        windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
    }

    if (visible) {
        val mediaItem by playingViewModel.globalDataManager.currentMediaItem.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
        ) {
            BlurImageBg(mediaItem = mediaItem)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { }
                    .padding(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ImageCover(mediaItem = mediaItem)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    SongDetailPanel(mediaItem = mediaItem)
                    ControlPanel(
                        mediaBrowser = mainViewModel.mediaBrowser,
                        playingViewModel.globalDataManager
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.ImageCover(mediaItem: MediaItem?) {
    val songs = Library.getSongOrNull(mediaItem?.mediaId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(10.dp),
            color = Color(0x55000000),
            elevation = 0.dp
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(songs)
                    .size(SizeUtils.dp2px(256f))
                    .crossfade(true)
                    .build(),
                contentDescription = ""
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_line),
                        contentDescription = "",
                        contentScale = FixedScale(1f),
                        colorFilter = ColorFilter.tint(color = Color.LightGray),
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                } else {
                    SubcomposeAsyncImageContent(
                        modifier = Modifier.fillMaxHeight(),
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.BlurImageBg(mediaItem: MediaItem?) {
    val songs = Library.getSongOrNull(mediaItem?.mediaId)

    AsyncImage(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
        model = ImageRequest.Builder(LocalContext.current)
            .data(songs)
            .size(SizeUtils.dp2px(128f))
            .transformations(BlurTransformation(LocalContext.current, 25f, 4f))
            .crossfade(true)
            .build(),
        contentDescription = "",
        contentScale = ContentScale.Crop
    )
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0x40000000))
    )
}

@Composable
fun SongDetailPanel(
    mediaItem: MediaItem?
) {
    if (mediaItem == null) {
        Text(
            text = "歌曲读取失败",
            color = Color.White,
            fontSize = 24.sp
        )
        return
    }
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = mediaItem.mediaMetadata.title.toString(),
            color = Color.White,
            fontSize = 24.sp
        )
        Text(
            text = mediaItem.mediaMetadata.artist.toString(),
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = mediaItem.mediaMetadata.albumTitle.toString(),
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ControlPanel(
    mediaBrowser: MSongBrowser,
    globalDataManager: GlobalDataManager
) {
    val isPlaying = globalDataManager.currentIsPlayingFlow.collectAsState()
    var repeatMode by rememberDataSaverState(
        Config.KEY_SETTINGS_REPEAT_MODE, Config.DEFAULT_SETTINGS_REPEAT_MODE
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = { mediaBrowser.browser?.seekToPrevious() }) {
            Image(
                painter = painterResource(id = R.drawable.ic_skip_back_line),
                contentDescription = "skip_back",
                modifier = Modifier.size(28.dp)
            )
        }
        IconToggleButton(checked = isPlaying.value, onCheckedChange = {
            mediaBrowser.togglePlay()
        }) {
            Image(
                painter = painterResource(
                    if (isPlaying.value) R.drawable.ic_pause_line else R.drawable.ic_play_line
                ),
                contentDescription = "play_pause",
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { mediaBrowser.browser?.seekToNext() }) {
            Image(
                painter = painterResource(id = R.drawable.ic_skip_forward_line),
                contentDescription = "skip_forward",
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { repeatMode = (repeatMode + 1) % 3 }) {
            Image(
                painter = painterResource(
                    when (RepeatMode.values()[repeatMode]) {
                        RepeatMode.ListRecycle -> R.drawable.ic_order_play_line
                        RepeatMode.RepeatOne -> R.drawable.ic_repeat_one_line
                        RepeatMode.Shuffle -> R.drawable.ic_shuffle_line
                    }
                ),
                contentDescription = "play_pause",
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}