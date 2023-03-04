package com.lalilu.lmusic.compose.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.compose.component.base.InputBar

@Composable
fun NewPlaylistBar(
    onCancel: () -> Unit = {},
    onCommit: (String) -> Unit = {}
) {
    val text = remember { mutableStateOf("") }
    val isCommitEnable by remember(text.value) { derivedStateOf { text.value.isNotEmpty() } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        InputBar(
            modifier = Modifier.weight(1f),
            hint = "新建歌单",
            value = text,
            onSubmit = {
                onCommit(it)
                text.value = ""
            }
        )
        IconButton(onClick = onCancel) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_line),
                contentDescription = "取消按钮"
            )
        }
        IconButton(
            onClick = {
                onCommit(text.value)
                text.value = ""
            }, enabled = isCommitEnable
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_line),
                contentDescription = "确认按钮"
            )
        }
    }
}