package com.lalilu.lmusic.screen.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.requirePalette
import com.lalilu.lmusic.utils.recomposeHighlighter

@Composable
fun RecommendCard2(
    modifier: Modifier = Modifier,
    data: () -> Any?,
    getId: () -> String,
    width: Dp = 200.dp,
    height: Dp = 125.dp,
    isPlaying: Boolean = false,
    onShowDetail: (id: String) -> Unit = {},
    onPlay: (id: String) -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var cardMainColor by remember { mutableStateOf(Color.Gray) }
    val gradientStartOffsetY = remember(density) { density.run { height.toPx() } / 2f }

    Surface(
        modifier = modifier.recomposeHighlighter(),
        elevation = 1.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clickable { onShowDetail(getId()) }
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(context)
                    .requirePalette {
                        cardMainColor = Color(it.getDarkVibrantColor(android.graphics.Color.GRAY))
                    }
                    .data(data())
                    .build(),
                contentDescription = "",
                placeholder = ColorPainter(dayNightTextColor(0.15f)),
                error = ColorPainter(dayNightTextColor(0.15f))
            )
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                cardMainColor
                            ),
                            start = Offset(0f, gradientStartOffsetY),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .fillMaxSize()
                    .padding(20.dp)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Bottom),
                content = bottomContent
            )
        }
    }
}