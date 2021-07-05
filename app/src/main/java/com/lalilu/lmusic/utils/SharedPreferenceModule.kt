package com.lalilu.lmusic.utils

import android.app.Application
import org.jetbrains.annotations.Nullable

class SharedPreferenceModule(application: Application) {
    val lastPlaySp = application.getSharedPreferences(LAST_PLAY_SP, 0)

    companion object {
        const val LAST_PLAY_SP = "last_play_sp"
        const val LAST_MUSIC_ID = "last_music_id"
        const val LAST_PLAYLIST_ID = "last_playlist_id"


        @Volatile
        private var instance: SharedPreferenceModule? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): SharedPreferenceModule {
            instance = instance ?: synchronized(SharedPreferenceModule::class.java) {
                application ?: throw NullPointerException("No Application Context Input")
                instance ?: SharedPreferenceModule(application)
            }
            return instance!!
        }
    }
}