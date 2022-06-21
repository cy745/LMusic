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
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.BlurTransformation
import com.blankj.utilcode.util.SizeUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.DeviceType
import com.lalilu.lmusic.utils.WindowSize
import com.lalilu.lmusic.utils.WindowSizeClass
import com.lalilu.lmusic.utils.rememberWindowSizeClass
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel

@Composable
fun ShowScreen(
    currentWindowSizeClass: WindowSizeClass = rememberWindowSizeClass(),
    mainViewModel: MainViewModel = hiltViewModel(),
    playingViewModel: PlayingViewModel = hiltViewModel()
) {
    val visible = remember(
        currentWindowSizeClass.deviceType,
        currentWindowSizeClass.windowSize
    ) {
        currentWindowSizeClass.deviceType == DeviceType.Phone &&
                currentWindowSizeClass.windowSize != WindowSize.Compact
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
@OptIn(ExperimentalCoilApi::class)
fun RowScope.ImageCover(mediaItem: MediaItem?) {
    val imagePainter = rememberImagePainter(
        data = mediaItem
    ) {
        size(SizeUtils.dp2px(256f))
        crossfade(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        val loaded = imagePainter.state.painter != null
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(10.dp),
            color = Color(0x55000000),
            elevation = if (loaded) 5.dp else 0.dp
        ) {
            if (loaded) {
                Image(
                    painter = imagePainter,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentScale = ContentScale.FillHeight
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_music_line),
                    contentDescription = "",
                    contentScale = FixedScale(1f),
                    colorFilter = ColorFilter.tint(color = Color.LightGray),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
fun BoxScope.BlurImageBg(mediaItem: MediaItem?) {
    val blurImagePainter = rememberImagePainter(
        data = mediaItem
    ) {
        size(SizeUtils.dp2px(128f))
        transformations(BlurTransformation(LocalContext.current, 25f, 4f))
        crossfade(true)
    }

    Image(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
        painter = blurImagePainter,
        contentScale = ContentScale.Crop,
        contentDescription = ""
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
    val isPlaying = globalDataManager.currentIsPlaying.collectAsState()
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
                    when (repeatMode) {
                        0 -> R.drawable.ic_play_list_line
                        1 -> R.drawable.ic_repeat_one_line
                        else -> R.drawable.ic_shuffle_line
                    }
                ),
                contentDescription = "play_pause",
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}