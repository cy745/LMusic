package com.lalilu.component.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lalilu.component.lumo.components.Switch

@Composable
fun SettingSwitcher(
    modifier: Modifier = Modifier,
    state: MutableState<Boolean>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null,
    enableContentClickable: Boolean = true
) = SettingSwitcher(
    modifier = modifier,
    state = state,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) },
    enableContentClickable = enableContentClickable
)

@Composable
fun SettingSwitcher(
    modifier: Modifier = Modifier,
    state: MutableState<Boolean>,
    title: String,
    subTitle: String? = null,
    enableContentClickable: Boolean = true
) = SettingSwitcher(
    modifier = modifier,
    state = { state.value },
    onStateUpdate = { state.value = it },
    title = title,
    subTitle = subTitle,
    enableContentClickable = enableContentClickable
)

@Composable
fun SettingSwitcher(
    modifier: Modifier = Modifier,
    state: () -> Boolean,
    onStateUpdate: (Boolean) -> Unit,
    title: String,
    subTitle: String? = null,
    enableContentClickable: Boolean = true
) {
    SettingBaseItem(
        modifier = modifier,
        title = title,
        subTitle = subTitle,
        enableContentClickable = enableContentClickable,
        onContentStartClick = { onStateUpdate(!state()) },
        contentEnd = { interaction ->
            Switch(
                checked = state(),
                onCheckedChange = { onStateUpdate(it) },
                interactionSource = interaction,
            )
        }
    )
}