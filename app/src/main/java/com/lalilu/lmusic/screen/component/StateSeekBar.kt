package com.lalilu.lmusic.screen.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StateSeekBar(
    value: Float,
    selections: List<String>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),
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
        SliderImpl(
            enabled = enabled,
            selections = selections,
            positionFraction = positionFraction,
            tickFractions = tickFractions,
            draggableState = draggableState,
            colors = colors,
            width = maxPx - minPx,
            interactionSource = interactionSource,
            modifier = modifier2
        )
    }
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    selections: List<String>,
    positionFraction: Float,
    tickFractions: List<Float>,
    draggableState: SliderDraggableState,
    colors: SliderColors,
    width: Float,
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
    val titleColor = contentColorFor(backgroundColor = backgroundColor)
    val bgColor = titleColor.copy(0.2f)
    val thumbColor = titleColor.copy(0.7f)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
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
        Box(modifier.then(DefaultSliderConstraints)) {
            val offset = width * positionFraction

            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingAnim.value.dp)
                    .clip(RoundedCornerShape(radiusAnim.value.dp))
                    .background(color = bgColor)
            )
            Spacer(
                modifier = Modifier
                    .graphicsLayer { translationX = offset }
                    .fillMaxHeight()
                    .fillMaxWidth(1f / selections.size)
                    .padding(paddingAnim.value.dp)
                    .clip(RoundedCornerShape(radiusAnim.value.dp))
                    .background(color = thumbColor)
            )
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(selections.size),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.Center,
                userScrollEnabled = false
            ) {
                selections.forEach {
                    item {
                        Text(
                            text = it,
                            color = backgroundColor,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}