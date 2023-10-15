package com.lalilu.lmusic.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
object DialogWrapper {
    private val tempCompose: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)

    fun show(content: @Composable () -> Unit) {
        tempCompose.value = content
    }

    @Composable
    fun collectBottomSheetIsExpended(
        sheetState: ModalBottomSheetState,
    ): State<Boolean> = remember {
        derivedStateOf {
            if (sheetState.currentValue == sheetState.targetValue && sheetState.progress == 1f) {
                return@derivedStateOf sheetState.currentValue == ModalBottomSheetValue.Expanded
            }

            when (sheetState.currentValue) {
                ModalBottomSheetValue.Hidden -> sheetState.progress >= 0.95f
                ModalBottomSheetValue.Expanded -> sheetState.progress <= 0.05f

                else -> false
            }
        }
    }


    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val sheetState = remember {
            ModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                density = density,
                animationSpec = SpringSpec(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 1000f
                ),
            )
        }
        val isBottomSheetExpended by collectBottomSheetIsExpended(sheetState)

        LaunchedEffect(tempCompose.value) {
            if (tempCompose.value != null) sheetState.show() else sheetState.hide()
        }

        ModalBottomSheetLayout(
            modifier = modifier.fillMaxSize(),
            sheetState = sheetState,
            sheetBackgroundColor = MaterialTheme.colors.background,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetContent = { tempCompose.value?.invoke() },
            content = {
                content()
                BackHandler(enabled = isBottomSheetExpended) {
                    scope.launch { sheetState.hide() }
                }
            }
        )
    }
}