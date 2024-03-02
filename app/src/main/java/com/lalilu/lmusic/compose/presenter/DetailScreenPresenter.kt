package com.lalilu.lmusic.compose.presenter

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.lalilu.common.base.Playable
import com.lalilu.component.base.UiAction
import com.lalilu.component.base.UiState
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplaylist.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

sealed class DetailScreenAction : UiAction {
    data object Like : DetailScreenAction()
    data object UnLike : DetailScreenAction()
    data object PlayPause : DetailScreenAction()
}

data class DetailScreenLikeBtnState(
    val isLiked: Boolean,
    val onAction: (action: UiAction) -> Unit
) : UiState

data class DetailScreenIsPlayingState(
    val isPlaying: Boolean,
    val onAction: (action: UiAction) -> Unit
) : UiState

@SuppressLint("ComposableNaming")
@Composable
fun DetailScreenLikeBtnPresenter(
    mediaId: String,
    playlistRepo: PlaylistRepository = koinInject(),
): DetailScreenLikeBtnState {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val isLiked by playlistRepo.isItemInFavourite(mediaId).collectAsState(initial = false)

    return DetailScreenLikeBtnState(isLiked = isLiked) {
        when (it) {
            DetailScreenAction.Like -> scope.launch {
                playlistRepo.addMediaIdsToFavourite(mediaIds = listOf(mediaId))
            }

            DetailScreenAction.UnLike -> scope.launch {
                playlistRepo.removeMediaIdsFromFavourite(mediaIds = listOf(mediaId))
            }
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun DetailScreenIsPlayingPresenter(
    mediaId: String,
    playingVM: PlayingViewModel = koinInject()
): DetailScreenIsPlayingState {
    val isPlaying = playingVM.isItemPlaying(mediaId, Playable::mediaId)

    return DetailScreenIsPlayingState(isPlaying = isPlaying) {
        when (it) {
            DetailScreenAction.PlayPause -> playingVM.play(
                mediaId = mediaId,
                addToNext = true,
                playOrPause = true
            )
        }
    }
}