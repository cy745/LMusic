package com.lalilu.lmusic.compose.presenter

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.lalilu.common.base.Playable
import com.lalilu.lmusic.compose.UiAction
import com.lalilu.lmusic.compose.UiState
import com.lalilu.lmusic.viewmodel.PlayingViewModel
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
//    favRepo: FavoriteRepository = koinInject()
): DetailScreenLikeBtnState {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val isLiked by remember { mutableStateOf(false) }

    return DetailScreenLikeBtnState(isLiked = isLiked) {
        when (it) {
            DetailScreenAction.Like -> scope.launch { }
            DetailScreenAction.UnLike -> scope.launch { }
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