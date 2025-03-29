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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import com.lalilu.R
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.LyricSourceEmbedded
import com.lalilu.lmedia.lyric.LyricUtils
import com.lalilu.lmusic.compose.component.playing.LyricViewActionDialog
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricLayout
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.coil.BlurTransformation
import com.lalilu.lplayer.MPlayer
import com.lalilu.lplayer.action.PlayerAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/**
 * Expended 状态下的播放布局
 */
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
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PlayerPanel(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                currentPlaying = currentPlaying
            )

            LyricPanel(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun PlayerPanel(
    modifier: Modifier = Modifier,
    currentPlaying: MediaItem? = null
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(300, 500))
            },
            targetState = currentPlaying,
            label = ""
        ) { model ->
            Card(
                shape = RoundedCornerShape(2.dp)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = model,
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }

        SongDetailPanel(playable = currentPlaying)
        Spacer(Modifier.weight(1f))
        ControlPanel()
    }
}

@Composable
fun LyricPanel(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val lyricSource = remember { LyricSourceEmbedded(context = context) }
    val lyrics = remember { mutableStateOf<List<LyricItem>>(emptyList()) }
    val isLyricScrollEnable = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val currentPosition = remember { mutableLongStateOf(0L) }

    LaunchedEffect(key1 = MPlayer.currentMediaItem) {
        withContext(Dispatchers.IO) {
            MPlayer.currentMediaItem
                ?.let { lyricSource.loadLyric(it) }
                ?.let { LyricUtils.parseLrc(it.first, it.second) }
                .let { if (isActive) lyrics.value = it ?: emptyList() }
        }
    }

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                withFrameMillis {
                    val newValue = MPlayer.currentPosition
                    if (currentPosition.longValue != newValue) {
                        currentPosition.longValue = newValue
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        LyricLayout(
            modifier = Modifier
                .fillMaxSize(),
            lyricEntry = lyrics,
            listState = listState,
            currentTime = { currentPosition.longValue },
            screenConstraints = constraints,
            isUserClickEnable = { true },
            isUserScrollEnable = { isLyricScrollEnable.value },
            onPositionReset = {
                if (isLyricScrollEnable.value) {
                    isLyricScrollEnable.value = false
                }
            },
            onItemClick = {
                if (isLyricScrollEnable.value) {
                    isLyricScrollEnable.value = false
                }
                PlayerAction.SeekTo(it.time).action()
            },
            onItemLongClick = {
                isLyricScrollEnable.value = !isLyricScrollEnable.value
            },
        )
    }
}

@Composable
private fun SongDetailPanel(
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = playable.mediaMetadata.title.toString(),
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = playable.mediaMetadata.subtitle.toString(),
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}


@Composable
private fun ControlPanel(
    settingsSp: SettingsSp = koinInject()
) {
    val isPlaying = remember { derivedStateOf { MPlayer.isPlaying } }
    var playMode by settingsSp.playMode

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterHorizontally)
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
        IconButton(onClick = { DialogWrapper.push(LyricViewActionDialog) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_text),
                contentDescription = "",
                tint = Color.White
            )
        }
//        IconButton(onClick = { playMode = (playMode + 1) % 3 }) {
//            Image(
//                // TODO 待完善播放模式的显示
//                imageVector = RemixIcon.Media.playLine,
////                painter = painterResource(
////                    when (PlayMode.values()[playMode]) {
////                        PlayMode.ListRecycle -> R.drawable.ic_order_play_line
////                        PlayMode.RepeatOne -> R.drawable.ic_repeat_one_line
////                        PlayMode.Shuffle -> R.drawable.ic_shuffle_line
////                    }
////                ),
//                contentDescription = "play_pause",
//                colorFilter = ColorFilter.tint(color = Color.White),
//                modifier = Modifier.size(24.dp)
//            )
//        }
    }
}