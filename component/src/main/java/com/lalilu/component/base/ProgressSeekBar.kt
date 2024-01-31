package com.lalilu.component.base

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun ProgressSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinished: (() -> Unit)? = null,
) {
    SliderContainer(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        interactionSource = interactionSource,
        colors = colors,
        onValueChangeFinished = onValueChangeFinished
    ) { positionFraction, tickFractions, draggableState, minPx, maxPx, modifier2 ->
        ProgressSliderImpl(
            enabled = enabled,
            positionFraction = positionFraction,
            tickFractions = tickFractions,
            draggableState = draggableState,
            colors = colors,
            minPx = minPx,
            maxPx = maxPx,
            value = value,
            valueRange = valueRange,
            interactionSource = interactionSource,
            modifier = modifier2
        )
    }
}

@Composable
fun ProgressSliderImpl(
    enabled: Boolean,
    positionFraction: Float,
    tickFractions: List<Float>,
    draggableState: SliderDraggableState,
    colors: SliderColors,
    minPx: Float,
    maxPx: Float,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    interactionSource: MutableInteractionSource,
    modifier: Modifier
) {
    val interactions = remember { mutableStateListOf<Interaction>() }
    val paddingAnim = animateFloatAsState(
        targetValue = if (interactions.isNotEmpty()) 3f else 0f
    )
    val radiusAnim = animateFloatAsState(
        targetValue = if (interactions.isNotEmpty()) 8f else 10f
    )
    val backgroundColor = MaterialTheme.colors.background
    val bgColor = contentColorFor(backgroundColor = backgroundColor).copy(0.2f)
    val thumbColor = contentColorFor(backgroundColor = backgroundColor).copy(0.7f)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    Surface(shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = modifier
                .then(DefaultSliderConstraints)
                .fillMaxSize()
                .padding(paddingAnim.value.dp)
                .clip(RoundedCornerShape(radiusAnim.value.dp))
                .background(color = bgColor)
        ) {
            val offset = (minPx + maxPx) * positionFraction
            val offsetDp = LocalDensity.current.run { offset.toDp() }
            val widthPx = LocalDensity.current.run { 64.dp.toPx() }

            Text(
                color = backgroundColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.CenterEnd),
                text = "${valueRange.endInclusive.roundToInt()}"
            )

            Spacer(
                modifier = Modifier
                    .width(offsetDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(radiusAnim.value.dp))
                    .background(color = thumbColor)
            )

            Text(
                color = backgroundColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        translationX = (offset - widthPx).coerceAtLeast(0f)
                    },
                text = "${value.roundToInt()}"
            )
        }
    }
}