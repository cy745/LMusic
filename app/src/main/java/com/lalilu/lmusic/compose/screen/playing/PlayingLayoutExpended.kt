package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.coil.BlurTransformation
import com.lalilu.lplayer.MPlayer
import com.lalilu.lplayer.action.PlayerAction
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.playLine
import org.koin.compose.koinInject

@Composable
fun PlayingLayoutExpended(
    modifier: Modifier = Modifier,
) {
    val currentPlaying = MPlayer.currentMediaItem
    val context = LocalContext.current
    val data = remember(currentPlaying) {
        ImageRequest.Builder(context)
            .data(currentPlaying)
            .size(500)
            .crossfade(true)
            .transformations(BlurTransformation(context, 25f, 8f))
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(300, 500))
            },
            targetState = data,
            label = ""
        ) { model ->
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = model,
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedContent(
                    modifier = Modifier.size(300.dp),
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(300, 500))
                    },
                    targetState = currentPlaying,
                    label = ""
                ) { model ->
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = model,
                        contentScale = ContentScale.Crop,
                        contentDescription = null
                    )
                }

                SongDetailPanel(playable = currentPlaying)
                ControlPanel()
            }
        }
    }
}

@Composable
fun SongDetailPanel(
    playable: MediaItem?,
) {
    if (playable == null) {
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
            text = playable.mediaMetadata.title.toString(),
            color = Color.White,
            fontSize = 24.sp
        )
        Text(
            text = playable.mediaMetadata.subtitle.toString(),
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = playable.mediaMetadata.subtitle.toString(),
            color = Color.White,
            fontSize = 12.sp
        )
    }
}


@Composable
fun ControlPanel(
    settingsSp: SettingsSp = koinInject()
) {
    val isPlaying = remember { derivedStateOf { MPlayer.isPlaying } }
    var playMode by settingsSp.playMode

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = { PlayerAction.SkipToPrevious.action() }) {
            Image(
                painter = painterResource(id = R.drawable.ic_skip_previous_line),
                contentDescription = "skip_back",
                modifier = Modifier.size(28.dp)
            )
        }
        IconToggleButton(
            checked = isPlaying.value,
            onCheckedChange = {
                PlayerAction.PlayOrPause.action()
            }
        ) {
            Image(
                painter = painterResource(
                    if (isPlaying.value) R.drawable.ic_pause_line else R.drawable.ic_play_line
                ),
                contentDescription = "play_pause",
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { PlayerAction.SkipToNext.action() }) {
            Image(
                painter = painterResource(id = R.drawable.ic_skip_next_line),
                contentDescription = "skip_forward",
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { playMode = (playMode + 1) % 3 }) {
            Image(
                // TODO 待完善播放模式的显示
                imageVector = RemixIcon.Media.playLine,
//                painter = painterResource(
//                    when (PlayMode.values()[playMode]) {
//                        PlayMode.ListRecycle -> R.drawable.ic_order_play_line
//                        PlayMode.RepeatOne -> R.drawable.ic_repeat_one_line
//                        PlayMode.Shuffle -> R.drawable.ic_shuffle_line
//                    }
//                ),
                contentDescription = "play_pause",
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}