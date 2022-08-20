package com.lalilu.lmusic.screen.component.card

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LAlbum

@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    album: LAlbum,
    drawText: Boolean = true,
    onAlbumSelected: (String) -> Unit = {}
) {
    val screenWidth = LocalDensity.current.run {
        (LocalConfiguration.current.screenWidthDp.dp.toPx() / 2).toInt()
    }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            elevation = 1.dp
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album)
                    .crossfade(true)
                    .size(screenWidth)
                    .build(),
                contentDescription = "albumCover"
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = rememberRipple(),
                                onClick = {
                                    onAlbumSelected(album.id)
                                }
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
                } else {
                    SubcomposeAsyncImageContent(
                        contentDescription = "albumCover",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = rememberRipple(),
                                onClick = {
                                    onAlbumSelected(album.id)
                                }
                            ),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
        AnimatedVisibility(visible = drawText) {
            Text(
                text = album.name,
                color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                    .copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            onAlbumSelected(album.id)
                        }
                    )
                    .padding(10.dp)
            )
        }
    }
}