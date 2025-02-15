package com.lalilu.component.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.lalilu.component.base.ProgressSeekBar


@Composable
fun SettingProgressSeekBar(
    value: () -> Float,
    onValueUpdate: (Float) -> Unit = {},
    title: String,
    subTitle: String? = null,
    valueRange: IntRange
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
            value = tempValue.floatValue,
            onValueChange = {
                tempValue.floatValue = it
                onValueUpdate(it)
            },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1,
            onValueChangeFinished = { onValueUpdate(tempValue.floatValue) }
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