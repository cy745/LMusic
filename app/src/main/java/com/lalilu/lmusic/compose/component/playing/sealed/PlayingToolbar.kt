package com.lalilu.lmusic.compose.component.playing.sealed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.lalilu.R
import com.lalilu.common.base.Playable
import com.lalilu.lmusic.compose.component.playing.PlayingHeader
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.LPlayer
import org.koin.compose.koinInject


@Composable
fun PlayingToolbar(
    playingVM: PlayingViewModel = koinInject(),
) {
    val song by LPlayer.runtime.info.playingFlow.collectAsState(null)
    val defaultSloganStr = stringResource(id = R.string.default_slogan)

    PlayingHeader(
        title = { song?.title?.takeIf(String::isNotBlank) ?: defaultSloganStr },
        subTitle = { song?.subTitle ?: defaultSloganStr },
        isPlaying = {
            song?.let { playingVM.isItemPlaying(it.mediaId, Playable::mediaId) } ?: false
        }
    )
}