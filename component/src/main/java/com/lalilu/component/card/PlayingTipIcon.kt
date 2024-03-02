package com.lalilu.component.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty


@Composable
fun PlayingTipIcon(
    modifier: Modifier = Modifier,
    isPlaying: () -> Boolean = { false }
) {
    AnimatedVisibility(
        visible = isPlaying(),
        modifier = modifier.wrapContentWidth(),
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/90463-wave.json"))
        val properties = rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.STROKE_COLOR,
                value = Color(0xFF00AA1C).toArgb(),
                keyPath = arrayOf("**", "Group 1", "Stroke 1")
            ),
        )

        LottieAnimation(
            composition,
            modifier = Modifier
                .padding(end = 5.dp)
                .size(28.dp, 20.dp)
                .graphicsLayer {
                    scaleX = 2.5f
                    scaleY = 2.5f
                },
            iterations = LottieConstants.IterateForever,
            clipSpec = LottieClipSpec.Progress(0.06f, 0.9f),
            dynamicProperties = properties
        )
    }
}
