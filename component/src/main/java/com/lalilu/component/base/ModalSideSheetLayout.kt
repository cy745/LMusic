package com.lalilu.component.base

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

class ModalSideSheetState(
    initialState: Boolean = false
) {
    var isVisible: Boolean by mutableStateOf(initialState)
}

@Composable
fun rememberModalSideSheetState(
    initialState: Boolean = false
): ModalSideSheetState {
    return remember {
        ModalSideSheetState(
            initialState = initialState
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun ModalSideSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.CenterStart,
    sheetState: ModalSideSheetState = rememberModalSideSheetState(),
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val maxModalSheetWidth = 450.dp
    val maxModalSheetWidthPx = LocalDensity.current.run { maxModalSheetWidth.roundToPx() }

    val progress = animateFloatAsState(
        targetValue = if (sheetState.isVisible) 100f else 0f,
        animationSpec = animationSpec,
        label = "isVisibleProgress"
    )

    BoxWithConstraints(
        modifier.clipToBounds()
    ) {
        Box(Modifier.fillMaxSize()) {
            content()
            Scrim(
                visible = sheetState.isVisible,
                onDismiss = { sheetState.isVisible = false },
                color = scrimColor,
            )
        }

        Surface(
            Modifier
                .align(alignment) // We offset from the top so we'll center from there
                .widthIn(max = maxModalSheetWidth)
                .fillMaxWidth()
                .offset {
                    val multiply = if (alignment == Alignment.CenterEnd) 1f else -1f
                    val offsetX = lerp(
                        start = 0f,
                        stop = maxModalSheetWidthPx.toFloat(),
                        fraction = (1f - (progress.value / 100f)) * multiply
                    )
                    IntOffset(offsetX.roundToInt(), 0)
                },
            shape = sheetShape,
            elevation = sheetElevation,
            color = sheetBackgroundColor,
            contentColor = sheetContentColor
        ) {
            Column(content = sheetContent)
        }
    }
}

@Composable
private fun Scrim(
    color: Color,
    onDismiss: () -> Unit,
    visible: Boolean
) {
    if (color.isSpecified) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = TweenSpec(),
            label = ""
        )
        val closeSheet = "关闭菜单"
        val dismissModifier = if (visible) {
            Modifier
                .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
                .semantics(mergeDescendants = true) {
                    contentDescription = closeSheet
                    onClick { onDismiss(); true }
                }
        } else {
            Modifier
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissModifier)
        ) {
            drawRect(color = color, alpha = alpha)
        }
    }
}
