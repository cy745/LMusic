package com.lalilu.component.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun SettingSmallProgressSeekBar(
    value: () -> Float,
    onValueUpdate: (Float) -> Unit = {},
    onFinishedUpdate: (Float) -> Unit = {},
    title: String,
    subTitle: String? = null,
    valueRange: IntRange
) {
    val tempValue = remember { mutableFloatStateOf(value()) }
    val interactionSource = remember { MutableInteractionSource() }
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { }
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(0.8f),
        ) {
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing(30.dp)
                ),
                maxLines = 1,
                text = title,
                color = textColor,
                fontSize = 14.sp
            )
            if (subTitle != null) {
                Text(
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(30.dp)
                    ),
                    maxLines = 1,
                    text = subTitle,
                    fontSize = 12.sp,
                    color = textColor.copy(0.5f)
                )
            }
        }

        ProgressSeekBar(
            modifier = Modifier.weight(1.2f),
            value = tempValue.floatValue,
            onValueChange = {
                tempValue.floatValue = it
                onValueUpdate(it)
            },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1,
            onValueChangeFinished = { onFinishedUpdate(tempValue.floatValue) }
        )
    }
}