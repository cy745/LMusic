package com.lalilu.lmusic.screen.component.card

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.lalilu.R
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.requirePalette

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecommendCard(
    data: () -> Any?,
    getId: () -> String,
    width: Dp = 200.dp,
    height: Dp = 125.dp,
    isPlaying: Boolean = false,
    onShowDetail: (id: String) -> Unit = {},
    onPlay: (id: String) -> Unit = {}
) {
    val context = LocalContext.current
    var cardMainColor by remember { mutableStateOf(Color.Gray) }

    Surface(
        elevation = 1.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onShowDetail(getId()) },
                model = ImageRequest.Builder(context)
                    .data(data())
                    .requirePalette {
                        cardMainColor = Color(it.getLightMutedColor(android.graphics.Color.GRAY))
                    }
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = "",
                placeholder = ColorPainter(dayNightTextColor(0.15f)),
                error = ColorPainter(dayNightTextColor(0.15f))
            )

            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = isPlaying,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (isPlaying) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/90463-wave.json"))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = cardMainColor.copy(alpha = 0.6f))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition,
                            iterations = LottieConstants.IterateForever,
                            clipSpec = LottieClipSpec.Progress(0.06f, 0.9f)
                        )
                    }
                }
            }

            AnimatedContent(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp), targetState = isPlaying
            ) {
                Surface(
                    elevation = 1.dp,
                    color = cardMainColor,
                    shape = CircleShape
                ) {
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = { onPlay(getId()) }) {
                        Icon(
                            painter = painterResource(id = if (it) R.drawable.ic_pause_line else R.drawable.ic_play_line),
                            contentDescription = ""
                        )
                    }
                }
            }
        }
    }
}