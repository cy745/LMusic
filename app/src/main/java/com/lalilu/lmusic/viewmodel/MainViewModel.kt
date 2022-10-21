package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.utils.SelectHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val songSelectHelper = SelectHelper<LSong>()
    val playlistSelectHelper = SelectHelper<LPlaylist>()

    @Composable
    fun navToAddToPlaylist(): () -> Unit {
        val navToAddToPlaylistAction = ScreenActions.navToAddToPlaylist()

        return remember {
            {
                if (songSelectHelper.selectedItem.size > 0) {
                    navToAddToPlaylistAction()
                } else {
                    ToastUtils.showShort("请先选择歌曲")
                }
            }
        }
    }

    fun playSongWithPlaylist(items: List<LSong>, item: LSong) = viewModelScope.launch {
        LMusicBrowser.setSongs(items, item)
        LMusicBrowser.reloadAndPlay()
    }
}