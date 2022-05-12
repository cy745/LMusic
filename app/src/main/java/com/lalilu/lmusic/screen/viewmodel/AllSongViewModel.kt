package com.lalilu.lmusic.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.lalilu.lmusic.datasource.ALL_ID
import com.lalilu.lmusic.datasource.MMediaSource
import com.lalilu.lmusic.service.MSongBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class AllSongViewModel @Inject constructor(
    private val mediaSource: MMediaSource,
    private val mediaBrowser: MSongBrowser
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    val allSongsLiveData = flow {
        emit(mediaSource.getChildren(ALL_ID) ?: emptyList())
    }

    fun playSongWithAllSongPlaylist(items: List<MediaItem>, index: Int) =
        launch(Dispatchers.Main) {
            mediaBrowser.browser?.apply {
                clearMediaItems()
                setMediaItems(items)
                seekToDefaultPosition(index)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                play()
            }
        }
}