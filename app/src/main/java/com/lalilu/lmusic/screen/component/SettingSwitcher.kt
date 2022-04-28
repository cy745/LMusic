package com.lalilu.lmusic.screen.component

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState

@Composable
fun SettingSwitcher(
    state: DataSaverMutableState<Boolean>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null
) = SettingSwitcher(
    state = state,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) }
)

@Composable
fun SettingSwitcher(
    state: DataSaverMutableState<Boolean>,
    title: String,
    subTitle: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()
    var value by state
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = { value = !value }
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = title)
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
        Switch(
            checked = value,
            onCheckedChange = { value = it },
            interactionSource = interactionSource
        )
    }
}