package com.lalilu.lplaylist.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.lplaylist.entity.LPlaylist

@Composable
fun PlaylistCard(
    playlist: LPlaylist,
    isDragging: () -> Boolean
) {
    val elevation =
        animateDpAsState(if (isDragging()) 16.dp else 0.dp, label = "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation.value)
            .height(56.dp)
            .background(MaterialTheme.colors.surface)
    ) {
        Text(
            text = "${playlist.title}-${playlist.subTitle}",
            color = dayNightTextColor()
        )
    }
}