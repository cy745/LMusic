package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.service.LMusicBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val browser: LMusicBrowser
) : ViewModel() {

    var tempSongs: SnapshotStateList<LSong> = mutableStateListOf()
        private set

    @Composable
    fun navToAddToPlaylist(): (songs: List<LSong>) -> Unit {
        val navToAddToPlaylistAction = ScreenActions.navToAddToPlaylist()

        return remember {
            { songs ->
                if (songs.isEmpty()) {
                    ToastUtils.showShort("请先选择歌曲")
                } else {
                    tempSongs.clear()
                    tempSongs.addAll(songs)
                    navToAddToPlaylistAction("isAdding=${true}")
                }
            }
        }
    }

    fun playSongWithPlaylist(items: List<LSong>, item: LSong) = viewModelScope.launch {
        browser.setSongs(items, item)
        browser.reloadAndPlay()
    }
}