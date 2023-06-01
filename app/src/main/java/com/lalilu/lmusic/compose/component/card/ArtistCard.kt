package com.lalilu.lmusic.compose.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.lmedia.entity.LArtist

@Composable
fun ArtistCard(
    artist: LArtist,
    isPlaying: () -> Boolean = { false },
    onClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlayingTipIcon(isPlaying = isPlaying)
        Text(
            modifier = Modifier.weight(1f),
            text = artist.name,
            fontSize = 14.sp,
            color = textColor,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${artist.requireItemsCount()} 首歌曲",
            fontSize = 12.sp,
            color = textColor.copy(0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistCard(
    modifier: Modifier = Modifier,
    index: Int,
    artistName: String,
    songCount: Long,
    isPlaying: () -> Boolean = { false },
    onClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(
                start = 10.dp,
                end = 20.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.4f),
                text = "${index + 1}"
            )
            PlayingTipIcon(isPlaying = isPlaying)
            Text(
                text = artistName,
                fontSize = 14.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "$songCount 首歌曲",
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}