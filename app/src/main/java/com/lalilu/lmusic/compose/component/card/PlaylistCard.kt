package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmusic.utils.extension.dayNightTextColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    icon: Int = R.drawable.ic_play_list_fill,
    iconTint: Color = LocalContentColor.current,
    getPlaylist: () -> LPlaylist,
    getIsSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val playlist = remember { getPlaylist() }
    val bgColor by animateColorAsState(if (getIsSelected()) dayNightTextColor(0.15f) else Color.Transparent)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 15.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Icon(
            modifier = dragModifier,
            painter = painterResource(id = icon),
            contentDescription = "",
            tint = iconTint.copy(alpha = 0.7f)
        )
        Text(
            modifier = Modifier.weight(1f),
            text = playlist.name,
            color = dayNightTextColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            modifier = dragModifier.padding(start = 20.dp),
            text = "${playlist.requireItemsCount()} 首歌曲",
            color = dayNightTextColor(0.5f),
            style = MaterialTheme.typography.subtitle2
        )
    }
}
