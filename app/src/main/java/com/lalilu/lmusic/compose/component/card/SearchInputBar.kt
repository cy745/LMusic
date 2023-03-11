package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.lmusic.compose.component.base.InputBar


@Composable
fun SearchInputBar(
    modifier: Modifier = Modifier,
    value: String,
    onSearchFor: (String) -> Unit,
    onChecked: () -> Unit
) {
    val text = remember { mutableStateOf(value) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        InputBar(
            modifier = Modifier.weight(1f),
            value = text,
            onSubmit = onSearchFor
        )
        IconButton(onClick = { onSearchFor(text.value) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_2_line),
                contentDescription = "搜索按钮"
            )
        }
        IconButton(onClick = onChecked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_line),
                contentDescription = "搜索按钮"
            )
        }
    }
}

@Composable
fun LyricCard(
    title: String,
    artist: String,
    albumTitle: String?,
    duration: String?,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)
    val color: Color by animateColorAsState(
        if (selected) contentColorFor(
            backgroundColor = MaterialTheme.colors.background
        ).copy(0.2f) else Color.Transparent
    )

    Surface(color = color) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                duration?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = textColor,
                        textAlign = TextAlign.End
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = artist,
                    fontSize = 12.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                albumTitle?.let {
                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }
            }
        }
    }
}