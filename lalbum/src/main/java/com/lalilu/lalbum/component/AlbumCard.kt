package com.lalilu.lalbum.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.component.R as ComponentR
import com.lalilu.component.card.PlayingTipIcon
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.component.extension.requirePalette
import com.lalilu.component.extension.dayNightTextColor


@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    album: () -> LAlbum,
    showTitle: () -> Boolean = { true },
    isPlaying: () -> Boolean = { false },
    onClick: () -> Unit = {}
) {
    val item = remember { album() }

    AlbumCard(
        modifier = modifier,
        imageData = { item },
        title = { item.name },
        showTitle = showTitle,
        isPlaying = isPlaying,
        onClick = onClick
    )
}

@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    imageData: () -> Any?,
    title: () -> String,
    showTitle: () -> Boolean = { true },
    isPlaying: () -> Boolean = { false },
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        AlbumCoverCard(
            imageData = imageData,
            onClick = onClick,
            isPlaying = isPlaying,
            interactionSource = interactionSource
        )
        AlbumTitleText(
            title = title,
            showTitle = showTitle,
            onClick = onClick,
            interactionSource = interactionSource
        )
    }
}

@Composable
fun AlbumTitleText(
    modifier: Modifier = Modifier,
    title: () -> String,
    showTitle: () -> Boolean = { true },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {}
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = showTitle(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 5.dp, vertical = 10.dp),
            text = title(),
            color = dayNightTextColor(0.8f),
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlbumCoverCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp,
    imageData: () -> Any?,
    onClick: () -> Unit,
    isPlaying: () -> Boolean = { false },
    shape: Shape = RoundedCornerShape(5.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var palette by remember { mutableStateOf<Palette?>(null) }
    val mainColor = animateColorAsState(
        targetValue = if (isPlaying() && palette != null)
            Color(
                palette?.getVibrantColor(android.graphics.Color.GRAY)
                    ?: android.graphics.Color.GRAY
            ) else Color.Transparent
    )

    Surface(
        shape = shape,
        modifier = modifier,
        elevation = elevation,
        interactionSource = interactionSource,
        onClick = onClick
    ) {
        Box {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.linearGradient(
                                0.3f to Color.Transparent,
                                1.0f to mainColor.value,
                                start = Offset.Zero,
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageData())
                    .placeholder(ComponentR.drawable.ic_music_2_line_100dp)
                    .error(ComponentR.drawable.ic_music_2_line_100dp)
                    .crossfade(true)
                    .requirePalette { palette = it }
                    .build(),
                contentScale = ContentScale.FillWidth,
                contentDescription = "Album Cover"
            )
            PlayingTipIcon(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 15.dp, bottom = 15.dp),
                isPlaying = isPlaying
            )
        }
    }
}