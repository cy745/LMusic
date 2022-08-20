package com.lalilu.lmusic.screen.component.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.lmedia.extension.getDuration
import com.lalilu.lmusic.utils.rememberCoverWithFlow

@Composable
@OptIn(
    ExperimentalFoundationApi::class
)
fun PlayingCard(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val imagePainter = rememberCoverWithFlow(mediaItem = mediaItem) {
        crossfade(true)
        size(SizeUtils.dp2px(64f))
    }
//    val lrcIconPainter = rememberImagePainter(
//        data = mediaItem.getLyric()
//    )
    val mediaTypeIconPainter = painterResource(
        id = getMusicTypeIcon(mediaItem)
    )
    val titleText = mediaItem.mediaMetadata.title.toString()
    val artistText = mediaItem.mediaMetadata.artist.toString()
    val durationText = TimeUtils.millis2String(mediaItem.mediaMetadata.getDuration(), "mm:ss")
    val color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    val colorFilter = ColorFilter.tint(color = color.copy(alpha = 0.9f))

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
            elevation = 0.dp
        ) {
            val loaded = imagePainter.state.painter != null
            val painter = if (loaded) imagePainter else
                painterResource(id = R.drawable.ic_music_line)

            Image(
                painter = painter,
                contentDescription = "SongCardImage",
                contentScale = if (loaded) ContentScale.Crop else FixedScale(1f),
                modifier = Modifier
                    .size(64.dp)
                    .aspectRatio(1f)
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
                    color = color,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
//                    Image(
//                        painter = lrcIconPainter,
//                        contentDescription = "lrcIconPainter",
//                        colorFilter = colorFilter,
//                        modifier = Modifier
//                            .size(20.dp)
//                            .aspectRatio(1f)
//                    )
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