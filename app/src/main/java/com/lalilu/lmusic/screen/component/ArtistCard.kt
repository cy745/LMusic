package com.lalilu.lmusic.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.lalilu.lmusic.datasource.extensions.getArtistId

@Composable
fun ArtistCard(
    index: Int,
    mediaItem: MediaItem,
    onSelected: (String) -> Unit
) = ArtistCard(
    index = index,
    artistTitle = mediaItem.mediaMetadata.artist.toString(),
    onClick = { onSelected(mediaItem.mediaMetadata.getArtistId().toString()) }
)

@Composable
fun ArtistCard(
    index: Int,
    artistTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .heightIn(min = 48.dp)
            .padding(
                start = 10.dp,
                end = 20.dp
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = Color.DarkGray,
            text = "${index + 1}"
        )
        Text(
            text = artistTitle,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
