package com.lalilu.lmusic.screen.component.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
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
import com.lalilu.lmusic.utils.sources.LyricSourceFactory

@Composable
@OptIn(
    ExperimentalFoundationApi::class
)
fun PlayingCard(
    modifier: Modifier = Modifier,
    song: LSong,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val durationText = TimeUtils.millis2String(song.durationMs, "mm:ss")
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    val colorFilter = ColorFilter.tint(color = color.copy(alpha = 0.9f))
    val hasLrc = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val lyric = LyricSourceFactory.instance?.getLyric(song)
        hasLrc.value = lyric != null && lyric.first.isNotEmpty()
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
                    .data(song)
                    .placeholder(R.drawable.ic_music_line_bg_64dp)
                    .error(R.drawable.ic_music_line_bg_64dp)
                    .crossfade(true)
                    .build(),
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
                    text = song.name,
                    fontSize = 16.sp,
                    letterSpacing = 0.05.em,
                    color = color,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (hasLrc.value) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lrc_fill),
                            contentDescription = "lrcIconPainter",
                            colorFilter = colorFilter,
                            modifier = Modifier
                                .size(20.dp)
                                .aspectRatio(1f)
                        )
                    }
                    Image(
                        painter = painterResource(id = getMusicTypeIcon(song.mimeType)),
                        contentDescription = "mediaTypeIconPainter",
                        colorFilter = colorFilter,
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
                    text = song._artist,
                    color = color.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.05.em,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = durationText,
                    fontSize = 14.sp,
                    letterSpacing = 0.05.em,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}