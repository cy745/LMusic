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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.utils.fetcher.getLyric
import com.lalilu.lmusic.utils.rememberCoverForOnce

@Composable
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalCoilApi::class
)
fun SongCard(
    modifier: Modifier = Modifier,
    index: Int,
    mediaItem: MediaItem,
    onSongSelected: (index: Int) -> Unit = { },
    onSongShowDetail: (mediaId: String) -> Unit = { }
) {
    val imagePainter = rememberCoverForOnce(mediaItem = mediaItem) {
        crossfade(true)
        size(SizeUtils.dp2px(64f))
    }
    val lrcIconPainter = rememberImagePainter(
        data = mediaItem.getLyric()
    )
    val mediaTypeIconPainter = painterResource(
        id = getMusicTypeIcon(mediaItem)
    )
    val titleText = mediaItem.mediaMetadata.title.toString()
    val artistText = "#${index + 1} ${mediaItem.mediaMetadata.artist}"
    val durationText = TimeUtils.millis2String(mediaItem.mediaMetadata.getDuration(), "mm:ss")
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    val colorFilter = ColorFilter.tint(color = color.copy(alpha = 0.9f))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onSongSelected(index) },
                onLongClick = { onSongShowDetail(mediaItem.mediaId) })
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
                    Image(
                        painter = lrcIconPainter,
                        contentDescription = "lrcIconPainter",
                        colorFilter = colorFilter,
                        modifier = Modifier
                            .size(20.dp)
                            .aspectRatio(1f)
                    )
                    Image(
                        painter = mediaTypeIconPainter,
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
            if (imagePainter.state.painter != null) {
                Image(
                    painter = imagePainter,
                    contentDescription = "SongCardImage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .aspectRatio(1f)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_music_line),
                    contentDescription = "",
                    contentScale = FixedScale(1f),
                    colorFilter = ColorFilter.tint(color = Color.LightGray),
                    modifier = Modifier
                        .size(64.dp)
                        .aspectRatio(1f)
                )
            }

        }
    }
}

@DrawableRes
fun getMusicTypeIcon(mediaItem: MediaItem?): Int {
    mediaItem ?: return R.drawable.ic_mp3_line
    val mimeType = mediaItem.localConfiguration?.mimeType ?: return R.drawable.ic_mp3_line
    val strings = mimeType.split("/").toTypedArray()

    return when (strings[strings.size - 1].uppercase()) {
        "FLAC" -> R.drawable.ic_flac_line
        "WAV", "X-WAV" -> R.drawable.ic_wav_line
        "APE" -> R.drawable.ic_ape_line
        "MPEG", "MP3" -> R.drawable.ic_mp3_line
        else -> R.drawable.ic_mp3_line
    }
}