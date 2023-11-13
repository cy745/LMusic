package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*
import com.lalilu.R
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.utils.coil.requirePalette
import com.lalilu.component.extension.dayNightTextColor

@Composable
fun RecommendCardForAlbum(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    item: () -> LAlbum,
    width: () -> Dp = { 150.dp },
    height: () -> Dp = { 150.dp },
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RecommendCardCover(
            contentModifier = contentModifier,
            width = width,
            height = height,
            imageData = { item() },
            onClick = onClick
        )
        ExpendableTextCard(
            title = { item().name },
            subTitle = { item().artistName },
            maxWidth = width
        )
    }
}

@Composable
fun RecommendCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    item: () -> LSong,
    isPlaying: () -> Boolean = { false },
    width: () -> Dp = { 150.dp },
    height: () -> Dp = { 150.dp },
    onClick: () -> Unit = {},
    onClickButton: () -> Unit = {}
) {
    val song = remember { item() }

    RecommendCard(
        modifier = modifier,
        contentModifier = contentModifier,
        imageData = { song },
        title = { song.name },
        subTitle = { song._artist },
        isPlaying = isPlaying,
        width = width,
        height = height,
        onClick = onClick,
        onClickButton = onClickButton
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecommendCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    imageData: () -> Any?,
    title: () -> String,
    subTitle: () -> String,
    isPlaying: () -> Boolean = { false },
    width: () -> Dp = { 150.dp },
    height: () -> Dp = { 150.dp },
    onClick: () -> Unit = {},
    onClickButton: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/90463-wave.json"))
    val properties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.STROKE_COLOR,
            value = dayNightTextColor().toArgb(),
            keyPath = arrayOf("**", "Group 1", "Stroke 1")
        )
    )

    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RecommendCardCover(
            contentModifier = contentModifier,
            width = width,
            height = height,
            imageData = imageData,
            onClick = onClick
        ) { palette ->
            val mainColor = Color(
                palette()?.getLightMutedColor(android.graphics.Color.GRAY)
                    ?: android.graphics.Color.GRAY
            )
            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = isPlaying(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                0.3f to Color.Transparent,
                                1.0f to mainColor,
                                start = Offset.Zero,
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                        .align(Alignment.Center), contentAlignment = Alignment.BottomStart
                ) {
                    LottieAnimation(
                        composition,
                        modifier = Modifier.size(50.dp, 50.dp),
                        iterations = LottieConstants.IterateForever,
                        clipSpec = LottieClipSpec.Progress(0.06f, 0.9f),
                        dynamicProperties = properties
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp),
                elevation = 1.dp,
                color = mainColor,
                shape = CircleShape
            ) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = onClickButton
                ) {
                    AnimatedContent(targetState = isPlaying()) { playing ->
                        Icon(
                            painter = painterResource(id = if (playing) R.drawable.ic_pause_line else R.drawable.ic_play_line),
                            contentDescription = "Play / Pause Button"
                        )
                    }
                }
            }
        }

        ExpendableTextCard(
            title = title, subTitle = subTitle, maxWidth = width
        )
    }
}

@Composable
fun RecommendCardCover(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    elevation: Dp = 1.dp,
    width: () -> Dp = { 150.dp },
    height: () -> Dp = { 150.dp },
    shape: Shape = RoundedCornerShape(10.dp),
    imageData: () -> Any?,
    onClick: () -> Unit = {},
    extraContent: @Composable BoxScope.(palette: () -> Palette?) -> Unit = {}
) {
    var palette by remember { mutableStateOf<Palette?>(null) }

    Surface(
        modifier = modifier, elevation = elevation, shape = shape
    ) {
        Box(
            modifier = contentModifier
                .width(width())
                .height(height())
                .background(color = dayNightTextColor(0.15f))
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                model = ImageRequest.Builder(LocalContext.current).data(imageData())
                    .placeholder(R.drawable.ic_music_2_line_100dp)
                    .error(R.drawable.ic_music_2_line_100dp)
                    .requirePalette { palette = it }
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = "Recommend Card Cover Image"
            )
            extraContent { palette }
        }
    }
}