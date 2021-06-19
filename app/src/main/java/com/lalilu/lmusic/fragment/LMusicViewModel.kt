package com.lalilu.lmusic.fragment

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.jetbrains.annotations.Nullable

class LMusicViewModel private constructor(application: Application) :
    AndroidViewModel(application) {
    val mediaList = MutableLiveData<MutableList<MediaBrowserCompat.MediaItem>>(null)
    val metadata = MutableLiveData<MediaMetadataCompat>(null)
    val playBackState = MutableLiveData<PlaybackStateCompat>(null)
    val mediaController = MutableLiveData<MediaControllerCompat>(null)

    companion object {
        @Volatile
        private var instance: LMusicViewModel? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): LMusicViewModel {
            instance ?: synchronized(LMusicViewModel::class.java) {
                if (application == null) throw NullPointerException("No Application Context Input")
                instance = LMusicViewModel(application)
            }
            return instance!!
        }
    }
}