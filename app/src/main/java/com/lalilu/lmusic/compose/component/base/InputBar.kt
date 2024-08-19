package com.lalilu.lmusic.compose.component.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.dayNightTextColor

@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    defaultValue: String = "",
    value: MutableState<String> = remember { mutableStateOf(defaultValue) },
    onValueChange: (String) -> Unit = { },
    onSubmit: (String) -> Unit = {},
) {
    val focusRequest = remember { FocusRequester() }
    val focused = remember { mutableStateOf(false) }
    val color = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
    val borderColor = animateColorAsState(
        targetValue = if (focused.value) Color(0xFF135CB6) else color,
        label = "TextField border color with focus"
    )

    BasicTextField(
        modifier = modifier
            .focusRequester(focusRequest)
            .onFocusChanged {
                focused.value = it.hasFocus && it.isFocused
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        keyboardActions = KeyboardActions(onSearch = {
            onSubmit(value.value)
        }),
        minLines = 1,
        textStyle = TextStyle.Default.copy(
            color = dayNightTextColor(),
            fontSize = 18.sp
        ),
        cursorBrush = SolidColor(dayNightTextColor()),
        value = value.value,
        onValueChange = {
            value.value = it
            onValueChange(it)
        }
    ) { innerTextField ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                border = BorderStroke(2.dp, borderColor.value),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    innerTextField()
                }
            }
        }
    }
}