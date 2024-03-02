package com.lalilu.component.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.base.ProgressSeekBar
import kotlin.math.roundToInt

@Composable
fun SettingProgressSeekBar(
    state: MutableState<Int>,
    selection: List<String>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null
) = SettingStateSeekBar(
    state = state,
    selection = selection,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) }
)

@Composable
fun SettingProgressSeekBar(
    state: MutableState<Int>,
    title: String,
    subTitle: String? = null,
    valueRange: IntRange
) {
    var value by state
    val tempValue = remember(value) { mutableStateOf(value.toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { }
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp
        )
        ProgressSeekBar(
            value = tempValue.value,
            onValueChange = { tempValue.value = it },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1,
            onValueChangeFinished = {
                value = tempValue.value.roundToInt()
            }
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