package com.lalilu.component.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.enableFor

@Composable
fun SettingSwitcher(
    state: MutableState<Boolean>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null,
    enableContentClickable: Boolean = true
) = SettingSwitcher(
    state = state,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) },
    enableContentClickable = enableContentClickable
)

@Composable
fun SettingSwitcher(
    state: MutableState<Boolean>,
    title: String,
    subTitle: String? = null,
    enableContentClickable: Boolean = true
) = SettingSwitcher(
    state = { state.value },
    onStateUpdate = { state.value = it },
    title = title,
    subTitle = subTitle,
    enableContentClickable = enableContentClickable
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingSwitcher(
    state: () -> Boolean,
    onStateUpdate: (Boolean) -> Unit,
    title: String,
    subTitle: String? = null,
    enableContentClickable: Boolean = true
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    SettingSwitcher(
        enableContentClickable = enableContentClickable,
        onContentStartClick = { onStateUpdate(!state()) },
        contentStart = {
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing(30.dp)
                ),
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
                    text = subTitle,
                    fontSize = 12.sp,
                    color = textColor.copy(0.5f)
                )
            }
        },
        contentEnd = { interaction ->
            Switch(
                checked = state(),
                onCheckedChange = { onStateUpdate(it) },
                interactionSource = interaction,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = textColor.multiply(0.7f)
                )
            )
        }
    )
}

@Composable
fun SettingSwitcher(
    enableContentClickable: Boolean = true,
    onContentStartClick: () -> Unit = {},
    contentStart: @Composable ColumnScope.() -> Unit = {},
    contentEnd: @Composable (MutableInteractionSource) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .enableFor(enable = { enableContentClickable }) {
                clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    onClick = onContentStartClick
                )
            }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            content = contentStart
        )
        contentEnd(interactionSource)
    }
}

fun Color.multiply(value: Float): Color {
    return Color(red * value, green * value, blue * value)
}