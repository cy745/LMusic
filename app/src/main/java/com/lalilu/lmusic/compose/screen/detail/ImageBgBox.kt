package com.lalilu.lmusic.compose.screen.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.drawable.CrossfadeDrawable
import coil.request.ImageRequest


@Composable
fun ImageBgBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    imageData: Any? = null,
    imageModifier: Modifier = Modifier,
    imageCrossFadeDuration: Int = CrossfadeDrawable.DEFAULT_DURATION,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val model = remember(imageData) {
        imageData?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(imageCrossFadeDuration)
                .build()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = contentAlignment
    ) {
        AsyncImage(
            modifier = imageModifier,
            model = model,
            contentScale = ContentScale.Crop,
            contentDescription = ""
        )
        content()
    }
}