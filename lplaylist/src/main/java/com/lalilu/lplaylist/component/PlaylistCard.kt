package com.lalilu.lplaylist.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.R as ComponentR
import com.lalilu.lplaylist.entity.LPlaylist

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    playlist: LPlaylist,
    modifier: Modifier = Modifier,
    draggingModifier: Modifier = Modifier,
    isDragging: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    isSelecting: () -> Boolean = { false },
    onClick: (LPlaylist) -> Unit = {},
    onLongClick: (LPlaylist) -> Unit = {},
) {
    val elevation =
        animateDpAsState(if (isDragging()) 16.dp else 0.dp, label = "")

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .shadow(elevation.value)
            .background(MaterialTheme.colors.surface)
            .combinedClickable(
                onClick = { onClick(playlist) },
                onLongClick = { onLongClick(playlist) }
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.subtitle1,
                color = dayNightTextColor()
            )
            AnimatedVisibility(
                visible = playlist.subTitle.isNotBlank(),
                label = "SubTitleVisibility"
            ) {
                Text(
                    text = playlist.subTitle,
                    style = MaterialTheme.typography.body2,
                    color = dayNightTextColor(alpha = 0.8f)
                )
            }
        }

        Text(
            text = "${playlist.mediaIds.size}",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            color = dayNightTextColor(alpha = 0.8f)
        )

        AnimatedVisibility(
            visible = isSelecting(),
            label = "DragHandleVisibility"
        ) {
            Icon(
                modifier = draggingModifier,
                painter = painterResource(id = ComponentR.drawable.ic_draggable),
                contentDescription = "DragHandle",
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistCardPreview() {
    val playlist = LPlaylist(
        id = "12312312",
        title = "日语",
        subTitle = "日语相关的歌曲",
        coverUri = "",
        mediaIds = listOf("12312", "12312312")
    )
    PlaylistCard(
        playlist = playlist,
        isDragging = { false },
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaylistCardListPreview() {
    val playlist = LPlaylist(
        id = "12312312",
        title = "日语",
        subTitle = "日语相关的歌曲",
        coverUri = "",
        mediaIds = listOf("12312", "12312312")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PlaylistCard(
            playlist = playlist,
            isDragging = { false },
            onClick = {}
        )
        PlaylistCard(
            playlist = playlist,
            isDragging = { false },
            onClick = {}
        )
    }
}