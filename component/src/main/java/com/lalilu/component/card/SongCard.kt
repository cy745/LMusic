package com.lalilu.component.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lalilu.common.base.Sticker
import com.lalilu.component.R
import com.lalilu.component.extension.durationMsToString
import com.lalilu.lmedia.entity.LSong

@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(20.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    song: () -> LSong,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onEnterSelect: () -> Unit = {},
    isFavour: () -> Boolean,
    hasLyric: () -> Boolean = { false },
    isPlaying: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    showPrefix: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false },
    reverseLayout: () -> Boolean = { false },
    stickerContent: @Composable RowScope.() -> Unit = {
        StickerRow(
            isFavour = isFavour,
            hasLyric = hasLyric,
            extSticker = Sticker.ExtSticker(song().fileInfo.mimeType)
        )
    },
    prefixContent: @Composable (Modifier) -> Unit = {}
) {
    val item = remember { song() }

    SongCard(
        modifier = modifier,
        dragModifier = dragModifier,
        horizontalArrangement = horizontalArrangement,
        interactionSource = interactionSource,
        title = { item.metadata.title },
        subTitle = { item.metadata.artist },
        duration = { item.metadata.duration },
        imageData = { item },
        onClick = onClick,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onEnterSelect = onEnterSelect,
        isPlaying = isPlaying,
        fixedHeight = fixedHeight,
        reverseLayout = reverseLayout,
        isSelected = isSelected,
        showPrefix = showPrefix,
        stickerContent = stickerContent,
        prefixContent = prefixContent
    )
}


@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    dragModifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(20.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    paddingValues: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
    title: () -> String,
    subTitle: () -> String,
    duration: () -> Long,
    imageData: () -> Any?,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onEnterSelect: () -> Unit = {},
    isPlaying: () -> Boolean = { false },
    fixedHeight: () -> Boolean = { false },
    reverseLayout: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    showPrefix: () -> Boolean = { false },
    stickerContent: @Composable RowScope.() -> Unit = {},
    prefixContent: @Composable (Modifier) -> Unit = {}
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected()) MaterialTheme.colors.onBackground.copy(0.15f)
        else Color.Transparent,
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
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick
            )
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        if (reverseLayout()) {
            SongCardImage(
                modifier = dragModifier,
                imageData = imageData,
                interaction = interactionSource,
                onClick = onClick,
                onLongClick = onEnterSelect
            )
        }

        SongCardContent(
            modifier = Modifier.weight(1f),
            title = title,
            subTitle = subTitle,
            duration = duration,
            isPlaying = isPlaying,
            showPrefix = showPrefix,
            fixedHeight = fixedHeight,
            prefixContent = prefixContent,
            stickerContent = stickerContent
        )

        if (!reverseLayout()) {
            SongCardImage(
                modifier = dragModifier,
                imageData = imageData,
                interaction = interactionSource,
                onClick = onClick,
                onLongClick = onEnterSelect
            )
        }
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
        modifier = modifier.wrapContentHeight(),
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
                color = MaterialTheme.colors.onBackground,
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
                        .copy(color = MaterialTheme.colors.onBackground.copy(0.5f)),
                ) {
                    prefixContent(Modifier.padding(end = 5.dp))
                }
            }
            Text(
                modifier = Modifier.weight(1f),
                text = subTitle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onBackground.copy(0.5f),
                style = MaterialTheme.typography.caption,
            )
            Text(
                modifier = Modifier.padding(start = 5.dp),
                text = durationMsToString(duration = duration()),
                fontSize = 12.sp,
                letterSpacing = 0.05.em,
                color = MaterialTheme.colors.onBackground.copy(0.7f)
            )
        }
    }
}

@Composable
fun SongCardImage(
    modifier: Modifier = Modifier,
    imageData: () -> Any?,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
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


@Preview(showBackground = true)
@Composable
private fun SongCardPreview() {
    SongCard(
        title = { "歌いましょう鳴らしましょう" },
        subTitle = { "MyGO!!!!!" },
        duration = { 189999L },
        imageData = { "https://api.sretna.cn/layout/pc.php" }
    )
}

@Preview(showBackground = true)
@Composable
private fun SongCardPreviewMulti() {
    Column {
        SongCard(
            title = { "测试" },
            subTitle = { "测试" },
            duration = { 159999L },
            imageData = { "https://api.sretna.cn/layout/pc.php" }
        )
        SongCard(
            title = { "测试" },
            subTitle = { "测试" },
            duration = { 159999L },
            imageData = { "https://api.sretna.cn/layout/pc.php" }
        )
        SongCard(
            title = { "测试" },
            subTitle = { "测试" },
            duration = { 159999L },
            imageData = { "https://api.sretna.cn/layout/pc.php" }
        )
        SongCard(
            title = { "测试" },
            subTitle = { "测试" },
            duration = { 159999L },
            imageData = { "https://api.sretna.cn/layout/pc.php" }
        )
    }
}