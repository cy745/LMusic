package com.lalilu.lartist.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lalilu.component.R
import com.lalilu.component.card.PlayingTipIcon
import com.lalilu.component.extension.dayNightTextColor

@Composable
fun ArtistCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    songCount: Long,
    isSelected: () -> Boolean = { false },
    imageSource: () -> Any? = { null },
    isPlaying: () -> Boolean = { false },
    onClick: () -> Unit = {}
) {
    val bgColor = animateColorAsState(
        targetValue = if (isSelected()) MaterialTheme.colors.onBackground.copy(0.3f)
        else Color.Transparent, label = ""
    )

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind { drawRect(bgColor.value) }
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .wrapContentHeight()
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(30.dp)
                    ),
                maxLines = 1,
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
                overflow = TextOverflow.Ellipsis
            )

            subTitle?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    text = subTitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colors.onBackground.copy(0.5f),
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }

        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            imageSource()?.let { imageData ->
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    elevation = 2.dp
                ) {
                    val textColor = MaterialTheme.colors.background

                    AsyncImage(
                        modifier = Modifier
                            .height(64.dp)
                            .drawWithContent {
                                drawContent()
                                drawRect(color = textColor, alpha = 0.5f)
                            }
                            .aspectRatio(2f / 1f),
                        model = ImageRequest.Builder(LocalContext.current)
                            .size(200)
                            .data(imageData)
                            .placeholder(R.drawable.ic_music_line_bg_64dp)
                            .error(R.drawable.ic_music_line_bg_64dp)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Song Card Image"
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "$songCount 首歌曲",
                    maxLines = 1,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
                PlayingTipIcon(isPlaying = isPlaying)
            }
        }
    }
}