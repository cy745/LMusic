package com.lalilu.lmusic.compose.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.playback.PlayMode
import com.lalilu.lmusic.utils.coil.BlurTransformation
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import org.koin.androidx.compose.get

@Composable
fun ShowScreen(
    playingVM: PlayingViewModel = get()
) {
    val windowSize = LocalWindowSize.current
    val configuration = LocalConfiguration.current
    val isPad by windowSize.rememberIsPad()

    val visible = remember(isPad, configuration.orientation) {
        !isPad && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    if (visible) {
        val song by playingVM.runtime.playingFlow.collectAsState(null)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
        ) {
            BlurImageBg(song = song)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { }
                    .padding(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ImageCover(song = song)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    SongDetailPanel(song = song)
                    ControlPanel(playingVM)
                }
            }
        }
    }
}

@Composable
fun RowScope.ImageCover(song: LSong?) {
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
                    .data(song)
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
fun BoxScope.BlurImageBg(song: LSong?) {

    AsyncImage(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
        model = ImageRequest.Builder(LocalContext.current)
            .data(song)
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
    song: LSong?
) {
    if (song == null) {
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
            text = song.name,
            color = Color.White,
            fontSize = 24.sp
        )
        Text(
            text = song._artist,
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = song._albumTitle,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ControlPanel(
    playingVM: PlayingViewModel = get()
) {
    val isPlaying = playingVM.runtime.isPlayingFlow.collectAsState(false)
    var playMode by playingVM.lMusicSp.playMode

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = { playingVM.browser.play() }) {
            Image(
                painter = painterResource(id = R.drawable.ic_skip_back_line),
                contentDescription = "skip_back",
                modifier = Modifier.size(28.dp)
            )
        }
        IconToggleButton(
            checked = isPlaying.value,
            onCheckedChange = { playingVM.browser.playPause() }
        ) {
            Image(
                painter = painterResource(
                    if (isPlaying.value) R.drawable.ic_pause_line else R.drawable.ic_play_line
                ),
                contentDescription = "play_pause",
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { playingVM.browser.skipToNext() }) {
            Image(
                painter = painterResource(id = R.drawable.ic_skip_forward_line),
                contentDescription = "skip_forward",
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { playMode = (playMode + 1) % 3 }) {
            Image(
                painter = painterResource(
                    when (PlayMode.values()[playMode]) {
                        PlayMode.ListRecycle -> R.drawable.ic_order_play_line
                        PlayMode.RepeatOne -> R.drawable.ic_repeat_one_line
                        PlayMode.Shuffle -> R.drawable.ic_shuffle_line
                    }
                ),
                contentDescription = "play_pause",
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}