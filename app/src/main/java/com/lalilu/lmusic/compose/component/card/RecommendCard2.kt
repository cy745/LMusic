package com.lalilu.lmusic.compose.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LSong

@Composable
fun RecommendCard2(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    item: () -> LSong,
    onClick: () -> Unit = {}
) {
    val song = remember { item() }

    RecommendCard2(
        modifier = modifier,
        contentModifier = contentModifier,
        imageData = { song },
        title = { song.name },
        subTitle = { song.metadata.artist },
        onClick = onClick
    )
}

@Composable
fun RecommendCard2(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    imageData: () -> Any?,
    title: () -> String,
    subTitle: () -> String,
    onClick: () -> Unit = {}
) {
    RecommendCardCover(
        modifier = modifier.width(IntrinsicSize.Min),
        contentModifier = contentModifier,
        imageData = imageData,
        onClick = onClick
    ) { palette ->
        val mainColor =  Color(
            palette()?.getDarkVibrantColor(android.graphics.Color.GRAY)
                ?: android.graphics.Color.GRAY
        )

        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        0.5f to Color.Transparent,
                        1.0f to mainColor,
                        start = Offset.Zero,
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .fillMaxSize()
                .padding(20.dp)
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Bottom),
        ) {
            ExpendableTextCard(
                title = title,
                subTitle = subTitle,
                titleColor = Color.White,
                subTitleColor = Color.White.copy(0.8f)
            )
        }
    }
}