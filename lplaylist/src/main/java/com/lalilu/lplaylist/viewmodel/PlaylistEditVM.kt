package com.lalilu.lplaylist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.MviWithIntent
import com.lalilu.common.mviImplWithIntent
import com.lalilu.component.extension.toMutableState
import com.lalilu.component.extension.toState
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
data class PlaylistEditState(
    val playlistId: String,
) {
    fun getPlaylistFlow(playlistRepo: PlaylistRepository): Flow<LPlaylist?> {
        return playlistRepo.getPlaylistsFlow().mapLatest { list ->
            list.firstOrNull { it.id == playlistId }
        }
    }
}

sealed interface PlaylistEditAction {
    data object Confirm : PlaylistEditAction
}

sealed interface PlaylistEditEvent {

}

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
@KoinViewModel
class PlaylistEditVM(
    playlistId: String? = null,
    private val actualId: String = playlistId ?: Uuid.random().toHexString(),
    private val playlistRepo: PlaylistRepository
) : ViewModel(),
    MviWithIntent<PlaylistEditState, PlaylistEditEvent, PlaylistEditAction>
    by mviImplWithIntent(PlaylistEditState(actualId)) {

    val state = stateFlow()
        .toState(PlaylistEditState(actualId), viewModelScope)

    private val playlistFlow = stateFlow()
        .distinctUntilChangedBy { it.playlistId }
        .flatMapLatest { it.getPlaylistFlow(playlistRepo) }

    val titleState = playlistFlow
        .mapLatest { it?.title ?: "" }
        .toMutableState("", viewModelScope)

    val subTitleState = playlistFlow
        .mapLatest { it?.subTitle ?: "" }
        .toMutableState("", viewModelScope)

    val playlist = playlistFlow
        .toState(viewModelScope)

    override fun intent(intent: PlaylistEditAction) = viewModelScope.launch {
        when (intent) {
            is PlaylistEditAction.Confirm -> {
                if (titleState.value.isBlank()) {
                    ToastUtils.showShort("歌单标题不能为空")
                    return@launch
                }

                playlistRepo.save(
                    LPlaylist(
                        id = state.value.playlistId,
                        title = titleState.value,
                        subTitle = subTitleState.value,
                        mediaIds = playlist.value?.mediaIds ?: emptyList(),
                        coverUri = playlist.value?.coverUri ?: "",
                    )
                )

                AppRouter.intent(NavIntent.Pop)
            }

            else -> {}
        }
    }
}