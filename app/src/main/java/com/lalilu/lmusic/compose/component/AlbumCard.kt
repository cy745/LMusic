package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.utils.extension.dayNightTextColor


@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    album: () -> LAlbum,
    showTitle: () -> Boolean = { true },
    onClick: () -> Unit = {}
) {
    val item = remember { album() }

    AlbumCard(
        modifier = modifier,
        imageData = { item },
        title = { item.name },
        showTitle = showTitle,
        onClick = onClick
    )
}

@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    imageData: () -> Any?,
    title: () -> String,
    showTitle: () -> Boolean = { true },
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
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = showTitle()
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
    imageData: () -> Any?,
    onClick: () -> Unit,
    elevation: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(5.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Surface(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            contentDescription = "Album Cover",
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageData())
                .placeholder(R.drawable.ic_music_2_line_100dp)
                .error(R.drawable.ic_music_2_line_100dp)
                .crossfade(true)
                .build()
        )
    }
}