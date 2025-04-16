package com.lalilu.lhistory.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lalilu.RemixIcon
import com.lalilu.component.R
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.repeatLine
import kotlinx.datetime.Instant
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Preview
@Composable
fun HistoryItemCard(
    modifier: Modifier = Modifier,
    title: () -> String = { "title" },
    imageData: () -> Any? = { null },
    startTime: () -> Long = { System.currentTimeMillis() },
    repeatCount: () -> Int = { 0 },
    duration: () -> Long = { 0 },
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val data = remember(imageData()) {
        ImageRequest.Builder(context)
            .data(imageData())
            .placeholder(R.drawable.ic_music_line_bg_64dp)
            .error(R.drawable.ic_music_line_bg_64dp)
            .crossfade(true)
            .build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                modifier = Modifier,
                text = title(),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colors.onBackground,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = repeatCount() > 0,
                    enter = fadeIn() + expandHorizontally(clip = false),
                    exit = fadeOut() + shrinkHorizontally(clip = false)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color = MaterialTheme.colors.onBackground.copy(0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(10.dp),
                            imageVector = RemixIcon.Media.repeatLine,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onBackground,
                        )

                        Text(
                            modifier = Modifier,
                            text = "${repeatCount()}",
                            color = MaterialTheme.colors.onBackground,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val timeAgo = remember(startTime()) {
                    HumanReadable.timeAgo(Instant.fromEpochMilliseconds(startTime()))
                }
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = timeAgo,
                    color = MaterialTheme.colors.onBackground.copy(0.6f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                AnimatedVisibility(
                    visible = duration() > 1000,
                    enter = fadeIn() + expandHorizontally(clip = false),
                    exit = fadeOut() + shrinkHorizontally(clip = false)
                ) {
                    val durationStr = remember(duration()) {
                        HumanReadable.duration(duration().toDuration(DurationUnit.MILLISECONDS))
                    }
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = durationStr,
                        color = MaterialTheme.colors.onBackground.copy(0.6f),
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        Surface(
            modifier = modifier,
            elevation = 2.dp,
            shape = RoundedCornerShape(5.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(60.dp)
                    .aspectRatio(1f),
                model = data,
                contentScale = ContentScale.Crop,
                contentDescription = "Song Card Image"
            )
        }
    }
}