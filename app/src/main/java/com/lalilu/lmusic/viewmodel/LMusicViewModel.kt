package com.lalilu.lmusic.viewmodel

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmusic.LMusicList

class LMusicViewModel {
    val mList: MutableLiveData<LMusicList<String, MediaBrowserCompat.MediaItem>> =
        MutableLiveData(null)

    companion object {
        @Volatile
        private var mInstance: LMusicViewModel? = null

        fun getInstance(): LMusicViewModel {
            if (mInstance == null) synchronized(LMusicViewModel::class.java) {
                if (mInstance == null) mInstance = LMusicViewModel()
            }
            return mInstance!!
        }
    }
}