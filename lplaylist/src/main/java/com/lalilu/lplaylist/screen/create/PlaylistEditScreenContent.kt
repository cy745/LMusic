package com.lalilu.lplaylist.screen.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.base.NavigatorHeader

@Composable
internal fun PlaylistEditScreenContent(
    titleHint: () -> String = { "" },
    subTitleHint: () -> String = { "" },
    isEditing: () -> Boolean = { false },
    titleValue: () -> String = { "" },
    subTitleValue: () -> String = { "" },
    onUpdateTitle: (String) -> Unit = {},
    onUpdateSubTitle: (String) -> Unit = {}
) {
    val focusRequestForTitle = remember { FocusRequester() }
    val focusRequestForSubTitle = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            NavigatorHeader(
                modifier = Modifier.statusBarsPadding(),
                title = if (isEditing()) "更新歌单" else "创建歌单",
                subTitle = if (isEditing()) "更新歌单" else "创建歌单",
            )
        }
        item {
            EditText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                title = "标题",
                text = titleValue,
                focusRequester = focusRequestForTitle,
                onUpdateText = onUpdateTitle,
                onNext = { focusRequestForSubTitle.requestFocus() }
            )
        }
        item {
            EditText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                title = "简介/副标题",
                text = subTitleValue,
                focusRequester = focusRequestForSubTitle,
                onUpdateText = onUpdateSubTitle,
                onDone = {
                    keyboard?.hide()
                    focusRequestForTitle.freeFocus()
                    focusRequestForSubTitle.freeFocus()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistEditScreenContentPreview() {
    MaterialTheme {
        PlaylistEditScreenContent(
            titleValue = { "Title" },
            subTitleValue = { "SubTitle" }
        )
    }
}

@Composable
fun EditText(
    modifier: Modifier = Modifier,
    title: String,
    focusRequester: FocusRequester,
    text: () -> String = { "" },
    onUpdateText: (String) -> Unit = {},
    onFocus: () -> Unit = {},
    onNext: (KeyboardActionScope.() -> Unit)? = null,
    onDone: (KeyboardActionScope.() -> Unit)? = null
) {
    val focused = remember { mutableStateOf(false) }

    BasicTextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                focused.value = it.hasFocus && it.isFocused
                if (it.hasFocus && it.isFocused) onFocus()
            }
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = when {
                onDone != null -> ImeAction.Done
                onNext != null -> ImeAction.Next
                else -> ImeAction.Default
            },
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true
        ),
        keyboardActions = KeyboardActions(
            onNext = onNext,
            onDone = onDone
        ),
        textStyle = TextStyle.Default.copy(
            color = MaterialTheme.colors.onBackground,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        minLines = 2,
        cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
        value = text(),
        onValueChange = onUpdateText
    ) { innerTextField ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onBackground
                )
            }

            Surface(
                color = MaterialTheme.colors.onBackground.copy(0.05f),
                shape = RoundedCornerShape(8.dp),
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