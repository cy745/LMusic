package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.entity.Song

class MusicServiceViewModel private constructor() : ViewModel() {
    private var playingSong: MutableLiveData<Song> = MutableLiveData()
    private var songList: MutableLiveData<List<Song>> = MutableLiveData()

    companion object {
        @Volatile
        private var mInstance: MusicServiceViewModel? = null

        fun getInstance(): MusicServiceViewModel {
            if (mInstance == null) synchronized(MusicServiceViewModel::class.java) {
                if (mInstance == null) mInstance = MusicServiceViewModel()
            }
            return mInstance!!
        }
    }

    fun getPlayingSong(): MutableLiveData<Song> = playingSong
    fun getSongList(): MutableLiveData<List<Song>> = songList
}