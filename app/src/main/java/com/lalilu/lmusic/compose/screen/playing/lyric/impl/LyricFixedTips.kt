package com.lalilu.lmusic.compose.screen.playing.lyric.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContext
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings

@Composable
fun LyricFixedTips(
    index: Int,
    item: LyricItem.FixedTips,
    modifier: Modifier = Modifier,
    settings: LyricSettings,
    context: LyricContext,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 36.dp)
            .padding(settings.containerPadding),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.content,
            style = settings.translationTextStyle,
            color = Color(0x80FFFFFF)
        )
    }
}