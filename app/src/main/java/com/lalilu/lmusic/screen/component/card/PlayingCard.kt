package com.lalilu.lmusic.screen.component.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.toColorFilter
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
@OptIn(
    ExperimentalFoundationApi::class
)
fun PlayingCard(
    modifier: Modifier = Modifier,
    getSong: () -> LSong,
    loadDelay: () -> Long = { 0L },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    var hasLrc by remember { mutableStateOf(false) }
    var imageData by remember { mutableStateOf<Any?>(null) }

    val song = remember { getSong() }
    val titleText = remember(song) { song.name }
    val artistText = remember(song) { song._artist }
    val durationText = remember(song) { TimeUtils.millis2String(song.durationMs, "mm:ss") }

    LaunchedEffect(Unit) {
        delay(loadDelay())

        if (isActive) {
            println("PlayingCard: isActive $isActive")

            val lyric = LyricSourceFactory.instance?.getLyric(song)
            hasLrc = lyric != null && lyric.first.isNotEmpty()

            imageData = song
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            elevation = 2.dp,
            shape = RoundedCornerShape(1.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(64.dp)
                    .aspectRatio(1f),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageData)
                    .placeholder(R.drawable.ic_music_line_bg_64dp)
                    .error(R.drawable.ic_music_line_bg_64dp)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = "SongCardImage"
            )
        }
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
                    text = titleText,
                    fontSize = 16.sp,
                    letterSpacing = 0.05.em,
                    color = dayNightTextColor(),
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
                    text = artistText,
                    color = dayNightTextColor(0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.05.em,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = durationText,
                    fontSize = 14.sp,
                    letterSpacing = 0.05.em,
                    color = dayNightTextColor(0.7f)
                )
            }
        }
    }
}