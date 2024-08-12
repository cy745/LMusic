package com.lalilu.component.base

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.lalilu.component.override.ModalBottomSheetState
import com.lalilu.component.override.ModalBottomSheetValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface EnhanceSheetState {
    val isVisible: Boolean
    fun show()
    fun hide()
    fun progress(from: Any, to: Any): Float
    fun dispatch(rawValue: Float) {}
    fun settle(velocity: Float) {}
}

val LocalEnhanceSheetState = compositionLocalOf<EnhanceSheetState?> { null }

class EnhanceBottomSheetState(
    private val bottomSheetState: BottomSheetState,
    private val scope: CoroutineScope,
) : EnhanceSheetState {
    override val isVisible: Boolean
        get() = bottomSheetState.isExpanded

    override fun show() {
        scope.launch {
            if (bottomSheetState.isCollapsed) {
                bottomSheetState.expand()
            }
        }
    }

    override fun hide() {
        scope.launch {
            if (bottomSheetState.isExpanded) {
                bottomSheetState.collapse()
            }
        }
    }

    override fun progress(from: Any, to: Any): Float {
        if (from !is BottomSheetValue || to !is BottomSheetValue) {
            return 0f
        }
        return bottomSheetState.progress(from, to)
    }
}


class EnhanceModalSheetState(
    private val sheetState: ModalBottomSheetState,
    private val scope: CoroutineScope
) : EnhanceSheetState {
    override val isVisible: Boolean by derivedStateOf {
        if (!sheetState.isSkipHalfExpanded) {
            return@derivedStateOf sheetState.progress(
                from = ModalBottomSheetValue.Hidden,
                to = ModalBottomSheetValue.HalfExpanded
            ) >= 0.95
        }

        sheetState.progress(
            from = ModalBottomSheetValue.Hidden,
            to = ModalBottomSheetValue.Expanded
        ) >= 0.95
    }

    override fun hide() {
        if (isVisible) {
            scope.launch { sheetState.hide() }
        }
    }

    override fun show() {
        if (!isVisible) {
            scope.launch { sheetState.show() }
        }
    }

    override fun progress(from: Any, to: Any): Float {
        if (from !is ModalBottomSheetValue || to !is ModalBottomSheetValue) {
            return 0f
        }
        return sheetState.progress(from, to)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun dispatch(rawValue: Float) {
        sheetState.anchoredDraggableState.dispatchRawDelta(rawValue)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun settle(velocity: Float) {
        scope.launch {
            sheetState.anchoredDraggableState.settle(velocity)
        }
    }
}