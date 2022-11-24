package com.lalilu.lmusic.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.remote.StringUdpService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val udpService: StringUdpService
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
                    navToAddToPlaylistAction(mapOf("isAdding" to true))
                }
            }
        }
    }

    val searching = mutableStateOf(false)
    val remoteDeviceList = mutableStateListOf<String>()

    fun stopListen() {
        udpService.stopListen()
        remoteDeviceList.clear()
    }

    fun search2() {
        udpService.startListenStr {
            remoteDeviceList.add("${System.currentTimeMillis()} $it")
        }
        viewModelScope.launch {
            searching.value = true
            repeat(5) {
                udpService.broadcast("REQUEST CONNECT")
                delay(3000)
            }
            searching.value = false
        }
    }
}

val LocalMainVM = compositionLocalOf<MainViewModel> {
    error("MainViewModel hasn't not presented")
}