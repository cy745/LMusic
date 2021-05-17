package com.lalilu.lmusic.viewmodel

import android.app.Application
import androidx.annotation.Nullable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Song

class MusicDataBaseViewModel private constructor(application: Application) :
    AndroidViewModel(application) {
    private var liveDataSongs: LiveData<List<Song>> =
        MusicDatabase.getInstance(application).songDao().getAllLiveData()

    companion object {
        @Volatile
        private var mInstance: MusicDataBaseViewModel? = null

        fun getInstance(@Nullable application: Application?): MusicDataBaseViewModel {
            if (application == null) throw NullPointerException()

            if (mInstance == null) synchronized(MusicServiceViewModel::class.java) {
                if (mInstance == null) mInstance = MusicDataBaseViewModel(application)
            }
            return mInstance!!
        }
    }

    fun getSongsLiveDate() = liveDataSongs
}