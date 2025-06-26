package com.lalilu.component.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.lumo.components.Slider
import com.lalilu.component.lumo.components.SliderDefaults

@Composable
fun SettingFloatProgressSeekBar(
    value: () -> Float,
    onValueUpdate: (Float) -> Unit = {},
    onFinishedUpdate: (Float) -> Unit = {},
    title: String,
    subTitle: String? = null,
    valueRange: ClosedFloatingPointRange<Float>
) {
    val tempValue = remember { mutableFloatStateOf(value()) }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp
            )
            Text(
                text = "%.2f / [${valueRange.start}, ${valueRange.endInclusive}]".format(tempValue.floatValue),
                fontSize = 10.sp,
                color = MaterialTheme.colors.onBackground.copy(0.5f)
            )
        }
        Slider(
            modifier = Modifier.height(28.dp),
            value = tempValue.floatValue,
            onValueChange = {
                tempValue.floatValue = it
                onValueUpdate(it)
            },
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colors.onBackground.copy(0.8f),
                inactiveTrackColor = MaterialTheme.colors.onBackground.copy(0.2f)
            ),
            valueRange = valueRange,
            onValueChangeFinished = { onFinishedUpdate(tempValue.floatValue) }
        )
        Row {
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onBackground.copy(0.5f)
                )
            }
        }
    }
}