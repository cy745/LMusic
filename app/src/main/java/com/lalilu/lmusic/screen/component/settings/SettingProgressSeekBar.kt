package com.lalilu.lmusic.screen.component.settings

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState
import com.lalilu.lmusic.screen.component.ProgressSeekBar
import kotlin.math.roundToInt

@Composable
fun SettingProgressSeekBar(
    state: DataSaverMutableState<Int>,
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
    state: DataSaverMutableState<Int>,
    title: String,
    subTitle: String? = null,
    valueRange: IntRange
) {
    var value by state
    val tempValue = remember(value) { mutableStateOf(value.toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }

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