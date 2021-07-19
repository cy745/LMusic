package com.lalilu.lmusic.state

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.service.LMusicService.Companion.ACTION_PLAY_PAUSE

class MainActivityViewModel : ViewModel() {
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()

    fun seekBarClick() {
        val playerModule = LMusicPlayerModule.getInstance(null)
        playerModule.mediaController.value?.transportControls?.sendCustomAction(
            ACTION_PLAY_PAUSE, null
        )
    }
}