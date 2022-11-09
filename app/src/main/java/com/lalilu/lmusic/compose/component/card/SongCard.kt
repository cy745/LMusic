package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.repository.LyricHelper
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.dayNightTextColorFilter
import com.lalilu.lmusic.utils.extension.durationMsToString
import com.lalilu.lmusic.utils.extension.mimeTypeToIcon
import com.lalilu.lmusic.utils.recomposeHighlighter

@Composable
fun SongCard(
    modifier: Modifier = Modifier.recomposeHighlighter(),
    lyricHelper: LyricHelper,
    dragModifier: Modifier = Modifier,
    song: () -> LSong,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onEnterSelect: () -> Unit = {},
    isSelected: () -> Boolean = { false }
) {
    val item = remember { song() }
    val hasLyric by lyricHelper.rememberHasLyric(song = item)

    SongCard(
        modifier = modifier,
        dragModifier = dragModifier,
        title = { item.name },
        subTitle = { item._artist },
        mimeType = { item.mimeType },
        duration = { item.durationMs },
        hasLyric = { hasLyric },
        imageData = { item },
        onClick = onClick,
        onLongClick = onLongClick,
        onEnterSelect = onEnterSelect,
        isSelected = isSelected
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    title: () -> String,
    subTitle: () -> String,
    mimeType: () -> String,
    duration: () -> Long,
    hasLyric: () -> Boolean = { false },
    imageData: () -> Any?,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onEnterSelect: () -> Unit = {},
    isSelected: () -> Boolean = { false }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor by animateColorAsState(if (isSelected()) dayNightTextColor(0.15f) else Color.Transparent)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = bgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SongCardContent(
            modifier = Modifier.weight(1f),
            title = title,
            subTitle = subTitle,
            mimeType = mimeType,
            duration = duration,
            hasLyric = hasLyric
        )
        SongCardImage(
            modifier = dragModifier,
            imageData = imageData,
            interaction = interactionSource,
            onClick = onClick,
            onLongClick = onEnterSelect
        )
    }
}

@Composable
fun SongCardContent(
    modifier: Modifier = Modifier,
    title: () -> String,
    subTitle: () -> String,
    mimeType: () -> String,
    duration: () -> Long,
    hasLyric: () -> Boolean = { false }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title(),
                color = dayNightTextColor(),
                style = MaterialTheme.typography.subtitle1
            )
            HasLyricIcon(hasLyric = hasLyric)
            Image(
                painter = painterResource(id = mimeTypeToIcon(mimeType = mimeType())),
                contentDescription = "MediaType Icon",
                colorFilter = dayNightTextColorFilter(0.9f),
                modifier = Modifier
                    .size(20.dp)
                    .aspectRatio(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = subTitle(),
                color = dayNightTextColor(0.5f),
                style = MaterialTheme.typography.caption,
            )
            Text(
                text = durationMsToString(duration = duration()),
                fontSize = 12.sp,
                letterSpacing = 0.05.em,
                color = dayNightTextColor(0.7f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongCardImage(
    modifier: Modifier = Modifier,
    imageData: () -> Any?,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier,
        elevation = 2.dp,
        shape = RoundedCornerShape(5.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(64.dp)
                .aspectRatio(1f)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                    onClick = onClick
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageData())
                .placeholder(R.drawable.ic_music_line_bg_64dp)
                .error(R.drawable.ic_music_line_bg_64dp)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
            contentDescription = "Song Card Image"
        )
    }
}

@Composable
fun HasLyricIcon(
    hasLyric: () -> Boolean = { false }
) {
    val alpha by animateFloatAsState(targetValue = if (hasLyric()) 1f else 0f)

    Image(
        painter = painterResource(id = R.drawable.ic_lrc_fill),
        contentDescription = "Lyric Icon",
        colorFilter = dayNightTextColorFilter(0.9f),
        modifier = Modifier
            .size(20.dp)
            .aspectRatio(1f)
            .alpha(alpha)
    )
}