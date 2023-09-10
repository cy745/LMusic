package com.lalilu.lmusic.compose.component.playing.sealed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.lalilu.R
import com.lalilu.lmusic.compose.component.playing.PlayingHeader
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import org.koin.compose.koinInject


@Composable
fun PlayingToolbar(
    playingVM: PlayingViewModel = koinInject()
) {
    val song by playingVM.runtime.playingFlow.collectAsState(null)
    val defaultSloganStr = stringResource(id = R.string.default_slogan)

    PlayingHeader(
        title = { song?.name?.takeIf(String::isNotBlank) ?: defaultSloganStr },
        subTitle = { song?._artist ?: defaultSloganStr },
        isPlaying = { song?.let { playingVM.isSongPlaying(mediaId = it.id) } ?: false }
    )
}