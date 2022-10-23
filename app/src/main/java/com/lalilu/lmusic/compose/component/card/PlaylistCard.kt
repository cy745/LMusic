package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmusic.utils.extension.dayNightTextColor

@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    getPlaylist: () -> LPlaylist,
    isSelected: (LPlaylist) -> Boolean,
    iconPainter: Painter? = null,
    iconTint: Color = dayNightTextColor(),
) {
    val playlist = remember { getPlaylist() }
    val bgColor by animateColorAsState(if (isSelected(playlist)) dayNightTextColor(0.15f) else Color.Transparent)

    PlaylistCard(
        modifier = modifier,
        title = playlist.name,
        subTitle = "${playlist.songs.size} 首歌曲",
        bgColor = bgColor
    )
}

@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    iconPainter: Painter? = null,
    iconTint: Color = dayNightTextColor(),
    bgColor: Color = Color.Transparent,
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = "",
                    tint = iconTint.copy(alpha = 0.7f)
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                color = dayNightTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.subtitle1
            )
            if (!subTitle.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = subTitle,
                    color = dayNightTextColor(0.5f),
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}