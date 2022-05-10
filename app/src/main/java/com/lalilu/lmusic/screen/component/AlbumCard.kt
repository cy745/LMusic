package com.lalilu.lmusic.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.lalilu.R
import com.lalilu.lmusic.datasource.extensions.getAlbumId

@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    drawText: Boolean = true,
    onAlbumSelected: (String) -> Unit = {}
) {
    val albumTitle = mediaItem.mediaMetadata.albumTitle.toString()
    val albumCoverUri = mediaItem.mediaMetadata.artworkUri
    val screenWidth = LocalDensity.current.run {
        (LocalConfiguration.current.screenWidthDp.dp.toPx() / 2).toInt()
    }
    val painter = rememberImagePainter(data = albumCoverUri) {
        crossfade(true)
        size(screenWidth)
    }
    AlbumCard(
        modifier = modifier,
        text = albumTitle,
        painter = painter,
        drawText = drawText,
        onClick = {
            onAlbumSelected(mediaItem.mediaMetadata.getAlbumId().toString())
        }
    )
}

@Composable
@OptIn(ExperimentalCoilApi::class)
fun AlbumCard(
    modifier: Modifier = Modifier,
    painter: ImagePainter,
    text: String? = null,
    drawText: Boolean = true,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            elevation = 1.dp
        ) {
            if (painter.state.painter != null) {
                Image(
                    painter = painter,
                    contentDescription = "albumCover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(),
                            onClick = onClick
                        ),
                    contentScale = ContentScale.FillWidth
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(),
                            onClick = onClick
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_2_line),
                        contentDescription = "",
                        contentScale = FixedScale(3.0f),
                        colorFilter = ColorFilter.tint(color = Color.LightGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
        }
        AnimatedVisibility(visible = drawText && text != null) {
            Text(
                text = text!!,
                color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                    .copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .padding(10.dp)
            )
        }
    }
}