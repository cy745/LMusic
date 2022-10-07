package com.lalilu.lmusic.screen.component.card

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.LMusicLyricManager
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.toColorFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SongCard(
    modifier: Modifier = Modifier,
    index: Int,
    serialNumber: String = "#${index + 1}",
    getSong: () -> LSong,
    loadDelay: () -> Long = { 0L },
    getIsSelected: (LSong) -> Boolean = { false },
    onItemClick: (song: LSong) -> Unit = { },
    onItemLongClick: (song: LSong) -> Unit = { },
    onItemImageClick: (song: LSong) -> Unit = { },
    onItemImageLongClick: (song: LSong) -> Unit = { }
) {
    val haptic = LocalHapticFeedback.current
    val song = remember { getSong() }
    var hasLrc by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(if (getIsSelected(song)) dayNightTextColor(0.15f) else Color.Transparent)
    val interaction = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        delay(loadDelay())

        if (isActive) {
            hasLrc = LMusicLyricManager.hasLyric(song)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = bgColor)
            .combinedClickable(
                interactionSource = interaction,
                indication = rememberRipple(),
                onClick = { onItemClick(song) },
                onLongClick = { onItemLongClick(song) })
            .padding(vertical = 8.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = song.name,
//                    fontSize = 16.sp,
//                    letterSpacing = 0.05.em,
                    color = dayNightTextColor(),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_lrc_fill),
                        contentDescription = "lrcIconPainter",
                        colorFilter = dayNightTextColor(0.9f).toColorFilter(),
                        modifier = Modifier
                            .size(20.dp)
                            .aspectRatio(1f)
                            .alpha(if (hasLrc) 1f else 0f)
                    )
                    Image(
                        painter = painterResource(id = getMusicTypeIcon(song.mimeType)),
                        contentDescription = "mediaTypeIconPainter",
                        colorFilter = dayNightTextColor(0.9f).toColorFilter(),
                        modifier = Modifier
                            .size(20.dp)
                            .aspectRatio(1f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$serialNumber ${song._artist}",
                    color = dayNightTextColor(0.5f),
//                    fontSize = 10.sp,
//                    letterSpacing = 0.05.em,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = TimeUtils.millis2String(song.durationMs, "mm:ss"),
                    fontSize = 12.sp,
                    letterSpacing = 0.05.em,
                    color = dayNightTextColor(0.7f)
                )
            }
        }
        Surface(
            elevation = 2.dp,
            shape = RoundedCornerShape(5.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(64.dp)
                    .aspectRatio(1f)
                    .combinedClickable(
                        interactionSource = interaction,
                        indication = null,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemImageLongClick(song)
                        },
                        onClick = { onItemImageClick(song) }
                    ),
//                    .pointerInput(Unit) {
//                        detectDragGesturesAfterLongPress { change, dragAmount ->
//                            println("change: $change, dragAmount: $dragAmount")
//                        }
//                    }
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song)
                    .placeholder(R.drawable.ic_music_line_bg_64dp)
                    .error(R.drawable.ic_music_line_bg_64dp)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = "SongCardImage"
            )
        }
    }
}

@DrawableRes
fun getMusicTypeIcon(mimeType: String): Int {
    val strings = mimeType.split("/").toTypedArray()

    return when (strings[strings.size - 1].uppercase()) {
        "FLAC" -> R.drawable.ic_flac_line
        "MPEG", "MP3" -> R.drawable.ic_mp3_line
        "MP4" -> R.drawable.ic_mp4_line
        "APE" -> R.drawable.ic_ape_line
        "DSD", "DSF" -> R.drawable.ic_dsd_line
        "WAV", "X-WAV" -> R.drawable.ic_wav_line
        else -> R.drawable.ic_mp3_line
    }
}