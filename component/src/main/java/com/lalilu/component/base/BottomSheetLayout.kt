package com.lalilu.component.base

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.component.override.ModalBottomSheetDefaults
import com.lalilu.component.override.ModalBottomSheetLayout
import com.lalilu.component.override.ModalBottomSheetValue
import com.lalilu.component.override.rememberModalBottomSheetState

@ExperimentalMaterialApi
@Composable
fun BottomSheetLayout(
    modifier: Modifier = Modifier,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = 0.dp,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    sheetGesturesEnabled: Boolean = true,
    skipHalfExpanded: Boolean = true,
    animationSpec: AnimationSpec<Float> = ModalBottomSheetDefaults.AnimationSpec,
    sheetContent: @Composable (enhanceSheetState: EnhanceSheetState) -> Unit = { },
    content: @Composable (enhanceSheetState: EnhanceSheetState) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = skipHalfExpanded,
        animationSpec = animationSpec
    )

    val enhanceSheetState = remember(sheetState) {
        EnhanceModalSheetState(
            sheetState = sheetState,
            scope = coroutineScope
        )
    }

    val scaleValue = remember(sheetState) {
        derivedStateOf {
            val state = sheetState.anchoredDraggableState
            val min = state.anchors.minAnchor()
            val max = state.anchors.maxAnchor()
            val offset = state.offset

            val fraction = offset.normalize(min, max)
            val scale = 0.8f + 0.2f * fraction
            scale.takeIf { !it.isNaN() } ?: 1f
        }
    }

    CompositionLocalProvider(LocalEnhanceSheetState provides enhanceSheetState) {
        ModalBottomSheetLayout(
            modifier = modifier,
            scrimColor = scrimColor,
            sheetState = sheetState,
            sheetShape = sheetShape,
            sheetElevation = sheetElevation,
            sheetBackgroundColor = sheetBackgroundColor,
            sheetContentColor = sheetContentColor,
            sheetGesturesEnabled = sheetGesturesEnabled,
            sheetContent = { sheetContent(enhanceSheetState) },
            content = {
                Surface(color = Color.Black) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleValue.value
                            scaleY = scaleX
                        }
                        .clip(RoundedCornerShape(32.dp)),
                        content = { content(enhanceSheetState) }
                    )
                }
            }
        )
    }
}

private fun Float.normalize(minValue: Float, maxValue: Float): Float {
    val min = minOf(minValue, maxValue)
    val max = maxOf(minValue, maxValue)

    if (min == max) return 0f
    if (this <= min) return 0f
    if (this >= max) return 1f

    return ((this - min) / (max - min))
        .coerceIn(0f, 1f)
}