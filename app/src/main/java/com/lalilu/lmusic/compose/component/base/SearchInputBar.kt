package com.lalilu.lmusic.compose.component.base

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun SearchInputBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    value: MutableState<String>,
    onValueChange: (String) -> Unit = {},
    onSubmit: (String) -> Unit = {},
) {
    InputBar(
        modifier = modifier.fillMaxWidth(),
        hint = hint,
        value = value,
        onValueChange = onValueChange,
        onSubmit = onSubmit
    )
}