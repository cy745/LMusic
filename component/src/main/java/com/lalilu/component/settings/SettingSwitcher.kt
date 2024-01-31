package com.lalilu.component.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingSwitcher(
    state: MutableState<Boolean>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null
) = SettingSwitcher(
    state = state,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) }
)

@Composable
fun SettingSwitcher(
    state: MutableState<Boolean>,
    title: String,
    subTitle: String? = null
) {
    var value by state
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    SettingSwitcher(
        onContentStartClick = {
            println("set: $value")
            value = !value
            println("after set: $value")
        },
        contentStart = {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp
            )
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    color = textColor.copy(0.5f)
                )
            }
        }
    ) { interaction ->
        Switch(
            checked = value,
            onCheckedChange = { value = it },
            interactionSource = interaction,
            colors = SwitchDefaults.colors(
                checkedThumbColor = textColor.multiply(0.7f)
            )
        )
    }
}

@Composable
fun SettingSwitcher(
    onContentStartClick: () -> Unit = {},
    contentStart: @Composable () -> Unit = {},
    contentEnd: @Composable (MutableInteractionSource) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onContentStartClick
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            contentStart()
        }
        contentEnd(interactionSource)
    }
}

fun Color.multiply(value: Float): Color {
    return Color(red * value, green * value, blue * value)
}