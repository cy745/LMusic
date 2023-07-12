package com.lalilu.lmusic.compose

import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp

object DrawerWrapper {

    @Composable
    fun Content(
        mainContent: @Composable () -> Unit,
        secondContent: @Composable () -> Unit,
    ) {
        val offsetX = remember { mutableStateOf(0f) }

        Layout(
            content = {
                mainContent()
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(20.dp)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = DraggableState { deltaX ->
                                offsetX.value += deltaX
                            }
                        )
                )
                secondContent()
            },
            measurePolicy = { measurables, constraints ->
                val spacer = measurables[1].measure(constraints)
                val lastXSpace = (constraints.maxWidth - spacer.width) / 2f

                val main =
                    measurables[0].measure(constraints.copy(maxWidth = (lastXSpace - offsetX.value).toInt()))
//                val second =
//                    measurables[2].measure(constraints.copy(maxWidth = (lastXSpace + offsetX.value).toInt()))

                layout(constraints.maxWidth, constraints.maxHeight) {
                    main.place(0, 0)
                    spacer.place(main.width, 0)
//                    second.place(main.width + spacer.width, 0)
                }
            }
        )
    }
}