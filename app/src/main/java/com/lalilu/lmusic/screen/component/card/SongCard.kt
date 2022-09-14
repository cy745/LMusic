package com.lalilu.lmusic.screen.component.card

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.utils.sources.LyricSourceFactory

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SongCard(
    modifier: Modifier = Modifier,
    index: Int,
    song: LSong,
    onSongSelected: (index: Int) -> Unit = { },
    onSongShowDetail: (mediaId: String) -> Unit = { }
) {
    val titleText = song.name
    val artistText = "#${index + 1} ${song._artist}"
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
                onClick = { onSongSelected(index) },
                onLongClick = { onSongShowDetail(song.id) })
            .padding(
                horizontal = 20.dp,
                vertical = 10.dp
            ),
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
                    text = titleText,
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
                    text = artistText,
                    color = color.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 0.05.em,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = durationText,
                    fontSize = 12.sp,
                    letterSpacing = 0.05.em,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
        Surface(
            elevation = 2.dp,
            shape = RoundedCornerShape(1.dp)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song)
                    .crossfade(true)
                    .size(SizeUtils.dp2px(64f))
                    .build(),
                contentDescription = "",
                modifier = Modifier
                    .size(64.dp)
                    .aspectRatio(1f)
            ) {
                if (painter.state is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent(
                        contentDescription = "SongCardImage",
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_line),
                        contentDescription = "",
                        contentScale = FixedScale(1f),
                        colorFilter = ColorFilter.tint(color = Color.LightGray)
                    )
                }
            }
        }
    }
}

@DrawableRes
fun getMusicTypeIcon(mimeType: String): Int {
    val strings = mimeType.split("/").toTypedArray()

    return when (strings[strings.size - 1].uppercase()) {
        "FLAC" -> R.drawable.ic_flac_line
        "WAV", "X-WAV" -> R.drawable.ic_wav_line
        "APE" -> R.drawable.ic_ape_line
        "MPEG", "MP3" -> R.drawable.ic_mp3_line
        else -> R.drawable.ic_mp3_line
    }
}