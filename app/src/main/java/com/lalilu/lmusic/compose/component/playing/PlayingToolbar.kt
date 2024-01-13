package com.lalilu.lmusic.compose.component.playing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.common.base.Playable
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.LPlayer


@Composable
fun PlayingToolbar(
    playingVM: PlayingViewModel = singleViewModel(),
    extraVisible: () -> Boolean = { true },
) {
    val song by LPlayer.runtime.info.playingFlow.collectAsState(null)
    val defaultSloganStr = stringResource(id = R.string.default_slogan)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 25.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlayingHeader(
            modifier = Modifier.weight(1f),
            title = { song?.title?.takeIf(String::isNotBlank) ?: defaultSloganStr },
            subTitle = { song?.subTitle ?: defaultSloganStr },
            isPlaying = {
                song?.let { playingVM.isItemPlaying(it.mediaId, Playable::mediaId) } ?: false
            }
        )

        AnimatedVisibility(
            visible = extraVisible(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LyricViewToolbar()
        }
    }
}