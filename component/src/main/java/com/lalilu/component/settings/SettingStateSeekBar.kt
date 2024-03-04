package com.lalilu.component.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.base.StateSeekBar
import kotlin.math.roundToInt

@Composable
fun SettingStateSeekBar(
    state: MutableState<Int>,
    selection: List<String>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null,
    paddingValues: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
) = SettingStateSeekBar(
    state = state,
    selection = selection,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) },
    paddingValues = paddingValues
)

@Composable
fun SettingStateSeekBar(
    state: MutableState<Int>,
    selection: List<String>,
    title: String,
    subTitle: String? = null,
    paddingValues: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
) = SettingStateSeekBar(
    state = { state.value },
    onStateUpdate = { state.value = it },
    selection = selection,
    title = title,
    subTitle = subTitle,
    paddingValues = paddingValues
)

@Composable
fun SettingStateSeekBar(
    state: () -> Int,
    onStateUpdate: (Int) -> Unit,
    selection: List<String>,
    title: String,
    subTitle: String? = null,
    paddingValues: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
) {
    val tempValue = remember(state()) { mutableStateOf(state().toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { }
            )
            .padding(paddingValues),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp
        )
        StateSeekBar(
            value = tempValue.value,
            steps = selection.size - 2,
            selections = selection,
            onValueChange = { tempValue.value = it },
            valueRange = 0f..(selection.size - 1f),
            onValueChangeFinished = { onStateUpdate(tempValue.value.roundToInt()) }
        )
        if (subTitle != null) {
            Text(
                text = subTitle,
                fontSize = 12.sp,
                color = contentColorFor(
                    backgroundColor = MaterialTheme.colors.background
                ).copy(0.5f)
            )
        }
    }
}