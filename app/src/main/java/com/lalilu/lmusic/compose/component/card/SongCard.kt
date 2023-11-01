package com.lalilu.lmusic.compose.component.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.common.base.Playable
import com.lalilu.common.base.Sticker
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.dayNightTextColorFilter
import com.lalilu.lmusic.utils.extension.durationMsToString
import com.lalilu.lmusic.utils.extension.mimeTypeToIcon

@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    song: () -> Playable,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onEnterSelect: () -> Unit = {},
    hasLyric: () -> Boolean = { false },
    isPlaying: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    showPrefix: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false },
    prefixContent: @Composable (Modifier) -> Unit = {}
) {
    val item = remember { song() }

    SongCard(
        modifier = modifier,
        dragModifier = dragModifier,
        title = { item.title },
        subTitle = { item.subTitle },
        duration = { item.durationMs },
        sticker = { item.sticker },
        imageData = { item },
        onClick = onClick,
        onLongClick = onLongClick,
        onEnterSelect = onEnterSelect,
        hasLyric = hasLyric,
        isPlaying = isPlaying,
        isSelected = isSelected,
        showPrefix = showPrefix,
        fixedHeight = fixedHeight,
        prefixContent = prefixContent
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    title: () -> String,
    subTitle: () -> String,
    duration: () -> Long,
    sticker: () -> List<Sticker>,
    imageData: () -> Any?,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onEnterSelect: () -> Unit = {},
    hasLyric: () -> Boolean = { false },
    isPlaying: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    showPrefix: () -> Boolean = { false },
    prefixContent: @Composable (Modifier) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor by animateColorAsState(
        targetValue = if (isSelected()) dayNightTextColor(0.15f) else Color.Transparent,
        label = ""
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(2.dp))
            .background(color = bgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SongCardContent(
            modifier = Modifier.weight(1f),
            title = title,
            subTitle = subTitle,
            duration = duration,
            isPlaying = isPlaying,
            showPrefix = showPrefix,
            fixedHeight = fixedHeight,
            prefixContent = prefixContent,
            stickerContent = {
                HasLyricIcon(
                    hasLyric = hasLyric,
                    fixedHeight = fixedHeight
                )

                val stickers = remember { sticker() }
                stickers.firstOrNull { it is Sticker.ExtSticker }?.let {
                    Image(
                        painter = painterResource(id = mimeTypeToIcon(mimeType = it.name)),
                        contentDescription = "MediaType Icon",
                        colorFilter = dayNightTextColorFilter(0.9f),
                        modifier = Modifier
                            .size(20.dp)
                            .aspectRatio(1f)
                    )
                }
            }
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
    duration: () -> Long,
    fixedHeight: () -> Boolean = { false },
    isPlaying: () -> Boolean = { false },
    showPrefix: () -> Boolean = { false },
    stickerContent: @Composable RowScope.() -> Unit = {},
    prefixContent: @Composable (Modifier) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title(),
                maxLines = if (fixedHeight()) 1 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                color = dayNightTextColor(),
                style = MaterialTheme.typography.subtitle1
            )
            stickerContent()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayingTipIcon(isPlaying = isPlaying)
            AnimatedVisibility(
                visible = showPrefix(),
                modifier = Modifier.wrapContentWidth(),
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.caption
                        .copy(color = dayNightTextColor(0.5f)),
                ) {
                    prefixContent(Modifier.padding(end = 5.dp))
                }
            }
            Text(
                modifier = Modifier.weight(1f),
                text = subTitle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = dayNightTextColor(0.5f),
                style = MaterialTheme.typography.caption,
            )
            Text(
                modifier = Modifier.padding(start = 5.dp),
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
    hasLyric: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false }
) {
    if (fixedHeight()) {
        AnimatedVisibility(
            visible = hasLyric(),
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_lrc_fill),
                contentDescription = "Lyric Icon",
                colorFilter = dayNightTextColorFilter(0.9f),
                modifier = Modifier
                    .size(20.dp)
                    .aspectRatio(1f)
            )
        }
    } else {
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
}